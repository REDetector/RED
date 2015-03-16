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

import com.xl.preferences.DatabasePreferences;
import com.xl.utils.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void databaseChanged(String database, String sampleName) throws SQLException {
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
     * @throws ClassNotFoundException If the JDBC driver had not been found before connecting, ClassNotFoundException will be thrown.
     * @throws SQLException           If there are any wrong inputs among host, port, user and password, the SQLException will be thrown.
     */
    public boolean connectDatabase(String host, String port, String user, String password) throws ClassNotFoundException, SQLException {
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
    public void commit() throws SQLException {
        con.commit();
    }

    /**
     * Calculate the row count accurately of a given database table. We used 'select count(*) from tableName' before, but it seems that 'select count(1) from
     * tableName' is faster and get the same result.
     *
     * @param tableName The table name to be counted its row line.
     * @return The row count of a given table name.
     */
    public int getRowCount(String tableName) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs;
        rs = stmt.executeQuery("select count(1) from " + tableName);
        if (rs != null && rs.next()) {
            return rs.getInt(1);
        } else {
            return 0;
        }
    }

    /**
     * Create a database. This should be only called before data is being imported to check whether the database has been created. We only create two database:
     * DENOVO_MODE and DNA_RNA_MODE.
     *
     * @param databaseName The database to be created.
     */
    public void createDatabase(String databaseName) throws SQLException {
        con.createStatement().executeUpdate("create database if not exists " + databaseName);
    }

    /**
     * Check whether the table exists in the database.
     *
     * @param tableName The table name.
     * @return True if the table exists.
     */
    public boolean existTable(String tableName) throws SQLException {
        List<String> tableLists = getCurrentTables(DatabasePreferences.getInstance().getCurrentDatabase());
        return tableLists.contains(tableName);
    }

    /**
     * Get all table names from a given database.
     *
     * @param database The database name.
     * @return A list which contains all table names in the database.
     */
    public List<String> getCurrentTables(String database) throws SQLException {
        List<String> tableLists = new ArrayList<String>();
        useDatabase(database);
        DatabaseMetaData databaseMetaData;
        databaseMetaData = con.getMetaData();
        ResultSet rs;
        rs = databaseMetaData.getTables(database, null, null, new String[]{"TABLE"});
        while (rs.next()) {
            // get table name
            tableLists.add(rs.getString(3));
        }
        return tableLists;
    }

    /**
     * Delete a table if it exists.
     *
     * @param tableName Table name to be deleted.
     */
    public void deleteTable(String tableName) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate("drop table if exists " + tableName);
        stmt.close();
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
    public void deleteTableAndFilters(String database, String sampleName) throws SQLException {
        List<String> tableLists = getCurrentTables(database);
        // Prevent from deleting BJ22N sample, but actually we want to delete BJ22 sample.
        Statement stmt = con.createStatement();
        for (String table : tableLists) {
            if (table.startsWith(sampleName + "_")) {
                stmt.executeUpdate("drop table if exists " + table);
            }
        }
        stmt.close();
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
    public List<String> queryTablesForSample(String sampleName) throws SQLException {
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
    public void useDatabase(String databaseName) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate("use " + databaseName);
        stmt.close();
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
     * @throws SQLException If the SQL clause is wrong then throw this exception.
     */
    public void executeSQL(String sql) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    /**
     * Provide a method to execute multi-table queries. You should not use this method to do a single table query, and use 'query(String table, String[]
     * columns, String selection, String[] selectionArgs)' instead.
     *
     * @param queryClause The query clause.
     * @return A result set contains all query result.
     * @throws SQLException If there is any SQL syntax error, then throw this exception.
     */
    public ResultSet query(String queryClause) throws SQLException {
        ResultSet rs;
        Statement stmt = con.createStatement();
        rs = stmt.executeQuery(queryClause);
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
    public ResultSet query(String table, String[] columns, String selection, String[] selectionArgs) throws SQLException {
        ResultSet rs;
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
        return rs;
    }

    /**
     * Close database.
     */
    public void closeDatabase() throws SQLException {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            logger.error("Unable to close the connection, please have a check.", e);
            throw new SQLException(e);
        }
    }

    /**
     * After filtration, this method will be called to distinct data from all columns.
     *
     * @param resultTable The table to distinct.
     */
    public void distinctTable(String resultTable) throws SQLException {
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
