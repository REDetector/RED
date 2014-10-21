package com.dw.denovo;

/**
 * Comphrehensive phase we focus on base in exon
 * we discard base in the rear or front of the sequence
 */

import com.dw.publicaffairs.DatabaseManager;
import com.xl.dialog.ProgressDialog;
import com.xl.dialog.REDProgressBar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SpliceJunctionFilter {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatabaseManager databaseManager;
    private REDProgressBar progressBar = REDProgressBar.getInstance();

    public SpliceJunctionFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        progressBar.addProgressListener(new ProgressDialog("Import splice-juntion data"));
    }

    public boolean hasEstablishedSpliceJunctionTable(String spliceJunctionTable) {
        databaseManager.createRefTable(spliceJunctionTable,
                "(chrom varchar(15),ref varchar(30),type varchar(9),begin int,end int,unuse1 float(8,6)," +
                        "unuse2 varchar(5),unuse3 varchar(5),info varchar(100),index(chrom,type))");
        ResultSet rs = databaseManager.query(spliceJunctionTable, "count(*)", "1 limit 0,100");
        int number = 0;
        try {
            if (rs.next()) {
                number = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return number > 0;
    }

    public void establishSpliceJunctionResultTable(String spliceJunctionResultTable) {
        databaseManager.deleteTable(spliceJunctionResultTable);
        databaseManager.createFilterTable(spliceJunctionResultTable);
    }

    public void loadSpliceJunctionTable(String spliceJunctionTable, String spliceJunctionPath) {
        System.out.println("Start loading SpliceJunctionTable..." + df.format(new Date()));
        progressBar.progressUpdated("Start loading splice-junction data from " + spliceJunctionPath + " to " + spliceJunctionTable, 0, 0);
        if (!hasEstablishedSpliceJunctionTable(spliceJunctionTable)) {
            try {
                progressBar.progressUpdated("Importing splice-junction data from " + spliceJunctionPath + " to " + spliceJunctionTable, 0, 0);
                databaseManager.executeSQL("load data local infile '" + spliceJunctionPath + "' into table " + spliceJunctionTable + " fields terminated" +
                        " by '\t' lines terminated by '\n'");
            } catch (SQLException e) {
                System.err.println("Error execute sql clause in " + SpliceJunctionFilter.class.getName() + ":loadSpliceJunctionTable()..");
                e.printStackTrace();
            }
        }

        System.out.println("End loading SpliceJunctionTable..." + df.format(new Date()));
    }

    public void executeSpliceJunctionFilter(String spliceJunctionTable, String spliceJunctionResultTable, String refTable, int edge) {
        System.out.println("Start executing SpliceJunctionFilter..." + df.format(new Date()));
        try {
            databaseManager.executeSQL("insert into " + spliceJunctionResultTable + " select * from " + refTable + " where not exists (select chrom from "
                    + spliceJunctionTable + " where (" + spliceJunctionTable + ".type='CDS' and " + spliceJunctionTable + ".chrom=" + refTable + ".chrom" +
                    " and ((" + spliceJunctionTable + ".begin<" + refTable + ".pos+" + edge + " and " + spliceJunctionTable + ".begin>" + refTable + "" +
                    ".pos-" + edge + ") or (" + spliceJunctionTable + ".end<" + refTable + ".pos+" + edge + " and " + spliceJunctionTable + ".end>"
                    + refTable + ".pos-" + edge + "))))");
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in" + SpliceJunctionFilter.class.getName() + ":executeSpliceJunctionFilter()");
            e.printStackTrace();
        }
        System.out.println("End executing SpliceJunctionFilter..." + df.format(new Date()));
    }

}
