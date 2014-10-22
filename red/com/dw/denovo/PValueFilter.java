package com.dw.denovo;

/**
 *P_value based on alt and ref 
 */

import com.dw.publicaffairs.DatabaseManager;
import com.xl.datatypes.probes.ProbeBean;
import com.xl.dialog.ProgressDialog;
import com.xl.dialog.REDProgressBar;
import net.sf.snver.pileup.util.math.FisherExact;
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
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatabaseManager databaseManager;

    private REDProgressBar progressBar = REDProgressBar.getInstance();

    public PValueFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public static void main(String[] args) {
        RCaller caller = new RCaller();
//        Globals.detect_current_rscript();
//        caller.setRExecutable("C:\\R\\R-3.1.1\\bin\\R.exe");
        caller.setRscriptExecutable("C:\\R\\R-3.1.1\\bin\\Rscript.exe");
        RCode code = new RCode();
        double[][] data = new double[][]{{233, 21}, {32, 12}};
        code.addDoubleMatrix("mydata", data);
        code.addRCode("result <- fisher.test(mydata)");
        code.addRCode("mylist <- list(pval = result$p.value)");
        caller.setRCode(code);
        caller.runAndReturnResult("mylist");
        double pValue = caller.getParser().getAsDoubleArray("pval")[0];
        System.out.println(pValue + "\t");

        FisherExact fisherExact = new FisherExact(1000);
        System.out.println(fisherExact.getTwoTailedP(233, 21, 32, 12));

        double[] datas = new double[]{0.02, 0.2343, 0.0005, 0.006, 0.4327, 0.2238, 0.43};

        caller = new RCaller();
        caller.setRscriptExecutable("C:\\R\\R-3.1.1\\bin\\Rscript.exe");
        code.addDoubleArray("parray", datas);
        code.addRCode("result<-p.adjust(parray,method='fdr',length(parray))");
        caller.setRCode(code);
        caller.runAndReturnResult("result");
        double[] result = caller.getParser().getAsDoubleArray("result");
        System.out.println("Calling FDR from R:");
        for (double re : result) {
            System.out.print(re + "\t");
        }
        System.out.println();
        System.out.println("Calling FDR from Jar Package1:");
//        FDR fdr = new FDR(datas, 1);
//        result = fdr.getThresholdedArray();
//        for (double re : result) {
//            System.out.print(re + "\t");
//        }
//        System.out.println();
//        System.out.println("Calling FDR from Jar Package2:");
//        result = fdr.getThresholdedArrayNP();
//        for (double re : result) {
//            System.out.print(re + "\t");
//        }
    }

    public boolean hasEstablishedDarnedTable(String darnedTable) {
        databaseManager.createRefTable(darnedTable, "(chrom varchar(15),coordinate int,strand varchar(5)," +
                "inchr varchar(5), inrna varchar(5) ,index(chrom,coordinate))");
        ResultSet rs = databaseManager.query(darnedTable, "count(*)", "1 limit 1,100");
        int number = 0;
        try {
            if (rs.next()) {
                number = rs.getInt(1);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // clear insert data
        return number > 0;
    }

    public void estblishPValueTable(String pvalueResultTable) {
        databaseManager.deleteTable(pvalueResultTable);
        databaseManager.createPValueTable(pvalueResultTable);
    }

    public void loadDarnedTable(String pvalueTable, String pvaluePath) {
        System.out.println("Start loading DarnedTable..." + df.format(new Date()));
        progressBar.addProgressListener(new ProgressDialog("Import pvalue data"));
        progressBar.progressUpdated("Start loading pvalue data from " + pvaluePath + " to " + pvalueTable, 0, 0);
        if (!hasEstablishedDarnedTable(pvalueTable)) {
            try {
                int count = 0;
                databaseManager.setAutoCommit(false);
                FileInputStream inputStream = new FileInputStream(pvaluePath);
                BufferedReader rin = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                // Skip the first row.
                rin.readLine();
                while ((line = rin.readLine()) != null) {
                    String[] sections = line.trim().split("\\t");
                    StringBuilder stringBuilder = new StringBuilder("insert into ");
                    stringBuilder.append(pvalueTable);
                    stringBuilder.append("(chrom,coordinate,strand,inchr,inrna) values(");
                    for (int i = 0; i < 5; i++) {
                        if (i == 0) {
                            stringBuilder.append("'chr").append(sections[i]).append("',");
                        } else if (i == 1) {
                            stringBuilder.append(sections[i]).append(",");
                        } else {
                            stringBuilder.append("'").append(sections[i]).append("',");
                        }
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1).append(")");
                    progressBar.progressUpdated("Importing " + count + " lines from " + pvaluePath + " to " + pvalueTable, 0, 0);
                    databaseManager.executeSQL(stringBuilder.toString());
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.err.println("Error load file from " + pvaluePath + " to file stream");
                e.printStackTrace();
                progressBar.progressExceptionReceived(e);
            } catch (SQLException e) {
                System.err.println("Error execute sql clause in " + PValueFilter.class.getName() + ":loadDarnedTable()");
                e.printStackTrace();
                progressBar.progressExceptionReceived(e);
            }
        }
        progressBar.progressComplete("pvalue_loaded", null);
        System.out.println("End loading DarnedTable..." + df.format(new Date()));
    }

    private List<PValueInfo> getExpectedInfo(String pvalueTable, String refTable) {
        List<PValueInfo> valueInfos = new ArrayList<PValueInfo>();
        try {
            ResultSet rs = databaseManager.query(refTable, "*", "1");
            while (rs.next()) {
                //1.CHROM varchar(15),2.POS int,3.ID varchar(30),4.REF varchar(3),5.ALT varchar(5),6.QUAL float(8,2),7.FILTER text,8.INFO text,9.GT text,
                // 10.AD text,11.DP text,12.GQ text,13.PL text
                PValueInfo info = new PValueInfo(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4).charAt(0), rs.getString(5).charAt(0),
                        rs.getFloat(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12),
                        rs.getString(13));
                String[] sections = info.getAd().split("/");
                info.altCount = Integer.parseInt(sections[0]);
                info.refCount = Integer.parseInt(sections[1]);
                info.setInDarnedDB(false);
                valueInfos.add(info);
            }
            //select refTable.* from refTable INNER JOIN pvalueTable ON refTable.chrom=pvalueTable.chrom and refTable.pos=pvalueTable.coordinate
            rs = databaseManager.query(refTable + "," + pvalueTable, refTable + ".*", refTable + ".chrom=" + pvalueTable + ".chrom and " + refTable + "" +
                    ".pos=" + pvalueTable + ".coordinate");
            while (rs.next()) {
                PValueInfo info = new PValueInfo(rs.getString(1), rs.getInt(2));
                if (valueInfos.contains(info)) {
                    valueInfos.get(valueInfos.indexOf(info)).setInDarnedDB(true);
                }
            }
            return valueInfos;

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

//    public double calPValue(double foundRef, double foundAlt, double knownRef, double knownAlt, RCaller caller, RCode code) {
//        try {
//            double[][] data = new double[][]{{foundRef, foundAlt}, {knownRef, knownAlt}};
//            code.addDoubleMatrix("mydata", data);
//            code.addRCode("result <- fisher.test(mydata)");
//            code.addRCode("mylist <- list(pval = result$p.value)");
//            caller.setRCode(code);
//            caller.runAndReturnResultOnline("mylist");
//            return caller.getParser().getAsDoubleArray("pval")[0];
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }

    private List<PValueInfo> executePValueFilter(String pvalueTable, String pvalueResultTable, String refTable) {
        System.out.println("Start executing PValueFilter..." + df.format(new Date()));
        List<PValueInfo> valueInfos = getExpectedInfo(pvalueTable, refTable);
        int knownAlt = 0;
        int knownRef = 0;
        for (PValueInfo info : valueInfos) {
            if (info.isInDarnedDB) {
                knownAlt += info.altCount;
                knownRef += info.refCount;
            } else {
                knownRef += info.altCount + info.refCount;
            }
        }
        knownAlt = (int) Math.ceil(knownAlt / valueInfos.size());
        knownRef = (int) Math.ceil(knownRef / valueInfos.size());
        FisherExact fisherExact = new FisherExact(1000);
        DecimalFormat dF = new DecimalFormat("#.###");
        for (PValueInfo pValueInfo : valueInfos) {
            int alt = pValueInfo.altCount;
            int ref = pValueInfo.refCount;
            double pValue = fisherExact.getTwoTailedP(ref, alt, knownRef, knownAlt);
            double level = (double) alt / (alt + ref);
            pValueInfo.setPValue(pValue);
            pValueInfo.setLevel(level);
            try {
                databaseManager.executeSQL("insert into " + pvalueResultTable + "(chrom,pos,id,ref,alt,qual,filter,info,gt,ad,dp,gq,pl,level," +
                        "pvalue) values( " + pValueInfo.toString() + "," + dF.format(level) + "," + pValue + ")");
            } catch (SQLException e) {
                System.err.println("Error execute sql clause in " + PValueFilter.class.getName() + ":executePValueFilter()");
                e.printStackTrace();
            }
        }
        System.out.println("End executing PValueFilter..." + df.format(new Date()));
        return valueInfos;
    }

    public void executeFDRFilter(String darnedTable, String darnedResultTable, String refTable, String rScript) {
        System.out.println("Start executing FDRFilter..." + df.format(new Date()));
        try {
            RCaller caller = new RCaller();
            if (rScript.trim().toLowerCase().contains("script")) {
                caller.setRscriptExecutable(rScript);
            } else {
                caller.setRExecutable(rScript);
            }
            RCode code = new RCode();
            List<PValueInfo> pValueList = executePValueFilter(darnedTable, darnedResultTable, refTable);
            double[] pValueArray = new double[pValueList.size()];
            for (int i = 0, len = pValueList.size(); i < len; i++) {
                pValueArray[i] = pValueList.get(i).getPvalue();
            }
            code.addDoubleArray("parray", pValueArray);
            code.addRCode("result<-p.adjust(parray,method='fdr',length(parray))");
            caller.setRCode(code);
            if (rScript.trim().toLowerCase().contains("script")) {
                caller.runAndReturnResult("result");
            } else {
                caller.runAndReturnResultOnline("result");
            }
            double[] results = caller.getParser().getAsDoubleArray("result");
            databaseManager.setAutoCommit(false);
            for (int i = 0, len = results.length; i < len; i++) {
                databaseManager.executeSQL("update " + darnedResultTable + " set fdr=" + results[i] + " where chrom='" + pValueList.get(i).getChr() + "' " +
                        "and pos=" + pValueList.get(i).getPos());
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
            databaseManager.executeSQL("delete from " + darnedResultTable + " where (pvalue > 0.05) || (fdr > 0.05)");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("End executing FDRFilter..." + df.format(new Date()));
    }

    private class PValueInfo extends ProbeBean {
        public boolean isInDarnedDB;
        public int refCount = 0;
        public int altCount = 0;

        public PValueInfo(String chr, int pos) {
            super(chr, pos);
        }

        public PValueInfo(String chr, int pos, String id, char ref, char alt, float qual, String filter, String info, String gt, String ad, String dp, String gq,
                          String pl) {
            super(chr, pos, id, ref, alt, qual, filter, info, gt, ad, dp, gq, pl);
        }

        public void setInDarnedDB(boolean isInDarnedDB) {
            this.isInDarnedDB = isInDarnedDB;
        }

        @Override
        public String toString() {
            return "'" + getChr() + "'," + getPos() + ",'" + getId() + "','" + getRef() + "','" + getAlt() + "'," + getQual() + ",'" + getFilter() + "'," +
                    "'" + getInfo() + "','" + getGt() + "','" + getAd() + "','" + getDp() + "','" + getGq() + "','" + getPl() + "'";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PValueInfo)) return false;
            PValueInfo that = (PValueInfo) o;
            return getPos() != that.getPos() && getChr().equals(that.getChr());
        }

        @Override
        public int hashCode() {
            int result = getChr().hashCode();
            result = 31 * result + getPos();
            return result;
        }
    }
}
