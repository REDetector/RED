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

package com.xl.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static boolean createFilterTable(String refTable, String tableName) {
        String sqlClause = null;
        try {
            sqlClause = "create table " + tableName + " like " + refTable;
            databaseManager.executeSQL(sqlClause);
        } catch (SQLException e) {
            logger.error("There is a syntax error for SQL clause: " + sqlClause, e);
        }
        return true;
    }

    public static boolean createRnaVcfTable(String tableName) {
        String sqlClause = null;
        try {
            sqlClause = "create table if not exists " + tableName
                + "(CHROM varchar(30), POS int, ID varchar(30),REF varchar(5),ALT varchar(5),QUAL float(10,2),FILTER text,"
                + "INFO text,GT varchar(10),REF_COUNT int,ALT_COUNT int,ALU varchar(1) default 'F',"
                + "STRAND varchar(1) default '+',P_VALUE float(10,8) default -1,FDR float(10,8) default -1,LEVEL float(10,8) default -1,index(chrom,pos))";
            databaseManager.executeSQL(sqlClause);
        } catch (SQLException e) {
            logger.error("There is a syntax error for SQL clause: " + sqlClause, e);
        }
        return true;
    }

    /**
     * A standard to create a reference table.
     *
     * @param tableName The table name to be created.
     * @param columnNames The column names.
     * @param columnParams The standard column parameters, it must be supported by MySQL database.
     * @param index Index we use when creating a table, which can be obtained from {@link com.xl.utils.Indexer Indexer}
     *            class.
     */
    public static void createReferenceTable(String tableName, String[] columnNames, String[] columnParams,
        String index) {
        if (columnNames == null || columnParams == null || columnNames.length == 0
            || columnNames.length != columnParams.length) {
            throw new IllegalArgumentException("Column names and column parameters can't not be null or zero-length.");
        }
        // Create table if not exists TableName(abc int, def varchar(2), hij text);
        StringBuilder stringBuilder = new StringBuilder("create table if not exists " + tableName + "(");
        stringBuilder.append(columnNames[0]).append(" ").append(columnParams[0]);
        for (int i = 1, len = columnNames.length; i < len; i++) {
            stringBuilder.append(", ").append(columnNames[i]).append(" ").append(columnParams[i]);
        }
        if (index != null && index.length() != 0) {
            stringBuilder.append(",");
            stringBuilder.append(index);
        }
        stringBuilder.append(")");
        try {
            databaseManager.executeSQL(stringBuilder.toString());
        } catch (SQLException e) {
            logger.error("There is a syntax error for SQL clause: " + stringBuilder.toString(), e);
        }
    }

    public static void createInfoTable() {
        String infoTable = DatabaseManager.INFORMATION_TABLE_NAME;
        if (!databaseManager.existTable(infoTable)) {
            String sql = "create table if not exists " + infoTable
                + "(tableName varchar(30) ,counts int, PRIMARY KEY (tableName))";
            try {
                databaseManager.executeSQL(sql);
            } catch (SQLException e) {
                logger.error("There is a syntax error for SQL clause: " + sql, e);
            }
        }
    }
}
