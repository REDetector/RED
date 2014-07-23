package com.dw.denovo;

/**
 * Comphrehensive phase
 * we focus on base in exon
 * we discard base in the rear or front of the sequence
 */

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComphrehensiveFilter {
    private DatabaseManager databaseManager;

    // FileInputStream inputStream;
    private String refSeqPath = null;
    private String refSeqResultTable = null;
    private String refSeqTable = null;
    private String refTable = null;

    private int count = 0;
    private String chr = null;
    private String ps = null;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ComphrehensiveFilter(DatabaseManager databaseManager, String refSeqPath,
                                String refSeqResultTable, String refSeqTable,
                                String refTable) {
        this.databaseManager = databaseManager;
        this.refSeqPath = refSeqPath;
        this.refSeqResultTable = refSeqResultTable;
        this.refSeqTable = refSeqTable;
        this.refTable = refTable;
    }

    public boolean establishCom() {
        System.out.println("escom start" + " " + df.format(new Date()));

        databaseManager.deleteTable(refSeqResultTable);
        databaseManager.createTable(refSeqResultTable, "(chrome varchar(15),"
                + Utilities.getInstance().getS2() + "," + "index(chrome,pos))");

        System.out.println("escom end" + " " + df.format(new Date()));
        return true;
    }

    public boolean establishRefCom() {
        databaseManager
                .createTable(
                        refSeqTable,
                        "(chrome varchar(15),ref varchar(30),type varchar(9),begin int,end int,unuse1 float(8,6),unuse2 varchar(5),unuse3 varchar(5),info varchar(100),index(chrome,type))");
        ResultSet rs = databaseManager.query(refSeqTable,
                "count(*)", "1 limit 0,100");
        int number = 0;
        try {
            if (rs.next()) {
                number = rs.getInt(1);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (number > 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean loadcom() {
        System.out.println("loadcom start" + " " + df.format(new Date()));

        if (establishRefCom()) {
            databaseManager.executeSQL("load data local infile '" + refSeqPath
                    + "' into table " + refSeqTable
                    + " fields terminated by '\t' lines terminated by '\n'");
        }

        System.out.println("loadcom end" + " " + df.format(new Date()));

        return true;

    }

    public boolean comphrehensiveF(int edge) {
        try {
            System.out.println("comf start" + " " + df.format(new Date()));

            ResultSet rs = databaseManager.query(refTable, "chrome,pos", "1");

            List<String> coordinate = new ArrayList<String>();
            databaseManager.setAutoCommit(false);

            while (rs.next()) {
                coordinate.add(rs.getString(1));
                coordinate.add(rs.getString(2));
            }
            for (int i = 0, len = coordinate.size(); i < len; i++) {
                switch (i % 2) {
                    case 0:
                        chr = coordinate.get(i);
                        break;
                    case 1:
                        ps = coordinate.get(i);
                        rs = databaseManager.query(refSeqTable, "type",
                                "( ((begin<" + ps + "+" + edge + " " + "and begin>"
                                        + ps + "-" + edge + ") or " + "(end<" + ps
                                        + "+" + edge + " and end>" + ps + "-"
                                        + edge + ")) "
                                        + "and type='CDS' and chrome='" + chr
                                        + "')");
                        if (!rs.next()) {
                            databaseManager.executeSQL("insert into "
                                    + refSeqResultTable + "  select * from "
                                    + refTable + " where chrome='" + chr
                                    + "' and pos=" + ps + "");
                            count++;
                            if (count % 10000 == 0)
                                databaseManager.commit();
                        }
                        break;
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);

            System.out.println("comf end" + " " + df.format(new Date()));

            return true;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

    public void distinctTable() {
        System.out.println("post start" + " " + df.format(new Date()));

        databaseManager.executeSQL("create temporary table newtable select distinct * from "
                + refSeqResultTable);
        databaseManager.executeSQL("truncate table " + refSeqResultTable);
        databaseManager.executeSQL("insert into " + refSeqResultTable +
                " select * from  newtable");
        databaseManager.deleteTable("newTable");

        System.out.println("post end" + " " + df.format(new Date()));
    }

}
