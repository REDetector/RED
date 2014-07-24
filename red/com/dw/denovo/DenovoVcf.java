package com.dw.denovo;

/**
 * import vcf file
 */

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DenovoVcf {
    private DatabaseManager databaseManager;

    FileInputStream inputStream;
    // SQL to be executed
    // basic unit to process
    private String line = null;
    // data of each column
    private String[] col = new String[40];
    private String[] temp = new String[10];
    private StringBuffer s1 = new StringBuffer();
    // count for each function
    private int count_r = 1;
    private int depth = -1;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // establish table structure for following tables
    public DenovoVcf(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // public int getdepth() {
    // try {
    // inputStream = new FileInputStream(rnaVcfPath);
    // } catch (FileNotFoundException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // BufferedReader rin = new BufferedReader(new InputStreamReader(
    // inputStream));
    // try {
    // while ((line = rin.readLine()) != null) {
    // if (line.startsWith("##"))
    // continue;
    // if (line.startsWith("#"))
    // continue;
    // // value in each line
    // for (int k = 0; k < line.split("\\t")[8].split(":").length; k++) {
    // if (line.split("\\t")[8].split(":")[k].equals("DP")) {
    // depth = k;
    // }
    // }
    // break;
    // }
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // System.out.println(depth);
    // return depth;
    // }

    public void establishRnaTable(String rnaVcfTable) {
        databaseManager.deleteTable(rnaVcfTable);
        databaseManager.createTable(rnaVcfTable, "(chrome varchar(15),"
                + Utilities.getInstance().getS2() + ",index(chrome,pos))");
    }

    // table for RnaVcf X.length-9=time for circulation
    public void loadRnaVcfTable(String rnaVcfTable, String rnaVcfPath, int num) {
        System.out.println("rnavcf start" + " " + df.format(new Date()));

        try {
            databaseManager.setAutoCommit(false);
            // timer for transaction
            int ts_count = 0;
            int gtype = -1;
            inputStream = new FileInputStream(rnaVcfPath);
            BufferedReader rin = new BufferedReader(new InputStreamReader(
                    inputStream));
            while ((line = rin.readLine()) != null) {
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    continue;
                }
                for (int i = 0; i < line.split("\\t").length; i++) {
                    col[i] = line.split("\\t")[i];
                }
                int length = col[8].split(":").length;
                if (col[num].split(":").length != length) {
                    continue;
                }
                if (count_r > 0) {
                    for (int i = 0; i < length; i++) {
                        if (col[8].split(":")[i].equals("DP")) {
                            depth = i;
                        }
                        if (col[8].split(":")[i].equals("GT")) {
                            gtype = i;
                        }
                    }
                    count_r--;
                }
                // data for import
                // '.' stands for undetected, so we discard it
                // if ((depth>-1)&&col[num].split(":")[depth].equals("."))
                // continue;
                if (gtype > -1
                        && ((col[num].split(":")[gtype].split("/")[0]
                        .equals(".")) || (col[num].split(":")[gtype]
                        .split("/")[1].equals(".")))) {
                    continue;
                }
                s1.append("'" + col[0] + "'");
                for (int i = 1; i < 8; i++)
                    s1.append("," + "'" + col[i] + "'");
                for (int i = 0; i < col[num].split(":").length; i++) {
                    temp[i] = col[num].split(":")[i].replace(",", ";");
                    // System.out.println(temp[i]);
                    s1.append("," + "'" + temp[i] + "'");
                }
                databaseManager.executeSQL("insert into " + rnaVcfTable + "("
                        + Utilities.getInstance().getS3() + ") values(" + s1
                        + ")");
                ts_count++;
                if (ts_count % 20000 == 0)
                    databaseManager.commit();
                // clear insert data
                s1.delete(0, s1.length());
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error load file from " + rnaVcfPath + " to file stream");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + DenovoVcf.class.getName() + ":loadRnaVcfTable()");
            e.printStackTrace();
        }

        System.out.println("rnavcf end" + " " + df.format(new Date()));
    }

    // table for RnaVcf X.length-9=time for circulation
    public void loadRnaVcfTable(String rnaVcfTable, String rnaVcfPath) {
        System.out.println("rnavcf start" + " " + df.format(new Date()));

        try {
            databaseManager.setAutoCommit(false);
            // timer for transaction
            int ts_count = 0;
            inputStream = new FileInputStream(rnaVcfPath);
            BufferedReader rin = new BufferedReader(new InputStreamReader(
                    inputStream));
            while ((line = rin.readLine()) != null) {
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    continue;
                }
                for (int i = 0; i < line.split("\\t").length; i++) {
                    col[i] = line.split("\\t")[i];
                }
                int length = col[8].split(":").length;
                if (col[9].split(":").length != length) {
                    continue;
                }
                if (count_r > 0) {
                    for (int i = 0; i < length; i++) {
                        if (col[8].split(":")[i].equals("DP")) {
                            depth = i;
                        }
                    }
                    count_r--;
                }
                // data for import
                // '.' stands for undetected, so we discard it
                // if ((depth>-1)&&col[num].split(":")[depth].equals("."))
                // continue;
                s1.append("'" + col[0] + "'");
                for (int i = 1; i < 8; i++)
                    s1.append("," + "'" + col[i] + "'");
                for (int i = 0; i < col[9].split(":").length; i++) {
                    temp[i] = col[9].split(":")[i].replace(",", ";");
                    // System.out.println(temp[i]);
                    s1.append("," + "'" + temp[i] + "'");
                }
                databaseManager.executeSQL("insert into " + rnaVcfTable + "("
                        + Utilities.getInstance().getS3() + ") values(" + s1
                        + ")");
                ts_count++;
                if (ts_count % 20000 == 0)
                    databaseManager.commit();
                // clear insert data
                s1.delete(0, s1.length());
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error load file from " + rnaVcfPath + " to file stream");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + DenovoVcf.class.getName() + ":loadRnaVcfTable()");
            e.printStackTrace();
        }
        System.out.println("rnavcf end" + " " + df.format(new Date()));
    }
}
