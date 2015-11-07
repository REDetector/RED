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

import com.xl.database.DatabaseManager;
import com.xl.database.Query;
import com.xl.datatypes.sites.SiteBean;
import com.xl.filter.Filter;
import com.xl.utils.RandomStringGenerator;
import com.xl.utils.Timer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * The Class RepeatRegionsFilter is a rule-based filter. Variants that were within repeat regions were excluded.
 * However, sites in SINE/Alu regions were remained since A-I RNA editing is pervasive in Alu repeats and it has been
 * implicated in human diseases such as breast cancer and Ewing's sarcoma.
 */
public class RepeatRegionsFilter2 implements Filter {
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Perform repeat regions filter. Variants that were within repeat regions were excluded.
     *
     * @param previousTable The previous table
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        logger.info("Start performing Repeat Regions Filter...\t" + Timer.getCurrentTime());
        String repeatTable = DatabaseManager.REPEAT_MASKER_TABLE_NAME;
        List<SiteBean> repeatRegionSites = new ArrayList<SiteBean>();
        List<SiteBean> nonRepeatRegionSites = new ArrayList<SiteBean>();
        List<SiteBean> aluRegionSites = new ArrayList<SiteBean>();
        try {
            int count = 0;
            Vector<SiteBean> sites = Query.queryAllEditingInfo(previousTable);
            for (SiteBean site : sites) {
                if (inRepeatRegion(site, repeatTable)) {
                    repeatRegionSites.add(site);
                } else {
                    nonRepeatRegionSites.add(site);
                }
            }

            String tempTable = RandomStringGenerator.createRandomString(10);
            databaseManager.executeSQL("create temporary table " + tempTable + " like " + repeatTable);
            databaseManager
                .executeSQL("insert into " + tempTable + " select * from " + repeatTable + " where type='SINE/Alu'");
            for (SiteBean site : repeatRegionSites) {
                if (inRepeatRegion(site, tempTable)) {
                    site.setIsAlu("T");
                    aluRegionSites.add(site);
                }
            }

            databaseManager.setAutoCommit(false);
            for (SiteBean site : nonRepeatRegionSites) {
                databaseManager.executeSQL(
                    "insert into " + currentTable + "(chrom,pos,id,ref,alt,qual,filter,info,gt,ad,dp,gq,pl,alu) "
                        + "values( " + site.toString() + ")");
                if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                    databaseManager.commit();
            }
            for (SiteBean site : aluRegionSites) {
                databaseManager.executeSQL(
                    "insert into " + currentTable + "(chrom,pos,id,ref,alt,qual,filter,info,gt,ad,dp,gq,pl,alu) "
                        + "values( " + site.toString() + ")");
                if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                    databaseManager.commit();
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Error execute sql clause in " + RepeatRegionsFilter2.class.getName() + ":performFilter()", e);
        }
        logger.info("End performing Repeat Regions Filter...\t" + Timer.getCurrentTime());
    }

    @Override
    public String getName() {
        return DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME;
    }

    private boolean inRepeatRegion(SiteBean site, String repeatTable) throws SQLException {
        // select begin from repeatTable limit 1 where site.chrom=repeatTable.chrom and site.pos<=repeatTable.end;
        ResultSet rs = databaseManager.query("select begin from " + repeatTable + " where chrom='" + site.getChr()
            + "' and end>=" + site.getPos() + " limit 1");
        int begin = rs != null && rs.next() ? rs.getInt(1) : Integer.MAX_VALUE;
        return site.getPos() >= begin;
    }
}
