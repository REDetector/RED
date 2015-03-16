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

package com.xl.database;

import com.xl.exception.DataLoadException;
import com.xl.main.REDApplication;
import com.xl.preferences.DatabasePreferences;
import com.xl.utils.Indexer;
import com.xl.utils.ui.OptionDialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLDataException;
import java.sql.SQLException;

/**
 * Created by Xing Li on 2014/11/13.
 * <p/>
 * TableCreator provides a variety of static methods to create different kinds of tables.
 */
public class TableCreator {
    private static final Logger logger = LoggerFactory.getLogger(TableCreator.class);
    /**
     * A reference of DatabaseManger.
     */
    private static DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Create a standard filter table except for FETFilter.
     *
     * @param tableName The table name to be created.
     */
    public static void createFilterTable(String tableName) throws SQLException {
        String sqlClause;
        if (databaseManager.existTable(tableName)) {
            int answer = OptionDialogUtils.showTableExistDialog(REDApplication.getInstance(), tableName);
            if (answer <= 0) {
                databaseManager.deleteTable(tableName);
            } else {
                return;
            }
        }
        String tableBuilder = DatabasePreferences.getInstance().getDatabaseTableBuilder();
        if (tableBuilder == null) {
            try {
                throw new DataLoadException("RNA/DNA vcf file has not been imported.");
            } catch (DataLoadException e) {
                logger.error("RNA/DNA vcf file has not been imported.", e);
                return;
            }
        }
        sqlClause = "create table " + tableName + "(" + tableBuilder + "," + Indexer.CHROM_POSITION + ")";
        databaseManager.executeSQL(sqlClause);
    }

    /**
     * Create a table for DARNED database.
     *
     * @param tableName Table name of DARNED database.
     */
    public static void createDARNEDTable(final String tableName) throws SQLException {
        if (!databaseManager.existTable(tableName)) {
            //"(chrom varchar(30),coordinate int,strand varchar(5),inchr varchar(5), inrna varchar(5) ,index(chrom,coordinate))");
            createReferenceTable(tableName, new String[]{"chrom", "coordinate", "strand", "inchr", "inrna"}, new String[]{
                    "varchar(30)", "int", "varchar(5)", "varchar(5)", "varchar(5)"
            }, Indexer.CHROM_COORDINATE);
        }

    }

    /**
     * Create a table for dbSNP database.
     *
     * @param tableName Table name of dbSNP database.
     */
    public static void createDBSNPTable(final String tableName) throws SQLException {
        if (!databaseManager.existTable(tableName)) {
            //chrom varchar(30),pos int,index(chrom,pos);
            createReferenceTable(tableName, new String[]{"chrom", "pos"}, new String[]{"varchar(30)", "int"}, Indexer.CHROM_POSITION);
        }
    }

    /**
     * Create a table for repeat regions file from RepeatMasker.
     *
     * @param tableName Table name of repeat regions file.
     */
    public static void createRepeatRegionsTable(final String tableName) throws SQLException {
        if (!databaseManager.existTable(tableName)) {
            //chrom varchar(30),begin int,end int,type varchar(40),index(chrom,begin,end);
            createReferenceTable(tableName, new String[]{"chrom", "begin", "end", "type"}, new String[]{"varchar(30)", "int", "int", "varchar(40)"},
                    Indexer.CHROM_BEGIN_END);
        }
    }

    /**
     * Create a table for gene annotation file which contains splice junction information.
     *
     * @param tableName Table name of gene annotation file.
     */
    public static void createSpliceJunctionTable(final String tableName) throws SQLException {
        if (!databaseManager.existTable(tableName)) {
            // "(chrom varchar(30),ref varchar(30),type varchar(9),begin int,end int,unuse1 float(8,6),unuse2 varchar(5),unuse3 varchar(5),
            // info varchar(100),index(chrom,type))");
            createReferenceTable(tableName, new String[]{"chrom", "ref", "type", "begin", "end", "score", "strand", "frame", "info"},
                    new String[]{"varchar(30)", "varchar(30)", "varchar(10)", "int", "int", "float(8,6)", "varchar(1)", "varchar(1)", "varchar(100)"},
                    Indexer.CHROM_TYPE);
        }
    }

    /**
     * Create a specific table for Fisher Exact Test Filter which have added information (i.e., level, p-value and fdr) to the standard filter table.
     *
     * @param tableName Table name of FETFilter.
     */
    public static void createFisherExactTestTable(String tableName) throws SQLException {
        String tableBuilder = DatabasePreferences.getInstance().getDatabaseTableBuilder();
        if (tableBuilder == null) {
            try {
                throw new DataLoadException("RNA/DNA vcf file has not been imported.");
            } catch (DataLoadException e) {
                logger.error("RNA/DNA vcf file has not been imported.", e);
                return;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("create table ").append(tableName).append("(").append(tableBuilder);
        stringBuilder.append(",");
        stringBuilder.append("level float, pvalue float, fdr float");
        stringBuilder.append(",");
        stringBuilder.append(Indexer.CHROM_POSITION);
        stringBuilder.append(")");
        databaseManager.executeSQL(stringBuilder.toString());
    }

    /**
     * A standard to create a reference table.
     *
     * @param tableName    The table name to be created.
     * @param columnNames  The column names.
     * @param columnParams The standard column parameters, it must be supported by MySQL database.
     * @param index        Index we use when creating a table, which can be obtained from {@link com.xl.utils.Indexer Indexer} class.
     */
    private static void createReferenceTable(String tableName, String[] columnNames, String[] columnParams, String index) throws SQLException {
        if (columnNames == null || columnParams == null || columnNames.length == 0 || columnNames.length != columnParams.length) {
            throw new SQLDataException("Column names and column parameters can't not be null or zero-length.");
        }
        // Create table if not exists TableName(abc int, def varchar(2), hij text);
        StringBuilder stringBuilder = new StringBuilder("create table if not exists " + tableName + "(");
        stringBuilder.append(columnNames[0]).append(" ").append(columnParams[0]);
        for (int i = 1, len = columnNames.length; i < len; i++) {
            stringBuilder.append(", ").append(columnNames[i]).append(" ").append(columnParams[i]);
        }
        stringBuilder.append(",");
        stringBuilder.append(index);
        stringBuilder.append(")");
        databaseManager.executeSQL(stringBuilder.toString());
    }
}
