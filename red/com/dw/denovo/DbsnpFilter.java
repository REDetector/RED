package com.dw.denovo;

/**
 * we will filter out base which already be recognized
 */

import com.dw.publicaffairs.DatabaseManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DbsnpFilter {
    private DatabaseManager databaseManager;

    private int count = 0;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DbsnpFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean hasEstablishDbSNPTable(String dbSnpTable) {
        databaseManager.createRefTable(dbSnpTable,
                "(chrom varchar(15),pos int,index(chrom,pos))");
        ResultSet rs = databaseManager.query(dbSnpTable, "count(*)",
                "1 limit 0,100");
        int number = 0;
        try {
            if (rs.next()) {
                number = rs.getInt(1);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return number > 0;
    }

    public boolean establishDbSNPResultTable(String dbSnpResultTable) {
        databaseManager.deleteTable(dbSnpResultTable);
        databaseManager.createFilterTable(dbSnpResultTable);
        return true;
    }

    public void loadDbSNPTable(String dbSNPTable, String dbSNPPath) {
        try {
            System.out.println("Start loading dbSNPTable" + " " + df.format(new Date()));

            if (!hasEstablishDbSNPTable(dbSNPTable)) {
                FileInputStream inputStream = new FileInputStream(dbSNPPath);
                BufferedReader rin = new BufferedReader(new InputStreamReader(
                        inputStream));
                String line;
                while ((line = rin.readLine()) != null) {
                    if (line.startsWith("#")) {
                        count++;
                    } else {
                        break;
                    }
                }
                rin.close();
                databaseManager.executeSQL("load data local infile '" + dbSNPPath + "' into table " + dbSNPTable + "" +
                        " fields terminated by '\t' lines terminated by '\n' IGNORE " + count + " LINES");
            }

            System.out.println("End loading dbSNPTable" + " " + df.format(new Date()));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error load file from " + dbSNPPath + " to file stream");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + DbsnpFilter.class.getName() + ":loadDbSNPTable()");
            e.printStackTrace();
        }
    }

    public void executeDbSNPFilter(String dbSnpTable, String dbSnpResultTable, String refTable) {
        System.out.println("Start executing DbSNPFilter..." + df.format(new Date()));
        try {
            databaseManager.executeSQL("insert into " + dbSnpResultTable + " select * from " + refTable + " where " +
                    "not exists (select chrom from " + dbSnpTable + " where (" + dbSnpTable + ".chrom=" + refTable +
                    ".chrom and " + dbSnpTable + ".pos=" + refTable + ".pos))");
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in" + DbsnpFilter.class.getName() + ":executeDbSNPFilter()");
            e.printStackTrace();
        }
        System.out.println("End executing DbSNPFilter..." + df.format(new Date()));
    }

}
