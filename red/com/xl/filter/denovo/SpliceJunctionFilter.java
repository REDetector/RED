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

import java.sql.SQLException;
import java.util.Map;

import com.xl.database.DatabaseManager;
import com.xl.database.TableCreator;
import com.xl.filter.Filter;
import com.xl.utils.Timer;

/**
 * The Class SpliceJunctionFilter is a rule-based filter. Variants that were within+/-k bp (e.g., k = 2) of the splice
 * junction, which were supposed to be unreliable, were excluded based on the gene annotation file.
 */
public class SpliceJunctionFilter implements Filter {
    public static final String PARAMS_INT_EDGE = "edge";
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Perform splice junction filter as user's preference. Variants that were within +/-k bp (e.g., k = 2) of the
     * splice junction, which were supposed to be unreliable, were excluded based on the gene annotation file.
     *
     * @param spliceJunctionTable The gene annotation file table name, it is constant.
     * @param spliceJunctionResultTable The result table
     * @param previousTable The previous table
     * @param splicejunction The threshold of splice junction
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return;
        } else if (params.size() != 1) {
            throw new IllegalArgumentException("Args " + params.toString()
                + " for Splice Junction Filter are incomplete, please have a check");
        }
        TableCreator.createFilterTable(previousTable, currentTable);
        logger.info("Start performing Splice Junction Filter...\t" + Timer.getCurrentTime());
        String spliceJunctionTable = DatabaseManager.SPLICE_JUNCTION_TABLE_NAME;
        int edge = Integer.parseInt(params.get(PARAMS_INT_EDGE));
        try {
            databaseManager.executeSQL("insert into " + currentTable + " select * from " + previousTable
                + " where not exists (select chrom from " + spliceJunctionTable + " where (" + spliceJunctionTable
                + ".type='CDS' and " + spliceJunctionTable + ".chrom=" + previousTable + ".chrom" + " and (("
                + spliceJunctionTable + ".begin<" + previousTable + ".pos+" + edge + " and " + spliceJunctionTable
                + ".begin>" + previousTable + "" + ".pos-" + edge + ") or (" + spliceJunctionTable + ".end<"
                + previousTable + ".pos+" + edge + " and " + spliceJunctionTable + ".end>" + previousTable + ".pos-"
                + edge + "))))");
        } catch (SQLException e) {
            logger.error("Error execute sql clause in" + SpliceJunctionFilter.class.getName() + ":performFilter()", e);
        }
        logger.info("End performing Splice Junction Filter...\t" + Timer.getCurrentTime());
    }

    @Override
    public String getName() {
        return DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME;
    }
}
