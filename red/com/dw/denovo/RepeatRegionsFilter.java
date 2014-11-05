package com.dw.denovo;

/**
 * we will filter out base in repeated area except for SINE/alu
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

public class RepeatRegionsFilter {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatabaseManager databaseManager;
    private REDProgressBar progressBar = REDProgressBar.getInstance();

    public RepeatRegionsFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean hasEstablishedRepeatTable(String repeatTable) {
        databaseManager.createRefTable(repeatTable, "(chrom varchar(30),begin int,end int,type varchar(40),index(chrom,begin,end))");
        ResultSet rs = databaseManager.query(repeatTable, "count(*)", "1 limit 0,100");
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

//    public void  establishedRepeatTable(String repeatTable){
//        databaseManager.createRefTable(repeatTable,
//                "(swscore int,perc1 float(6,3),perc2 float(6,3),perc3 float(6,3),chrom varchar(15),querybegin int," +
//                        "queryend int, leftpos varchar(20),complement varchar(2),repeatname varchar(20)," +
//                        "repeattype varchar(20), dbbegin int, dbend int,basesnum varchar(20), id int,index(chrom," +
//                        "querybegin, queryend))");
//    }

    public void establishRepeatResultTable(String repeatResultTable) {
        databaseManager.deleteTable(repeatResultTable);
        databaseManager.createFilterTable(repeatResultTable);
    }

//    public void loadRepeatTable2(String repeatTable, String repeatPath) {
//        System.out.println("Start loading RepeatTable" + " " + df.format(new Date()));
//
//        try {
//            establishedRepeatTable(repeatTable);
//            databaseManager.executeSQL("load data local infile '" + repeatPath + "' into table " + repeatTable +
//                    " fields terminated by ' ' lines terminated by '\n' IGNORE 3 LINES");
//        } catch (SQLException e) {
//            System.err.println("Error execute sql clause in " + RepeatRegionsFilter.class.getName() + ":loadDbSNPTable()");
//            e.printStackTrace();
//        }
//
//        System.out.println("End loading RepeatTable" + " " + df.format(new Date()));
//    }

    public void loadRepeatTable(String repeatTable, String repeatPath) {
        System.out.println("Start loading RepeatTable..." + df.format(new Date()));
        progressBar.addProgressListener(new ProgressDialog("Import repeat region data"));
        progressBar.progressUpdated("Start loading repeated region data from " + repeatPath + " to " + repeatTable, 0, 0);
        BufferedReader rin = null;
        try {
            if (!hasEstablishedRepeatTable(repeatTable)) {
                databaseManager.setAutoCommit(false);
                int count = 0;
                FileInputStream inputStream = new FileInputStream(repeatPath);
                rin = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                rin.readLine();
                rin.readLine();
                rin.readLine();
                while ((line = rin.readLine()) != null) {
                    String section[] = line.trim().split("\\s+");
                    if (count % 1000 == 0) {
                        progressBar.progressUpdated("Importing " + count + " lines from " + repeatPath + " to " + repeatTable, 0, 0);
                    }
                    databaseManager.executeSQL("insert into " + repeatTable + "(chrom,begin,end,type) values('" +
                            section[4] + "','" + section[5] + "','" + section[6] + "','" + section[10] + "')");
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error load file from " + repeatPath + " to file stream");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + RepeatRegionsFilter.class.getName() + ":loadRepeatTable()");
            e.printStackTrace();
        } finally {
            if (rin != null) {
                try {
                    rin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        progressBar.progressComplete("repeat_loaded", null);
        System.out.println("End loading RepeatTable..." + df.format(new Date()));
    }

    public void executeRepeatFilter(String repeatTable, String repeatResultTable, String aluResultTable, String refTable) {
        System.out.println("Start executing RepeatRegionsFilter..." + df.format(new Date()));
        try {
            databaseManager.executeSQL("insert into " + repeatResultTable + " select * from " + refTable + " where not exists (select * from " +
                    repeatTable + " where (" + repeatTable + ".chrom= " + refTable + ".chrom and  " + repeatTable + ".begin<=" + refTable + ".pos and " +
                    repeatTable + ".end>=" + refTable + ".pos)) ");

            System.out.println("Start executing AluFilter..." + df.format(new Date()));

            databaseManager.executeSQL("insert into " + aluResultTable + " SELECT * from " + refTable + " where exists (select chrom from " + repeatTable
                    + " where " + repeatTable + ".chrom = " + refTable + ".chrom and " + repeatTable + ".begin<=" + refTable + ".pos and " + repeatTable
                    + ".end>=" + refTable + ".pos and " + repeatTable + ".type='SINE/Alu')");

            databaseManager.executeSQL("update " + aluResultTable + " set alu = 'T'");
            databaseManager.executeSQL("insert into " + repeatResultTable + " select * from " + aluResultTable);

            System.out.println("End executing AluFilter..." + df.format(new Date()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("End executing RepeatRegionsFilter..." + df.format(new Date()));
    }

    public void establishAluResultTable(String aluResultTable) {
        databaseManager.deleteTable(aluResultTable);
        databaseManager.createFilterTable(aluResultTable);
    }

    //    public void rfilter(String repeatTable, String repeatResultTable, String aluResultTable, String refTable) {
//        try {
//            System.out.println("rfliter start" + " " + df.format(new Date()));// new
//            ResultSet rs = databaseManager.query(refTable, "chrom,pos", "1");
//            List<String> coordinate = new ArrayList<String>();
//            databaseManager.setAutoCommit(false);
//
//            while (rs.next()) {
//                coordinate.add(rs.getString(1));
//                coordinate.add(rs.getString(2));
//            }
//            for (int i = 0, len = coordinate.size(); i < len; i++) {
//                switch (i % 2) {
//                    case 0:
//                        chr = coordinate.get(i);
//                        break;
//                    case 1:
//                        ps = coordinate.get(i);
////					System.out.println(chr+" "+ps);
//                        rs = databaseManager.query(repeatTable, " type ",
//                                "(chrom='" + chr + "' and begin<" + ps
//                                        + " and end>" + ps + ")");
//
//                        if (!rs.next()) {
//                            databaseManager.executeSQL("insert into " + repeatResultTable
//                                    + "  select * from " + refTable
//                                    + " where chrom='" + chr + "' and pos=" + ps
//                                    + "");
//                            count++;
//                        } else if (rs.next() && rs.getString(1).equals("SINE/Alu")) {
//                            databaseManager.executeSQL("insert into " + aluResultTable
//                                    + "  select * from " + refTable
//                                    + " where chrom='" + chr + "' and pos=" + ps);
//                            count++;
//                            if (count % 10000 == 0) {
//                                databaseManager.commit();
//                            }
//                        }
//                        break;
//                }
//            }
//            databaseManager.commit();
//            databaseManager.executeSQL("insert into " + repeatResultTable + "  select * from " + aluResultTable);
//            databaseManager.commit();
//            databaseManager.setAutoCommit(true);
//
//            System.out.println("rfilter end" + " " + df.format(new Date()));
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
}
