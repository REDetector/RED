package com.dw.publicaffairs;

/**
 * Linked to target database
 */

import com.xl.exception.REDException;
import com.xl.preferences.REDPreferences;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    public static final String ALU_FILTER_RESULT_TABLE_NAME = "alufilter";

    private Connection con = null;
    private Statement stmt = null;

    private StringBuilder tableBuilder = null;
    private List<String> columnInfo = null;

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

    public StringBuilder getTableBuilder() {
        return tableBuilder;
    }

    public StringBuilder getColumnInfo() {
        if (columnInfo == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String column : columnInfo) {
            stringBuilder.append(column + ",");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder;
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

    public void createFilterTable(String tableName) {
        if (tableBuilder == null) {
            try {
                throw new REDException("RNA/DNA vcf file has not been imported.");
            } catch (REDException e) {
                e.printStackTrace();
            }
        }
        try {
            stmt.executeUpdate("create table " + tableName + "(" + tableBuilder.toString() + ")");
        } catch (SQLException e) {
            System.err.println("Error create table if not exists '" + tableName + "'");
            e.printStackTrace();
        }
    }

    public void createVCFTable(String tableName, String path) {
        BufferedReader rin;
        tableBuilder = new StringBuilder();
        columnInfo = new ArrayList<String>();
        try {
            InputStream inputStream = new FileInputStream(path);
            rin = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = rin.readLine()) != null) {
                String[] section = line.split("\\t");
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    tableBuilder.append(section[0].substring(1) + " " + " varchar(15)");
                    tableBuilder.append("," + section[1] + " " + "int");
                    tableBuilder.append("," + section[2] + " " + "varchar(30)");
                    tableBuilder.append("," + section[3] + " " + "varchar(3)");
                    tableBuilder.append("," + section[4] + " " + "varchar(5)");
                    tableBuilder.append("," + section[5] + " " + "float(8,2)");
                    tableBuilder.append("," + section[6] + " " + "text");
                    tableBuilder.append("," + section[7] + " " + "text");
                    columnInfo.add(section[0].substring(1));
                    for (int i = 1; i < 8; i++)
                        columnInfo.add(section[i]);
                    continue;
                }
                String[] column8 = section[8].split(":");
                for (int i = 0, len = column8.length; i < len; i++) {
                    tableBuilder.append("," + column8[i] + " " + "text");
                    columnInfo.add(column8[i]);
                }
                tableBuilder.append(",index(chrom,pos)");
                break;
            }
            stmt.executeUpdate("create table " + tableName + "(" + tableBuilder.toString() + ")");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e1) {
            System.err.println("Error create table '" + tableName + "'");
            e1.printStackTrace();
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
