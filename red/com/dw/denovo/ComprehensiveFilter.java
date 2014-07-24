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

public class ComprehensiveFilter {
    private DatabaseManager databaseManager;

    private int count = 0;
    private String chr = null;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ComprehensiveFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean hasEstablishedComprehensiveTable(String comprehensiveTable) {
        databaseManager.createTable(comprehensiveTable,
                "(chrome varchar(15),ref varchar(30),type varchar(9),begin int,end int,unuse1 float(8,6),unuse2 varchar(5),unuse3 varchar(5),info varchar(100),index(chrome,type))");
        ResultSet rs = databaseManager.query(comprehensiveTable,
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
        return number > 0;
    }

    public boolean establishComprehensiveResultTable(String comprehensiveResultTable) {
        System.out.println("escom start" + " " + df.format(new Date()));

        databaseManager.deleteTable(comprehensiveResultTable);
        databaseManager.createTable(comprehensiveResultTable, "(chrome varchar(15),"
                + Utilities.getInstance().getS2() + "," + "index(chrome,pos))");

        System.out.println("escom end" + " " + df.format(new Date()));
        return true;
    }


    public boolean loadComprehensiveTable(String comprehensiveTable, String comprehensivePath) {
        System.out.println("loadComprehensiveTable start" + " " + df.format(new Date()));

        if (!hasEstablishedComprehensiveTable(comprehensiveTable)) {
            try {
                databaseManager.executeSQL("load data local infile '" + comprehensivePath
                        + "' into table " + comprehensiveTable
                        + " fields terminated by '\t' lines terminated by '\n'");
            } catch (SQLException e) {
                System.err.println("Error execute sql clause in " + ComprehensiveFilter.class.getName() + ":loadComprehensiveTable()");
                e.printStackTrace();
            }
        }

        System.out.println("loadComprehensiveTable end" + " " + df.format(new Date()));
        return true;
    }

    public boolean executeComprehensiveFilter(String comprehensiveTable, String comprehensiveResultTable, String refTable,
                                              int edge) {
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
                        String ps = coordinate.get(i);
                        rs = databaseManager.query(comprehensiveTable, "type",
                                "( ((begin<" + ps + "+" + edge + " " + "and begin>"
                                        + ps + "-" + edge + ") or " + "(end<" + ps
                                        + "+" + edge + " and end>" + ps + "-"
                                        + edge + ")) "
                                        + "and type='CDS' and chrome='" + chr
                                        + "')");
                        if (!rs.next()) {
                            databaseManager.executeSQL("insert into "
                                    + comprehensiveResultTable + "  select * from "
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

}
