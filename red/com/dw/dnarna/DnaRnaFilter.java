package com.dw.dnarna;

/**
 * Detect SNP in DNA level
 */

import com.dw.dbutils.DatabaseManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DNARNAFilter {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatabaseManager databaseManager;

    public DNARNAFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void establishDnaRnaTable(String dnaRnaTable) {
        databaseManager.deleteTable(dnaRnaTable);
        databaseManager.createFilterTable(dnaRnaTable);
    }

    public void executeDnaRnaFilter(String dnaRnaResultTable, String dnaVcfTable, String refTable) {
        System.out.println("Start executing DNARNAFilter..." + df.format(new Date()));

        try {
            databaseManager.executeSQL("insert into " + dnaRnaResultTable + " select * from " + refTable + " where " +
                    "exists (select chrom from " + dnaVcfTable + " where (" + dnaVcfTable + ".chrom=" + refTable +
                    ".chrom and " + dnaVcfTable + ".pos=" + refTable + ".pos))");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            System.err.println("Error execute sql clause in " + DNARNAFilter.class.getName() + ":executeDnaRnaFilter()");
            e.printStackTrace();
        }
        System.out.println("End executing DNARNAFilter..." + df.format(new Date()));
    }

}
