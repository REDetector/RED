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
import com.xl.utils.Timer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * The Class SpliceJunctionFilter is a rule-based filter. Variants that were within+/-k bp (e.g., k = 2) of the splice
 * junction, which were supposed to be unreliable, were excluded based on the gene annotation file.
 */
public class SpliceJunctionFilter2 implements Filter {
    public static final String PARAMS_INT_EDGE = "edge";
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Perform splice junction filter as user's preference. Variants that were within +/-k bp (e.g., k = 2) of the
     * splice junction, which were supposed to be unreliable, were excluded based on the gene annotation file.
     *
     * @param previousTable The previous table
     * @param currentTable The result table
     * @param params The threshold of splice junction
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return;
        } else if (params.size() != 1) {
            throw new IllegalArgumentException(
                "Args " + params.toString() + " for Splice Junction Filter are incomplete, please have a check");
        }
        logger.info("Start performing Splice Junction Filter...\t" + Timer.getCurrentTime());
        String spliceJunctionTable = DatabaseManager.SPLICE_JUNCTION_TABLE_NAME;
        int edge = Integer.parseInt(params.get(PARAMS_INT_EDGE));
        List<SiteBean> spliceJunctionSites = new ArrayList<SiteBean>();
        try {
            // insert into currentTable select * from previousTable where not exist (select chrom from splice_junction
            // where ( splice_junction.type='CDS' and
            // splice_junction.chrom=previousTable.chrom and ((splice_junction.begin-edge<previousTable.pos and
            // splice_junction.begin+edge>previousTable.pos)
            // or (splice_junction.end<previousTable.pos+edge and splice_junction.end>previousTable.pos-edge))))
            int count = 0;
            Vector<SiteBean> sites = Query.queryAllEditingInfo(previousTable);
            for (SiteBean site : sites) {
                if (!inSpliceJunction(site, spliceJunctionTable, edge)) {
                    spliceJunctionSites.add(site);
                }
            }
            databaseManager.setAutoCommit(false);
            for (SiteBean site : spliceJunctionSites) {
                databaseManager.executeSQL(
                    "insert into " + currentTable + "(chrom,pos,id,ref,alt,qual,filter,info,gt,ad,dp,gq,pl,alu) "
                        + "values( " + site.toString() + ")");
                if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                    databaseManager.commit();
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Error execute sql clause in" + SpliceJunctionFilter2.class.getName() + ":performFilter()", e);
        }
        logger.info("End performing Splice Junction Filter...\t" + Timer.getCurrentTime());
    }

    private boolean inSpliceJunction(SiteBean site, String spliceJunctionTable, int edge) throws SQLException {
        // select begin from spliceJunctionTable limit 1 where site.chrom=spliceJunctionTable.chrom and
        // site.pos<=spliceJunctionTable.end;
        ResultSet rs = databaseManager.query("select begin,end,type from " + spliceJunctionTable + " where chrom='"
            + site.getChr() + "' and end>=" + site.getPos() + " limit 1");
        int pos = site.getPos();
        if (rs != null && rs.next()) {
            int begin = rs.getInt(1);
            int end = rs.getInt(2);
            String type = rs.getString(3);
            return type.equals("SINE/Alu")
                && ((pos > begin - edge && pos < begin + edge) || (pos > end - edge && pos < end + edge));
        }
        return false;
    }

    @Override
    public String getName() {
        return DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME;
    }
}
