package com.dw.denovo;

/**
 *P_value based on alt and ref 
 */

import com.dw.publicaffairs.DatabaseManager;
import rcaller.Globals;
import rcaller.RCaller;
import rcaller.RCode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PValueFilter {
    private DatabaseManager databaseManager;
    // File file = new File("D:/TDDOWNLOAD/data/hg19.txt");
    FileInputStream inputStream;
    private String line = null;
    private String[] col = new String[40];
    private StringBuffer s2 = new StringBuffer();
    private StringBuffer s3 = new StringBuffer();
    private int count = 1;
    private String chr;
    private String ps;
    private double known_alt = 0;
    private double known_ref = 0;
    ArrayList<Double> fd_ref = new ArrayList<Double>();
    ArrayList<Double> fd_alt = new ArrayList<Double>();
    List<String> coordinate = new ArrayList<String>();
    List<String> pValueCoordinate = new ArrayList<String>();

    private double fdr = 0;
    private double ref_n = 0;
    private double alt_n = 0;
    private double pValue = 0;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public PValueFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    public void loadDarnedTable(String darnedTable, String darnedPath) {
        try {
            System.out.println("loadhg19 start" + " " + df.format(new Date()));// new

            int count_ts = 0;
            inputStream = new FileInputStream(darnedPath);
            BufferedReader rin = new BufferedReader(new InputStreamReader(
                    inputStream));
            while ((line = rin.readLine()) != null) {
                StringBuffer s1 = new StringBuffer();
                if (count > 0) {
                    s2.append(line.split("\\t")[0] + " " + "varchar(15)");
                    s2.append("," + line.split("\\t")[1] + " " + "int");
                    s2.append("," + line.split("\\t")[2] + " " + "varchar(5)");
                    s2.append("," + line.split("\\t")[3] + " " + "varchar(5)");
                    s2.append("," + line.split("\\t")[4] + " " + "varchar(5)");
                    count--;
                    s3.append(line.split("\\t")[0]);
                    for (int i = 1; i < 5; i++)
                        s3.append("," + line.split("\\t")[i]);
                    databaseManager.deleteTable(darnedTable);
                    databaseManager.createTable(darnedTable, "(" + s2
                            + ",index(chrom,coordinate))");
                    continue;
                }
                databaseManager.setAutoCommit(false);
                for (int i = 0; i < 5; i++) {
                    col[i] = line.split("\\t")[i];
                    if (i == 0 && col[i].length() < 3)
                        col[i] = "chr" + col[i];
                }
                // A-I or G is what we focus on
                if (col[3].toCharArray()[0] == 'A'
                        && (col[4].toCharArray()[0] == 'G' || col[4]
                        .toCharArray()[0] == 'I')) {
                    s1.append("'" + col[0] + "'");
                    for (int i = 1; i < 5; i++)
                        s1.append("," + "'" + col[i] + "'");
                    databaseManager.executeSQL("insert into " + darnedTable + "("
                            + s3 + ") values(" + s1 + ")");
                    count_ts++;
                    if (count_ts % 20000 == 0)
                        databaseManager.commit();
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);

            // clear insert data
            s2.delete(0, s2.length());
            s3.delete(0, s3.length());

            System.out.println("loadhg19 end" + " " + df.format(new Date()));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error load file from " + darnedPath + " to file stream");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + PValueFilter.class.getName() + ":loadRnaVcfTable()");
            e.printStackTrace();
        }
    }

    private void level(String refTable, String chr, String ps) {
        try {
            ref_n = alt_n = 0;
            ResultSet rs = databaseManager.query(refTable, "AD", "chrome='"
                    + chr + "' and pos=" + ps + "");

            List<String> coordinate_level = new ArrayList<String>();

            while (rs.next()) {
                coordinate_level.add(rs.getString(1));
            }
            for (int i = 0, len = coordinate_level.size(); i < len; i++) {
                String[] section = coordinate_level.get(i).split(";");
                ref_n = Double.parseDouble(section[0]);
                alt_n = Double.parseDouble(section[1]);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void Exp_num(String darnedTable, String refTable) {
        try {
            ResultSet rs = databaseManager.query(refTable, "chrome,pos", "1");

            while (rs.next()) {
                coordinate.add(rs.getString(1));
                coordinate.add(rs.getString(2));
            }
            for (int i = 0, len = coordinate.size(); i < len; i++) {
                if (i % 2 == 0) {
                    chr = coordinate.get(i);
                } else {
                    ps = coordinate.get(i);
                    level(refTable, chr, ps);
                    rs = databaseManager.query(darnedTable, "strand", "chrom='"
                            + chr + "' and coordinate='" + ps + "'");
                    fd_alt.add(alt_n);
                    fd_ref.add(ref_n);
                    if (rs.next()) {
                        known_alt += alt_n;
                        known_ref += ref_n;
                    } else {
                        known_alt += 0;
                        known_ref += (alt_n + ref_n);
                    }
                }

            }

            known_alt /= (coordinate.size() / 2);
            known_ref /= (coordinate.size() / 2);
            known_alt = Math.round(known_alt);
            known_ref = Math.round(known_ref);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private double calculate(double found_ref, double found_alt,
                             double known_ref, double known_alt, String commandD) {
        try {
            RCaller caller = new RCaller();
            RCode code = new RCode();
            Globals.detect_current_rscript();
            caller.setRscriptExecutable(commandD);

            double[][] data = new double[][]{{found_ref, found_alt},
                    {known_ref, known_alt}};
            // double[][] data=new double[][]{{51.2,49.3},{100.,0}};
            code.addDoubleMatrix("mydata", data);
            code.addRCode("result <- fisher.test(mydata)");
            code.addRCode("mylist <- list(pval = result$p.value)");

            caller.setRCode(code);
            caller.runAndReturnResult("mylist");
            pValue = caller.getParser().getAsDoubleArray("pval")[0];
            return pValue;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return 0;
    }

    public void estblishPvTable(String darnedResultTable) {
        databaseManager.deleteTable(darnedResultTable);
        databaseManager
                .createTable(
                        darnedResultTable,
                        "(chrome varchar(15),pos int,ref smallint,alt smallint,level varchar(10),p_value double,fdr double)");
    }

    private List<Double> executePValueFilter(String darnedTable, String darnedResultTable, String refTable,
                                             String commandD) {
        System.out.println("executePValueFilter start" + " " + df.format(new Date()));

        List<Double> pValueList = new ArrayList<Double>();
        Exp_num(darnedTable, refTable);
        DecimalFormat dF = new DecimalFormat("0.000 ");

        for (int i = 0, len = fd_ref.size(); i < len; i++) {
//			System.out.println(fd_ref.get(i) + " " + coordinate.get(i) + " "
//					+ i);
            ref_n = fd_ref.get(i);
            alt_n = fd_alt.get(i);
            chr = coordinate.get(2 * i);

            ps = coordinate.get(2 * i + 1);
            double lev = alt_n / (alt_n + ref_n);
            // double lev=found_alt[i]/(found_alt[i]+found_ref[i]);
            // if (((int) ref_n + (int) alt_n) < 6)
            // {
            // continue;
            // }
            calculate(ref_n, alt_n, known_ref, known_alt, commandD);
            if (pValue < 0.05) {
                try {
                    databaseManager.executeSQL("insert into " + darnedResultTable
                            + "(chrome,pos,ref,alt,level,p_value) values('" + chr
                            + "'," + ps + "," + (int) ref_n + "," + (int) alt_n + ","
                            + dF.format(lev) + "," + pValue + ")");
                } catch (SQLException e) {
                    System.err.println("Error execute sql clause in " + PValueFilter.class.getName() + ":executePValueFilter()");
                    e.printStackTrace();
                }
                // System.out.println(chr+" "+ps+" "+(int)found_ref[i]+" "+(int)found_alt[i]+" "+dF.format(lev)+" "+pValue);
                pValueList.add(pValue);
                pValueCoordinate.add(chr);
                pValueCoordinate.add(ps);
            }
        }
        System.out.println("executePValueFilter end" + " " + df.format(new Date()));
        return pValueList;
    }

    public void executeFDRFilter(String darnedTable, String darnedResultTable, String refTable, String rScciptPath) {

        try {
            RCaller caller = new RCaller();
            RCode code = new RCode();
            Globals.detect_current_rscript();
            caller.setRscriptExecutable(rScciptPath);
            List<Double> pValueList = executePValueFilter(darnedTable, darnedResultTable, refTable, rScciptPath);
            double[] pValueArray = new double[pValueList.size()];
            for (int i = 0, len = pValueList.size(); i < len; i++) {
                pValueArray[i] = pValueList.get(i);
            }
            
            code.addDoubleArray("parray", pValueArray);
            code.addRCode("result <- qvalue(parray)");
//            code.addRCode("result<-p.adjust(parray,method='fdr',length(parray))");
            // code.addRCode("mylist <- list(qval = result$q.value)");
            caller.setRCode(code);
            caller.runAndReturnResult("result");

            double[] results = caller.getParser().getAsDoubleArray("result");
            for (int i = 0, len = results.length; i < len; i++) {
                // for (int i = 0; i < coordinate.size(); i++) {
                fdr = results[i];
                chr = pValueCoordinate.get(2 * i);
                ps = pValueCoordinate.get(2 * i + 1);
                if (fdr < 0.05) {
                    databaseManager.executeSQL("update " + darnedResultTable
                            + " set fdr=" + fdr + " where chrome='" + chr
                            + "' and pos=" + ps + " ");
                }
            }
            // clear insert data
            s2.delete(0, s2.length());
            s3.delete(0, s3.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
