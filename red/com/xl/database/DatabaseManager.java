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

/**
 * DatabaseManager is a class to manage database. We make it as a static private class so everyone can only use it in a single thread,
 * which will influence the efficiency, but in order to synchronize, we would like to make it.
 */

import com.xl.main.REDApplication;
import com.xl.preferences.DatabasePreferences;
import com.xl.utils.RandomStringGenerator;
import com.xl.utils.ui.IconLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class DatabaseManager {
    public static final int COMMIT_COUNTS_PER_ONCE = 10000;
    public static final String FILTER = "filter";
    public static final String DNA_RNA_DATABASE_NAME = "DNA_RNA_MODE";
    public static final String DENOVO_DATABASE_NAME = "DENOVO_MODE";
    public static final String RNA_VCF_RESULT_TABLE_NAME = "rnavcf";
    public static final String DNA_VCF_RESULT_TABLE_NAME = "dnavcf";
    public static final String QC_FILTER_RESULT_TABLE_NAME = "qcfilter";
    public static final String EDITING_TYPE_FILTER_RESULT_TABLE_NAME = "etfilter";
    public static final String SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME = "sjfilter";
    public static final String DBSNP_FILTER_RESULT_TABLE_NAME = "dbfilter";
    public static final String PVALUE_FILTER_RESULT_TABLE_NAME = "fetfilter";
    public static final String REPEAT_FILTER_RESULT_TABLE_NAME = "rrfilter";
    public static final String DNA_RNA_FILTER_RESULT_TABLE_NAME = "drfilter";
    public static final String LLR_FILTER_RESULT_TABLE_NAME = "llrfilter";
    public static final String ALU_FILTER_RESULT_TABLE_NAME = "alufilter";
    public static final String SPLICE_JUNCTION_TABLE_NAME = "splice_junction";
    public static final String DBSNP_DATABASE_TABLE_NAME = "dbsnp_database";
    public static final String REPEAT_MASKER_TABLE_NAME = "repeat_masker";
    public static final String DARNED_DATABASE_TABLE_NAME = "darned_database";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    /**
     * The single instance of DatabaseManager.
     */
    private static final DatabaseManager DATABASE_MANAGER = new DatabaseManager();
    /**
     * A statement to execute SQL clause. We use this statement in insert clause to improve the performance that prevent creating statement every time executing
     * the insert clause.
     */
    private static Statement stmt;
    /**
     * The database listener, including database connection and changes.
     */
    Vector<DatabaseListener> listeners = new Vector<DatabaseListener>();
    /**
     * A connection between Java and MySQL using Java Database Connectivity.
     */
    private Connection con = null;

    /**
     * Private constructor to make it a single instance.
     */
    private DatabaseManager() {
    }

    /**
     * The DatabaseManager instance.
     *
     * @return An instance of DatabaseManager.
     */
    public static DatabaseManager getInstance() {
        return DATABASE_MANAGER;
    }

    /**
     * Add a database listener to the current listener collection.
     *
     * @param listener The listener to be added.
     */
    public void addDatabaseListener(DatabaseListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a database listener from the current listener collection.
     *
     * @param listener The listener to be removed.
     */
    public void removeDatabaseListener(DatabaseListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    /**
     * Tell all classes which have inherit <code>DatabaseListener</code> that database has been connected.
     */
    public void databaseConnected() {
        Enumeration<DatabaseListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().databaseConnected();
        }
    }

    /**
     * Tell all classes which have inherit <code>DatabaseListener</code> that database or sample has been changed.
     *
     * @param database   The changed database.
     * @param sampleName The changed sample.
     */
    public void databaseChanged(String database, String sampleName) {
        logger.info("Database has been changed to {}", database);
        useDatabase(database);
        Enumeration<DatabaseListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().databaseChanged(database, sampleName);
        }
    }

    /**
     * A method to connect database using Java Database Connectivity.
     *
     * @param host     Host where database is on.
     * @param port     Port that database is used.
     * @param user     The user.
     * @param password The password.
     * @return Whether database has been connected, true if it is successful.
     */
    public boolean connectDatabase(String host, String port, String user, String password) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        String connectionURL = "jdbc:mysql://" + host + ":" + port;
        logger.info("Connecting to MySQL database...");
        con = DriverManager.getConnection(connectionURL, user, password);
        return con != null;
    }

    /**
     * To make it easy use transaction function, we provide this method to set database auto commit or not.
     *
     * @param autoCommit True if auto commit.
     */
    public void setAutoCommit(boolean autoCommit) {
        try {
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            logger.warn("Unable to set database auto commit.", e);
        }
    }

    /**
     * If database has not been set commit automatically, we should commit the transaction individually.
     */
    public void commit() {
        try {
            con.commit();
        } catch (SQLException e) {
            logger.warn("Unable to commit to the database, roll back to the previous status.", e);
        }
    }

    /**
     * Calculate the row count accurately of a given database table. We used 'select count(*) from tableName' before, but it seems that 'select count(1) from
     * tableName' is faster and get the same result.
     *
     * @param tableName The table name to be counted its row line.
     * @return The row count of a given table name.
     */
    public int getRowCount(String tableName) {
        Statement stmt;
        try {
            stmt = con.createStatement();
            ResultSet rs;
            rs = stmt.executeQuery("select count(1) from " + tableName);
            if (rs != null && rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            logger.error("Unable to get the row count.", e);
            return 0;
        }
    }

    /**
     * Create a database. This should be only called before data is being imported to check whether the database has been created. We only create two database:
     * DENOVO_MODE and DNA_RNA_MODE.
     *
     * @param databaseName The database to be created.
     */
    public void createDatabase(String databaseName) {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("create database if not exists " + databaseName);
            stmt.close();
        } catch (SQLException e) {
            logger.error("Unable to create database: create database if not exists {}", databaseName);
        }
    }

    /**
     * Check whether the table exists in the database.
     *
     * @param tableName The table name.
     * @return True if the table exists.
     */
    public boolean existTable(String tableName) {

        List<String> tableLists = getCurrentTables(DatabasePreferences.getInstance().getCurrentDatabase());
        return tableLists.contains(tableName.toLowerCase());
    }

    /**
     * Get all table names from a given database.
     *
     * @param database The database name.
     * @return A list which contains all table names in the database.
     */
    public List<String> getCurrentTables(String database) {
        List<String> tableLists = new ArrayList<String>();
        useDatabase(database);
        DatabaseMetaData databaseMetaData;
        try {
            databaseMetaData = con.getMetaData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(REDApplication.getInstance(), "Statement has not been created. Could not get meta data from database.", "Oops, " +
                    "something wrong...", JOptionPane.ERROR_MESSAGE, IconLoader.ICON_ERROR);
            logger.error("Statement has not been created. Could not get meta data from database.", e);
            return new ArrayList<String>();
        }
        ResultSet rs;
        try {
            rs = databaseMetaData.getTables(database, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                // get table name
                tableLists.add(rs.getString(3));
            }
            return tableLists;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(REDApplication.getInstance(), "Database " + database + " does not exist. Please have a check in your database.", "Oops, " +
                    "something wrong...", JOptionPane.ERROR_MESSAGE, IconLoader.ICON_ERROR);
            logger.error("Database " + database + " does not exist. Please have a check in your database.", e);
            return new ArrayList<String>();
        }
    }

    /**
     * Delete a table if it exists.
     *
     * @param tableName Table name to be deleted.
     */
    public void deleteTable(String tableName) {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("drop table if exists " + tableName);
            stmt.close();
        } catch (SQLException e) {
            logger.error("Error drop table: " + tableName, e);
        }
    }

    /**
     * We provide this method to delete a sample and its relative filtration result from database.
     * <p/>
     * Here is an example: If the sample name is 'BJ22', then we will check all table from this database and find out the table names which starts with 'BJ22_',
     * we add an '_' to prevent from deleting the replicate sample like 'BJ22N', 'BJ22T', 'BJ22P', etc.
     *
     * @param database   Database which is currently used.
     * @param sampleName The sample to be deleted.
     */
    public void deleteTableAndFilters(String database, String sampleName) {
        logger.info("Delete table and filters: {}, {}", database, sampleName);
        try {
            List<String> tableLists = getCurrentTables(database);
            // Prevent from deleting BJ22N sample, but actually we want to delete BJ22 sample.
            Statement stmt = con.createStatement();
            logger.info(tableLists.toString());
            for (String table : tableLists) {
                if (table.startsWith(sampleName + "_")) {
                    logger.info("drop table if exists " + table);
                    stmt.executeUpdate("drop table if exists " + table);
                }
            }
            stmt.close();
        } catch (SQLException e) {
            logger.error("Unable to drop tables and filters for sample " + sampleName, e);
        }
    }

    /**
     * Query all relative tables to the sample from database.
     * <p/>
     * Here is an example: If the sample name is 'BJ22', then we will check all table from this database and find out the table names which starts with 'BJ22_',
     * we add an '_' to prevent from querying the replicate sample like 'BJ22N', 'BJ22T', 'BJ22P', etc.
     *
     * @param sampleName The sample which should be queried.
     * @return A list contains all tables relative to this sample.
     */
    public List<String> queryTablesForSample(String sampleName) {
        List<String> tableLists = getCurrentTables(DatabasePreferences.getInstance().getCurrentDatabase());
        List<String> neededTables = new ArrayList<String>();
        for (String table : tableLists) {
            if (table.contains(sampleName + "_")) {
                neededTables.add(table);
            }
        }
        return neededTables;
    }

    /**
     * Change database.
     *
     * @param databaseName The database to be changed.
     */
    public void useDatabase(String databaseName) {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("use " + databaseName);
            stmt.close();
        } catch (SQLException e) {
            logger.error("Unable to use database " + databaseName, e);
        }
    }

    /**
     * Insert clause
     *
     * @param sql The SQL clause.
     */
    public void insert(String sql) throws SQLException {
        if (stmt == null || stmt.isClosed()) {
            stmt = con.createStatement();
        }
        stmt.executeUpdate(sql);
    }

    /**
     * Provide a common method to execute SQL clause. Some SQL clauses can't use specific methods provided by DatabaseManager table creation.
     *
     * @param sql The SQL clause.
     */
    public void executeSQL(String sql) {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            logger.error("Error execute the SQL clause: " + sql, e);
        }
    }

    /**
     * Provide a method to execute multi-table queries. You should not use this method to do a single table query, and use 'query(String table, String[]
     * columns, String selection, String[] selectionArgs)' instead.
     *
     * @param queryClause The query clause.
     * @return A result set contains all query result.
     */
    public ResultSet query(String queryClause) {
        ResultSet rs = null;
        try {
            Statement stmt = con.createStatement();
            rs = stmt.executeQuery(queryClause);
            stmt.close();
        } catch (SQLException e) {
            logger.error("Error execute the SQL clause: " + queryClause, e);
        }
        return rs;
    }

    /**
     * Query the given table, returning the result set.
     *
     * @param table         The table name to compile the query against.
     * @param columns       A list of which columns to return. Passing null will return all columns, which is discouraged to prevent reading data from storage
     *                      that isn't going to be used.
     * @param selection     A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all
     *                      rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the
     *                      selection. The values will be bound as Strings.
     * @return A ResultSet object, which is positioned before the first entry.
     */
    public ResultSet query(String table, String[] columns, String selection, String[] selectionArgs) {
        ResultSet rs = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select ");
        if (columns == null || columns.length == 0 || columns[0].equals("*")) {
            stringBuilder.append(" * ");
        } else {
            stringBuilder.append(columns[0]);
            for (int i = 1, len = columns.length; i < len; i++) {
                stringBuilder.append(",").append(columns[i]);
            }
        }
        stringBuilder.append(" from ").append(table);
        try {
            if (selection == null || selectionArgs == null || selectionArgs.length == 0) {
                Statement stmt = con.createStatement();
                rs = stmt.executeQuery(stringBuilder.toString());
            } else {
                stringBuilder.append(" WHERE ").append(selection);
                PreparedStatement statement = con.prepareStatement(stringBuilder.toString());
                for (int i = 1, len = selectionArgs.length; i <= len; i++) {
                    statement.setString(i, selectionArgs[i - 1]);
                }
                rs = statement.executeQuery(stringBuilder.toString());
            }
        } catch (SQLException e) {
            logger.error("Error execute SQL clause: " + stringBuilder.toString(), e);
        }
        return rs;
    }

    /**
     * Close database.
     */
    public void closeDatabase() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            logger.error("Unable to close the connection, please have a check.", e);
        }
    }

    /**
     * After filtration, this method will be called to distinct data from all columns.
     *
     * @param resultTable The table to distinct.
     */
    public void distinctTable(String resultTable) {
        String tempTable = RandomStringGenerator.createRandomString(10);
        executeSQL("create temporary table " + tempTable + " select distinct * from " + resultTable);
        executeSQL("truncate table " + resultTable);
        executeSQL("insert into " + resultTable + " select * from " + tempTable);
        deleteTable(tempTable);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeDatabase();
    }
}
