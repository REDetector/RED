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
import com.xl.utils.RandomStringGenerator;
import com.xl.utils.Timer;

import java.sql.SQLException;
import java.util.Map;

/**
 * The Class RepeatRegionsFilter is a rule-based filter. Variants that were within repeat regions were excluded.
 * However, sites in SINE/Alu regions were remained since A-I RNA editing is pervasive in Alu repeats and it has been
 * implicated in human diseases such as breast cancer and Ewing's sarcoma.
 */
public class RepeatRegionsFilter implements Filter {
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Perform repeat regions filter. Variants that were within repeat regions were excluded.
     *
     * @param repeatTable The repeat file table name, it is constant.
     * @param repeatResultTable The repeat result table
     * @param aluResultTable The Alu result table
     * @param previousTable The previous table
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        TableCreator.createFilterTable(previousTable, currentTable);
        logger.info("Start performing Repeat Regions Filter...\t" + Timer.getCurrentTime());
        String repeatTable = DatabaseManager.REPEAT_MASKER_TABLE_NAME;
        try {
            databaseManager.executeSQL("insert into " + currentTable + " select * from " + previousTable
                + " where not exists (select * from " + repeatTable + " where (" + repeatTable + ".chrom= "
                + previousTable + ".chrom and  " + repeatTable + ".begin<=" + previousTable + ".pos and " + repeatTable
                + ".end>=" + previousTable + ".pos)) ");

            logger.info("Start finding sites in Alu Regions...\t" + Timer.getCurrentTime());
            String tempTable = RandomStringGenerator.createRandomString(10);
            databaseManager.executeSQL("create temporary table " + tempTable + " like " + currentTable);
            databaseManager.executeSQL("insert into " + tempTable + " SELECT * from " + previousTable
                + " where exists (select chrom from " + repeatTable + " where " + repeatTable + ".chrom = "
                + previousTable + ".chrom and " + repeatTable + ".begin<=" + previousTable + ".pos and " + repeatTable
                + ".end>=" + previousTable + ".pos and " + repeatTable + ".type='SINE/Alu')");
            databaseManager.executeSQL("update " + tempTable + " set alu = 'T'");
            databaseManager.executeSQL("insert into " + currentTable + " select * from " + tempTable);
            databaseManager.deleteTable(tempTable);
            logger.info("End finding sites in Alu Regions...\t" + Timer.getCurrentTime());

        } catch (SQLException e) {
            logger.error("Error execute sql clause in " + RepeatRegionsFilter.class.getName() + ":performFilter()", e);
        }
        logger.info("End performing Repeat Regions Filter...\t" + Timer.getCurrentTime());
    }

    @Override
    public String getName() {
        return DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME;
    }
}
