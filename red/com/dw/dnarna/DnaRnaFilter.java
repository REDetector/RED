package com.dw.dnarna;

/**
 * Detect SNP in DNA level
 */

import com.dw.publicaffairs.DatabaseManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DnaRnaFilter {
    private DatabaseManager databaseManager;

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DnaRnaFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void establishDnaRnaTable(String dnaRnaTable) {
        databaseManager.deleteTable(dnaRnaTable);
        databaseManager.createFilterTable(dnaRnaTable);
    }

    public void executeDnaRnaFilter(String dnaRnaResultTable, String dnaVcfTable, String refTable) {
        System.out.println("Start executing DnaRnaFilter..." + df.format(new Date()));

        try {
            databaseManager.executeSQL("insert into " + dnaRnaResultTable + " select * from " + refTable + " where " +
                    "exists (select chrom from " + dnaVcfTable + " where (" + dnaVcfTable + ".chrom=" + refTable +
                    ".chrom and " + dnaVcfTable + ".pos=" + refTable + ".pos))");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            System.err.println("Error execute sql clause in " + DnaRnaFilter.class.getName() + ":executeDnaRnaFilter()");
            e.printStackTrace();
        }
        System.out.println("End executing DnaRnaFilter..." + df.format(new Date()));
    }

}
