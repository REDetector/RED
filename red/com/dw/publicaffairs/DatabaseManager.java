package com.dw.publicaffairs;

/**
 * Linked to target database
 */

import com.xl.preferences.REDPreferences;

import java.sql.*;

public class DatabaseManager {
    private static final DatabaseManager DATABASE_MANAGER = new DatabaseManager();
    public static final String NON_DENOVO_DATABASE_NAME = "nondenovo";
    public static final String DENOVO_DATABASE_NAME = "denovo";
    public static final String RNA_VCF_RESULT_TABLE_NAME = "rnavcf";
    public static final String DNA_VCF_RESULT_TABLE_NAME = "dnavcf";
    public static final String BASIC_FILTER_RESULT_TABLE_NAME = "basicfilter";
    public static final String SPECIFIC_FILTER_RESULT_TABLE_NAME = "specificfilter";
    public static final String COMPREHENSIVE_FILTER_TABLE_NAME = "refcomprehensive";
    public static final String COMPREHENSIVE_FILTER_RESULT_TABLE_NAME = "comprehensivefilter";
    public static final String DBSNP_FILTER_TABLE_NAME = "refdbsnp";
    public static final String DBSNP_FILTER_RESULT_TABLE_NAME = "dbsnpfilter";
    public static final String PVALUE_FILTER_TABLE_NAME = "refpvalue";
    public static final String PVALUE_FILTER_RESULT_TABLE_NAME = "pvaluefilter";
    public static final String REPEAT_FILTER_TABLE_NAME = "refrepeat";
    public static final String REPEAT_FILTER_RESULT_TABLE_NAME = "repeatfilter";
    public static final String DNA_RNA_FILTER_RESULT_TABLE_NAME = "dnarnafilter";
    public static final String LLR_FILTER_RESULT_TABLE_NAME = "llrfilter";

    private Connection con = null;
    private Statement stmt = null;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        return DATABASE_MANAGER;
    }

    public boolean connectDatabase(String host, String port, String user,
                                   String password) throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.jdbc.Driver");
        String connectionURL = "jdbc:mysql://" + host + ":" + port + "";
        con = DriverManager.getConnection(connectionURL, user, password);
        return con != null;
    }

    public boolean connectDatabase() throws ClassNotFoundException, SQLException {
        REDPreferences preferences = REDPreferences.getInstance();
        String host = preferences.getDatabaseHost();
        String port = preferences.getDatabasePort();
        String user = preferences.getDatabaseUser();
        String password = preferences.getDatabasePassword();
        Class.forName("com.mysql.jdbc.Driver");
        String connectionURL = "jdbc:mysql://" + host + ":" + port + "";
        con = DriverManager.getConnection(connectionURL, user, password);
        return con != null;
    }

    public void setAutoCommit(boolean autoCommit) {
        try {
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            System.err.println("Error set auto commit");
            e.printStackTrace();
        }
    }

    public void createStatement() {
        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            System.err.println("Error create createStatement");
            e.printStackTrace();
        }
    }

    public void commit() {
        try {
            con.commit();
        } catch (SQLException e) {
            System.err.println("Error commit to database");
            e.printStackTrace();
        }
    }

    public void createDatabase(String databaseName) {
        try {
            stmt.executeUpdate("create database if not exists " + databaseName);
        } catch (SQLException e) {
            System.err.println("Error create database '" + databaseName + "'");
            e.printStackTrace();
        }
    }

    public void createRefTable(String tableName, String tableParam) {
        try {
            stmt.executeUpdate("create table if not exists " + tableName + tableParam);
        } catch (SQLException e) {
            System.err.println("Error create table if not exists '" + tableName + "'");
            e.printStackTrace();
        }
    }

    public void createTable(String tableName, String tableParam) {
        try {
            stmt.executeUpdate("create table " + tableName + tableParam);
        } catch (SQLException e) {
            System.err.println("Error create table '" + tableName + "'");
            e.printStackTrace();
        }
    }

    public void deleteTable(String tableName) {
        try {
            stmt.executeUpdate("drop table if exists " + tableName);
        } catch (SQLException e) {
            System.err.println("Error delete table '" + tableName + "'");
            e.printStackTrace();
        }
    }

    public boolean useDatabase(String databaseName) {
        try {
            stmt.executeUpdate("use " + databaseName);
            return true;
        } catch (SQLException e) {
            System.err.println("Error use database '" + databaseName + "'");
            e.printStackTrace();
            return false;
        }
    }

    public void insertClause(String insertCommand) {
        try {
            stmt.executeUpdate(insertCommand);
        } catch (SQLException e) {
            System.err.println("Error insert '" + insertCommand + "'");
            e.printStackTrace();
        }
    }

    public void executeSQL(String sql) throws SQLException {
        stmt.executeUpdate(sql);
    }

    public ResultSet query(String table, String columns, String whereArgs) {
        try {
            return stmt.executeQuery("select " + columns + " from " + table
                    + " where " + whereArgs);
        } catch (SQLException e) {
            System.err.println("Error execute query clause: '" + "select "
                    + columns + " from " + table + "'");
            e.printStackTrace();
            return null;
        }
    }

    public void closeDatabase() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            System.err.println("Error close database!");
            e.printStackTrace();
        }
    }

    public void distinctTable(String resultTable) {
        try {
            executeSQL("create temporary table newtable select distinct * from "
                    + resultTable);
            executeSQL("truncate table " + resultTable);
            executeSQL("insert into " + resultTable +
                    " select * from  newtable");
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + DatabaseManager.class.getName() + ":distinctTable()");
            e.printStackTrace();
        }
        deleteTable("newTable");
    }
}
