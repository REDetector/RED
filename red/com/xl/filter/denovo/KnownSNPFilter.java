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
import com.xl.database.TableCreator;
import com.xl.filter.Filter;
import com.xl.utils.Timer;

import java.sql.SQLException;
import java.util.Map;

/**
 * The Class KnownSNPFilter is a rule-based filter that will filter out the site which was known SNP in DNA level for
 * eliminating germline variants.
 */
public class KnownSNPFilter implements Filter {
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Perform dbSNP filter. We filter out the site in previous filter which is in dbSNP database at the same time.
     *
     * @param previousTable The previous table
     * @param currentTable The result table
     * @param params
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        logger.info("Start performing Known SNP Filter...\t" + Timer.getCurrentTime());
        TableCreator.createFilterTable(previousTable, currentTable);
        String dbSnpTable = DatabaseManager.DBSNP_DATABASE_TABLE_NAME;
        try {
            databaseManager.executeSQL("insert into " + currentTable + " select * from " + previousTable
                + " where not exists (select chrom from " + dbSnpTable + " where (" + dbSnpTable + ".chrom="
                + previousTable + ".chrom and " + dbSnpTable + ".pos=" + previousTable + ".pos))");
        } catch (SQLException e) {
            logger.error("Error execute sql clause in" + KnownSNPFilter.class.getName() + ":performFilter()", e);
        }
        logger.info("End performing Known SNP Filter...\t" + Timer.getCurrentTime());
    }

    @Override
    public String getName() {
        return DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME;
    }
}
