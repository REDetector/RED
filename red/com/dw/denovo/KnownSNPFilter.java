package com.dw.denovo;

/**
 * we will filter out base which already be recognized
 */

import com.dw.dbutils.DatabaseManager;
import com.xl.dialog.ProgressDialog;
import com.xl.dialog.REDProgressBar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KnownSNPFilter {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatabaseManager databaseManager;
    private int count = 0;
    private REDProgressBar progressBar = REDProgressBar.getInstance();

    public KnownSNPFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean hasEstablishDbSNPTable(String dbSnpTable) {
        databaseManager.createRefTable(dbSnpTable,
                "(chrom varchar(15),pos int,index(chrom,pos))");
        ResultSet rs = databaseManager.query(dbSnpTable, "count(*)", "1 limit 0,100");
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
        System.out.println("Start loading dbSNPTable" + " " + df.format(new Date()));
        progressBar.addProgressListener(new ProgressDialog("Import dbsnp data"));
        try {
            progressBar.progressUpdated("Start loading dbSNP data from " + dbSNPPath + " to " + dbSNPTable, 0, 0);
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
                progressBar.progressUpdated("Importing dbSNP data from " + dbSNPPath + " to " + dbSNPTable, 0, 0);
                databaseManager.executeSQL("load data local infile '" + dbSNPPath + "' into table " + dbSNPTable + "" +
                        " fields terminated by '\t' lines terminated by '\n' IGNORE " + count + " LINES");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error load file from " + dbSNPPath + " to file stream");
            e.printStackTrace();
            progressBar.progressWarningReceived(e);
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + KnownSNPFilter.class.getName() + ":loadDbSNPTable()");
            e.printStackTrace();
            progressBar.progressWarningReceived(e);
        }
        progressBar.progressComplete("dbsnp_loaded", null);
        System.out.println("End loading dbSNPTable" + " " + df.format(new Date()));
    }

    public void executeDbSNPFilter(String dbSnpTable, String dbSnpResultTable, String refTable) {
        System.out.println("Start executing KnownSNPFilter..." + df.format(new Date()));
        try {
            databaseManager.executeSQL("insert into " + dbSnpResultTable + " select * from " + refTable + " where " +
                    "not exists (select chrom from " + dbSnpTable + " where (" + dbSnpTable + ".chrom=" + refTable +
                    ".chrom and " + dbSnpTable + ".pos=" + refTable + ".pos))");
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in" + KnownSNPFilter.class.getName() + ":executeDbSNPFilter()");
            e.printStackTrace();
        }
        System.out.println("End executing KnownSNPFilter..." + df.format(new Date()));
    }

}
