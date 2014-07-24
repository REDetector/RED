package com.dw.dnarna;

/**
 * LLR used for detecting editing sites
 */

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LLRFilter {
    private DatabaseManager databaseManager;

    private String chr;
    private String ps;
    private String chrom = null;
    private int count = 0;
    private int ref_n = 0;
    private int alt_n = 0;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LLRFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void establishLLRResultTable(String llrResultTable) {
        System.out.println("esllr start" + " " + df.format(new Date()));

        databaseManager.deleteTable(llrResultTable);
        databaseManager.createTable(llrResultTable, "(chrome varchar(15),"
                + Utilities.getInstance().getS2() + ")");

        System.out.println("esllr end" + " " + df.format(new Date()));
    }

    public void executeLLRFilter(String llrResultTable, String dnaVcfTable, String refTable) {
        try {
            System.out.println("executeLLRFilter start" + " " + df.format(new Date()));

            ResultSet rs = databaseManager.query(dnaVcfTable, "chrome",
                    "1 limit 0,1");
            List<String> coordinate = new ArrayList<String>();
            databaseManager.setAutoCommit(false);

            // whether it is
            boolean bool = false;
            if (rs.next() && rs.getString(1).length() < 3) {
                bool = true;
            }

            rs = databaseManager.query(refTable, "chrome,pos,AD", "1");
            while (rs.next()) {
                if (bool) {
                    chrom = rs.getString(1).replace("chr", "");
                    coordinate.add(chrom);
                    coordinate.add(rs.getString(2));
                    coordinate.add(rs.getString(3));
                } else {
                    coordinate.add(rs.getString(1));
                    coordinate.add(rs.getString(2));
                    coordinate.add(rs.getString(3));
                }
            }

            for (int i = 0, len = coordinate.size(); i < len; i++) {
                switch (i % 3) {
                    case 0:
                        chr = coordinate.get(i);
                        break;
                    case 1:
                        ps = coordinate.get(i);
                        break;
                    case 2:
                        String[] section = coordinate.get(i).split(";");
                        ref_n = Integer.parseInt(section[0]);
                        alt_n = Integer.parseInt(section[1]);

                        double q = 0;
                        rs = databaseManager.query(dnaVcfTable, "qual", "chrome='" + chr
                                + "' and pos=" + ps + "");
                        while (rs.next()) {
                            q = rs.getDouble(1);
                        }
                        if (alt_n + ref_n > 0) {

                            double f_ml = 1.0 * ref_n / (ref_n + alt_n);
                            double y = Math.pow(f_ml, ref_n)
                                    * Math.pow(1 - f_ml, alt_n);
                            y = Math.log(y) / Math.log(10.0);
                            double judge = 0.0;
                            judge = y + q / 10.0;
                            // System.out.println(judge);
                            // System.out.println(ref_n + " " + alt_n + " " + y +
                            // " "
                            // + judge);
                            if (judge >= 4) {
                                if (bool) {
                                    chr = "chr" + chr;
                                    databaseManager.executeSQL("insert into "
                                            + llrResultTable + " select * from "
                                            + refTable + " where chrome='" + chr
                                            + "' and pos=" + ps + "");
                                    count++;
                                    if (count % 10000 == 0) {
                                        databaseManager.commit();
                                    }
                                } else {
                                    databaseManager.executeSQL("insert into "
                                            + llrResultTable + " select * from "
                                            + refTable + " where chrome='" + chr
                                            + "' and pos=" + ps + "");
                                    count++;
                                    if (count % 10000 == 0) {
                                        databaseManager.commit();
                                    }
                                }
                            }
                        }
                        break;
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
            System.out.println("executeLLRFilter end" + " " + df.format(new Date()));

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // public void post() {
    // try {
    // System.out.println("post start"+" "+df.format(new Date()));// new
    // db.usedb();
    // sql[0] =
    // "create   temporary   table  newtable  select   distinct   *   from  executeLLRFilter";
    // sql[1] = "truncate   table  executeLLRFilter";
    // sql[2] = "insert   into   executeLLRFilter select   *   from  newtable";
    // db.result = db.stmt.executeUpdate(sql[0]);
    // db.result = db.stmt.executeUpdate(sql[1]);
    // db.result = db.stmt.executeUpdate(sql[2]);
    // db.con.commit();
    // sql[0] = "drop   table newtable";
    // db.result = db.stmt.executeUpdate(sql[0]);
    // db.con.commit();
    // System.out.println("post end"+" "+df.format(new Date()));// new
    // } catch (SQLException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
}
