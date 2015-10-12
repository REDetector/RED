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

package com.xl.filter.dnarna;

import com.xl.database.DatabaseManager;
import com.xl.filter.Filter;
import com.xl.utils.NegativeType;
import com.xl.utils.Timer;

import java.sql.SQLException;
import java.util.Map;

/**
 * The Class DnaRnaFilter is a rule-based filter. RNA-seq variants where its counterparts in genomic DNA is not
 * reference homozygote (e.g., AA) would be excluded if DNA sequencing data is available.
 */
public class DnaRnaFilter implements Filter {
    public static final String PARAMS_STRING_DNA_VCF_TABLE = "dnavcf";
    public static final String PARAMS_STRING_EDITING_TYPE = "editingtype";
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Perform DNA-RNA filter. RNA-seq variants where its counterparts in genomic DNA is not reference homozygote (e.g.,
     * AA) would be excluded if DNA sequencing data is available.
     *
     * @param previousTable The previous table
     * @param currentTable The result table
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return;
        } else if (params.size() != 2) {
            throw new IllegalArgumentException("Args " + params.toString()
                + " for DNA-RNA Filter are incomplete, please have a check");
        }
        logger.info("Start performing DNA-RNA Filter...\t" + Timer.getCurrentTime());
        String dnaVcfTable = params.get(PARAMS_STRING_DNA_VCF_TABLE);
        String editingType = params.get(PARAMS_STRING_EDITING_TYPE);
        String negativeType = NegativeType.getNegativeStrandEditingType(editingType);
        String darnedTable = DatabaseManager.KNOWN_RNA_EDITING_TABLE_NAME;
        /**
         * chrom | coordinate | strand | inchr | inrna
         */
        try {
            logger.info("Start selecting data from DNA VCF table...\t" + Timer.getCurrentTime());
            databaseManager.executeSQL("insert into " + currentTable + " select * from " + previousTable
                + " where exists (select chrom from " + dnaVcfTable + " where (" + dnaVcfTable + ".chrom="
                + previousTable + ".chrom and " + dnaVcfTable + ".pos=" + previousTable + ".pos and (" + dnaVcfTable
                + ".ref='" + editingType.charAt(0) + "' or  " + dnaVcfTable + ".ref='" + negativeType.charAt(0)
                + "')))");

            databaseManager.executeSQL("insert into " + currentTable + " select * from " + previousTable
                + " where exists (select chrom from " + darnedTable + " where (" + darnedTable + ".chrom="
                + previousTable + ".chrom and " + darnedTable + ".coordinate=" + previousTable + ".pos and ("
                + darnedTable + ".inchr='" + editingType.charAt(0) + "' or  " + darnedTable + ".inchr='"
                + negativeType.charAt(0) + "')))");

            logger.info("End selecting data from DNA VCF table...\t" + Timer.getCurrentTime());
        } catch (SQLException e) {
            logger.error("Error execute sql clause in " + DnaRnaFilter.class.getName() + ":performFilter()", e);
        }
        logger.info("End performing DNA-RNA Filter...\t" + Timer.getCurrentTime());
    }

    @Override
    public String getName() {
        return DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME;
    }
}
