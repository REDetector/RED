package com.dw.publicaffairs;

/**
 * Linked to target database
 */

import com.xl.exception.REDException;
import com.xl.preferences.REDPreferences;

import java.sql.*;

public class DatabaseManager {
    private static final DatabaseManager DATABASE_MANAGER = new DatabaseManager();
    public static final int COMMIT_COUNTS_PER_ONCE = 10000;
    public static final String NON_DENOVO_DATABASE_NAME = "nondenovo";
    public static final String DENOVO_DATABASE_NAME = "denovo";
    public static final String RNA_VCF_RESULT_TABLE_NAME = "rnavcf";
    public static final String DNA_VCF_RESULT_TABLE_NAME = "dnavcf";
    public static final String BASIC_FILTER_RESULT_TABLE_NAME = "basicfilter";
    public static final String SPECIFIC_FILTER_RESULT_TABLE_NAME = "specificfilter";
    public static final String SPLICE_JUNCTION_FILTER_TABLE_NAME = "refsplicejunction";
    public static final String SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME = "splicejunctionfilter";
    public static final String DBSNP_FILTER_TABLE_NAME = "refdbsnp";
    public static final String DBSNP_FILTER_RESULT_TABLE_NAME = "dbsnpfilter";
    public static final String PVALUE_FILTER_TABLE_NAME = "refpvalue";
    public static final String PVALUE_FILTER_RESULT_TABLE_NAME = "pvaluefilter";
    public static final String REPEAT_FILTER_TABLE_NAME = "refrepeat";
    public static final String REPEAT_FILTER_RESULT_TABLE_NAME = "repeatfilter";
    public static final String DNA_RNA_FILTER_RESULT_TABLE_NAME = "dnarnafilter";
    public static final String LLR_FILTER_RESULT_TABLE_NAME = "llrfilter";
    public static final String ALU_FILTER_RESULT_TABLE_NAME = "alufilter";

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
        stmt = con.createStatement();
        return con != null && stmt != null;
    }

    public void setAutoCommit(boolean autoCommit) {
        try {
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            System.err.println("Error set auto commit");
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

    public void createFilterTable(String tableName) {
        if (REDPreferences.getInstance().getDatabaseTableBuilder() == null) {
            try {
                throw new REDException("RNA/DNA vcf file has not been imported.");
            } catch (REDException e) {
                e.printStackTrace();
            }
        }
        try {
            stmt.executeUpdate("create table " + tableName + "(" + REDPreferences.getInstance().getDatabaseTableBuilder() + ")");
        } catch (SQLException e) {
            System.err.println("Error create table '" + tableName + "'");
            e.printStackTrace();
        }
    }

    public void createPValueTable(String darnedResultTable) {
        try {
            stmt.executeUpdate("create table " + darnedResultTable + "(chrom varchar(15),pos int,ref smallint," +
                    "alt smallint,level varchar(10),p_value double,fdr double)");
        } catch (SQLException e) {
            System.err.println("Error create table '" + darnedResultTable + "'");
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeDatabase();
    }
}
