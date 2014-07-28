package com.dw.dnarna;

/**
 * Detect SNP in DNA level
 */

import com.dw.publicaffairs.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DnaRnaFilter {
    private DatabaseManager databaseManager;

    private String chr = null;
    private String ps = null;
    private String chrom = null;
    private int count = 0;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DnaRnaFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void establishDnaRnaTable(String dnaRnaTable) {
        System.out.println("esdr start" + " " + df.format(new Date()));

        databaseManager.deleteTable(dnaRnaTable);
        databaseManager.createFilterTable(dnaRnaTable);

        System.out.println("esdr end" + " " + df.format(new Date()));
    }

    public void executeDnaRnaFilter(String dnaRnaResultTable, String dnaVcfTable, String refTable) {
        try {
            System.out.println("df start" + " " + df.format(new Date()));

            ResultSet rs = databaseManager.query(dnaVcfTable, "chrom",
                    "1 limit 0,1");
            List<String> coordinate = new ArrayList<String>();
            databaseManager.setAutoCommit(false);

            // whether it is
            boolean bool = false;
            if (rs.next() && rs.getString(1).length() < 3) {
                bool = true;
            }

            rs = databaseManager.query(refTable, "chrom,pos", "1");
            while (rs.next()) {
                if (bool) {
                    chrom = rs.getString(1).replace("chr", "");
                    coordinate.add(chrom);
                    coordinate.add(rs.getString(2));
                } else {
                    coordinate.add(rs.getString(1));
                    coordinate.add(rs.getString(2));
                }
            }

            for (int i = 0, len = coordinate.size(); i < len; i++) {
                if (i % 2 == 0) {
                    chr = coordinate.get(i);
                } else {
                    ps = coordinate.get(i);
                    // The first six base will be filtered out
                    rs = databaseManager.query(dnaVcfTable, "GT", "chrom='" + chr
                            + "' and pos=" + ps + "");
                    while (rs.next()) {
                        if (bool) {
                            chr = "chr" + chr;
                            databaseManager.executeSQL("insert into "
                                    + dnaRnaResultTable + " select * from "
                                    + refTable + " where chrom='" + chr
                                    + "' and pos=" + ps + "");
                            count++;
                            if (count % 10000 == 0)
                                databaseManager.commit();
                        } else {
                            databaseManager.executeSQL("insert into "
                                    + dnaRnaResultTable + " select * from "
                                    + refTable + " where chrom='" + chr
                                    + "' and pos=" + ps + "");
                            count++;
                            if (count % 10000 == 0)
                                databaseManager.commit();
                        }
                    }
                    databaseManager.commit();
                }
            }
            databaseManager.setAutoCommit(true);

            System.out.println("df end" + " " + df.format(new Date()));

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            System.err.println("Error execute sql clause in " + DnaRnaFilter.class.getName() + ":loadRnaVcfTable()");
            e.printStackTrace();
        }
    }

}
