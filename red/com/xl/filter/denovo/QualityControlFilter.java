/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 * 
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.filter.denovo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xl.database.DatabaseManager;
import com.xl.database.TableCreator;
import com.xl.datatypes.sites.SiteBean;
import com.xl.filter.Filter;
import com.xl.utils.Timer;

/**
 * The Class QualityControlFilter is a rule-based filter to filter RNA editing sites by their quality and coverage of
 * depth.
 */
public class QualityControlFilter implements Filter {
    public static final String PARAMS_STRING_QUALITY = "quality";
    public static final String PARAMS_INT_DEPTH = "depth";
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Perform quality control filter as user's preference. A given site would be removed if it was of a low quality
     * (e.g., Q< 20) or with a low depth of coverage.
     *
     * @param previousTable The previous filter table
     * @param qcResultTable The result table
     * @param quality The threshold of quality
     * @param depth The threshold of coverage of depth
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return;
        } else if (params.size() != 2) {
            throw new IllegalArgumentException("Args " + params.toString()
                + " for Quality Control Filter are incomplete, please have a check");
        }
        String quality = params.get(PARAMS_STRING_QUALITY);
        int depth = Integer.parseInt(params.get(PARAMS_INT_DEPTH));
        logger.info("Start performing Quality Control Filter...\t" + Timer.getCurrentTime());
        try {
            int count = 0;
            ResultSet rs = databaseManager.query(previousTable, new String[] { "chrom", "pos", "AD" }, null, null);
            List<SiteBean> siteBeans = new ArrayList<SiteBean>();
            while (rs.next()) {
                if (rs.getString(3) != null) {
                    SiteBean siteBean = new SiteBean(rs.getString(1), rs.getInt(2));
                    siteBean.setAd(rs.getString(3));
                    siteBeans.add(siteBean);
                }
            }
            databaseManager.setAutoCommit(false);
            for (SiteBean siteBean : siteBeans) {
                String[] sections = siteBean.getAd().split("/");
                int ref_n = Integer.parseInt(sections[0]);
                int alt_n = Integer.parseInt(sections[1]);
                if (ref_n + alt_n >= depth) {
                    databaseManager.executeSQL("insert into " + currentTable + " (select * from " + previousTable
                        + " where filter='PASS' and pos=" + siteBean.getPos() + " and qual >=" + quality
                        + " and chrom='" + siteBean.getChr() + "')");
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Error execute sql clause in " + QualityControlFilter.class.getName() + ":performFilter()", e);
        }
        logger.info("End performing Quality Control Filter...\t" + Timer.getCurrentTime());
    }

    @Override
    public String getName() {
        return DatabaseManager.QC_FILTER_RESULT_TABLE_NAME;
    }
}
