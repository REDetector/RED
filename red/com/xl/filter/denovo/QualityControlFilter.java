/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.filter.denovo;

import com.xl.database.DatabaseManager;
import com.xl.datatypes.sites.SiteBean;
import com.xl.utils.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class QualityControlFilter is a rule-based filter to filter RNA editing sites by their quality and coverage of depth.
 */
public class QualityControlFilter {
    private final Logger logger = LoggerFactory.getLogger(QualityControlFilter.class);
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager;

    /**
     * Initiate a new quality control filter.
     *
     * @param databaseManager the database manager
     */
    public QualityControlFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Perform quality control filter as user's preference. A given site would be removed if it was of a low quality (e.g., Q< 20) or with a low depth of
     * coverage.
     *
     * @param previousTable The previous filter table
     * @param qcResultTable The result table
     * @param quality       The threshold of quality
     * @param depth         The threshold of coverage of depth
     */
    public void executeQCFilter(String previousTable, String qcResultTable, double quality, int depth) throws SQLException {
        logger.info("Start executing QC filter... {}", Timer.getCurrentTime());
        int count = 0;
        ResultSet rs = databaseManager.query(previousTable, new String[]{"chrom", "pos", "AD"}, null, null);
        List<SiteBean> siteBeans = new ArrayList<SiteBean>();
        while (rs.next()) {
            SiteBean siteBean = new SiteBean(rs.getString(1), rs.getInt(2));
            siteBean.setAd(rs.getString(3));
            siteBeans.add(siteBean);
        }
        databaseManager.setAutoCommit(false);
        for (SiteBean siteBean : siteBeans) {
            String[] sections = siteBean.getAd().split("/");
            int ref_n = Integer.parseInt(sections[0]);
            int alt_n = Integer.parseInt(sections[1]);
            if (ref_n + alt_n >= depth) {
                databaseManager.executeSQL("insert into " + qcResultTable + " (select * from " + previousTable + " where filter='PASS' and pos=" + siteBean
                        .getPos() + " and qual >=" + quality + " and chrom='" + siteBean.getChr() + "')");
                if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                    databaseManager.commit();
            }
        }
        databaseManager.commit();
        databaseManager.setAutoCommit(true);
        logger.info("End executing QC filter... {}", Timer.getCurrentTime());
    }

}
