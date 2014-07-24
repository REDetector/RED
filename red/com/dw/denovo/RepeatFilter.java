package com.dw.denovo;

/**
 * we will filter out base in repeated area except for SINE/alu
 */

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RepeatFilter {
    private DatabaseManager databaseManager;

    FileInputStream inputStream;
    private String line = null;
    private int count = 3;
    private int index = 0;
    private String chr = null;
    private String ps = null;

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RepeatFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean hasEstablishedRepeatTable(String repeatTable) {
        databaseManager
                .createRefTable(repeatTable,
                        "(chrome varchar(15),begin int,end int,type varchar(40),index(chrome,begin,end))");
        ResultSet rs = databaseManager.query(repeatTable, "count(*)",
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

    public void loadRepeatTable(String repeatTable, String repeatPath) {
        try {
            System.out.println("loadRepeatTable start" + " " + df.format(new Date()));
            if (!hasEstablishedRepeatTable(repeatTable)) {
                int ts_count = 0;
                try {
                    inputStream = new FileInputStream(repeatPath);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                databaseManager.setAutoCommit(false);
                BufferedReader rin = new BufferedReader(new InputStreamReader(
                        inputStream));
                while ((line = rin.readLine()) != null) {
                    // clear head of fa.out
                    line = line.replaceAll("['   ']+", "\t");
                    if (count > 0) {
                        count--;
                        continue;
                    }
                    if (!line.startsWith("\t"))
                        index--;
                    if (line.split("\t")[index + 5].length() > 6) {
                        index = 0;
                        continue;
                    }
                    String section[] = line.split("\t");
                    databaseManager.executeSQL("insert into " + repeatTable
                            + "(chrome,begin,end,type) values('"
                            + section[index + 5] + "','"
                            + section[index + 6] + "','"
                            + section[index + 7] + "','"
                            + section[index + 11] + "')");
                    ts_count++;
                    if (ts_count % 30000 == 0)
                        databaseManager.commit();
                    index = 0;
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            }
            System.out.println("loadRepeatTable end" + " " + df.format(new Date()));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error load file from " + repeatPath + " to file stream");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + RepeatFilter.class.getName() + ":loadComprehensiveTable()");
            e.printStackTrace();
        }
    }

    public void establishRepeatResultTable(String repeatResultTable) {
        System.out.println("esrepeat start" + " " + df.format(new Date()));

        databaseManager.deleteTable(repeatResultTable);
        databaseManager.createTable(repeatResultTable, "(chrome varchar(15),"
                + Utilities.getInstance().getS2() + "," + "index(chrome,pos))");

        databaseManager.deleteTable("alutemp");
        databaseManager.createTable("alutemp", "(chrome varchar(15),"
                + Utilities.getInstance().getS2() + "," + "index(chrome,pos))");

        System.out.println("esrepeat end" + " " + df.format(new Date()));
    }

    public void executeRepeatFilter(String repeatTable, String repeatResultTable, String refTable) {
        System.out.println("rfliter start" + " " + df.format(new Date()));
        try {
            databaseManager.executeSQL("insert into " + repeatResultTable + " select * from " + refTable + " as A left " +
                    "join " + repeatTable + " as B on (b.chrome=a.chrome and b.begin<a.pos and b" + ".end>a.pos) where " +
                    "b.chrome is null");

//        databaseManager.executeSQL("insert into " + repeatResultTable
//                + " select * from " + refTable
//                + " where not exists (select * FROM " + repeatTable + " where (" + repeatTable
//                + ".chrome= " + refTable + ".chrome and  " + repeatTable
//                + ".begin<" + refTable + ".pos and " + repeatTable
//                + ".end>" + refTable + ".pos)) ");


            System.out.println("esrepeat alu start " + " " + df.format(new Date()));

            databaseManager.executeSQL("insert into alutemp SELECT * from "
                    + refTable + " where exists (select chrome from "
                    + repeatTable + " where " + repeatTable
                    + ".chrome = " + refTable + ".chrome and " + repeatTable
                    + ".begin<" + refTable + ".pos and " + repeatTable
                    + ".end>" + refTable + ".pos and " + repeatTable + ".type='SINE/Alu')");
//        databaseManager.executeSQL("insert into alutemp select * from "
//                + refTable + " select * from "
//						+ refTable
//						+ " as A left join "
//						+ repeatTable
//						+ " as B on (b.chrome=a.chrome and b.begin<a.pos and b.end>a.pos and b.type='SINE/Alu')) ");

            System.out.println("esrepeat final start " + " " + df.format(new Date()));
            databaseManager.executeSQL("insert into " + repeatResultTable
                    + " select * from alutemp");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("rfilter end" + " " + df.format(new Date()));
    }

    public void rfilter(String repeatTable, String repeatResultTable, String refTable) {
        try {
            System.out.println("rfliter start" + " " + df.format(new Date()));// new
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
//					System.out.println(chr+" "+ps);
                        rs = databaseManager.query(repeatTable, " type ",
                                "(chrome='" + chr + "' and begin<" + ps
                                        + " and end>" + ps + ")");
                        if (!rs.next()) {
                            databaseManager.executeSQL("insert into " + repeatResultTable
                                    + "  select * from " + refTable
                                    + " where chrome='" + chr + "' and pos=" + ps
                                    + "");
                            count++;

                            if (count % 10000 == 0) {
                                databaseManager.commit();
                            }
                        }
                        // SINEalu is also what we need
                        else if (rs.next() && rs.getString(1) == "SINE/Alu") {
                            databaseManager.executeSQL("insert into " + repeatResultTable
                                    + "  select * from " + refTable
                                    + " where chrome='" + chr + "' and pos=" + ps
                                    + "");
                            count++;

                            if (count % 10000 == 0) {
                                databaseManager.commit();
                            }
                        }
                        break;
                }
            }

            databaseManager.commit();
            databaseManager.setAutoCommit(true);

            System.out.println("rfilter end" + " " + df.format(new Date()));
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
