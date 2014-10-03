package com.dw.denovo;

/**
 *P_value based on alt and ref 
 */

import com.dw.publicaffairs.DatabaseManager;
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

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public PValueFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
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

    public void estblishPValueTable(String darnedResultTable) {
        databaseManager.deleteTable(darnedResultTable);
        databaseManager.createPValueTable(darnedResultTable);
    }

    public void loadDarnedTable(String darnedTable, String darnedPath) {
        System.out.println("Start loading DarnedTable..." + df.format(new Date()));
        if (!hasEstablishedDarnedTable(darnedTable)) {
            try {
                int count_ts = 0;
                databaseManager.setAutoCommit(false);
                FileInputStream inputStream = new FileInputStream(darnedPath);
                BufferedReader rin = new BufferedReader(new InputStreamReader(
                        inputStream));
                String line;
                // Skip the first row.
                rin.readLine();
                while ((line = rin.readLine()) != null) {
                    String[] sections = line.trim().split("\\t");
                    StringBuilder stringBuilder = new StringBuilder("insert into ");
                    stringBuilder.append(darnedTable);
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
                    databaseManager.executeSQL(stringBuilder.toString());
                    count_ts++;
                    if (count_ts % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.err.println("Error load file from " + darnedPath + " to file stream");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Error execute sql clause in " + PValueFilter.class.getName() + ":loadDarnedTable()");
                e.printStackTrace();
            }
        }
        System.out.println("End loading DarnedTable..." + df.format(new Date()));
    }

    private List<PValueInfo> getExpectedInfo(String darnedTable, String refTable) {
        List<PValueInfo> valueInfos = new ArrayList<PValueInfo>();
        try {
            ResultSet rs = databaseManager.query(refTable, "chrom,pos,AD", "1");
            while (rs.next()) {
                String[] sections = rs.getString(3).split("/");
                PValueInfo info = new PValueInfo(rs.getString(1), rs.getInt(2), Integer.parseInt(sections[0]),
                        Integer.parseInt(sections[1]));
                info.setInDarnedDB(false);
                valueInfos.add(info);
            }
            rs = databaseManager.query(refTable + "," + darnedTable, refTable + ".chrom," + refTable + ".pos," +
                    "" + refTable + ".AD", refTable + ".chrom=" + darnedTable + ".chrom and " + refTable + "" +
                    ".pos=" + darnedTable + ".coordinate");
            while (rs.next()) {
                String[] sections = rs.getString(3).split("/");
                PValueInfo valueInfo = new PValueInfo(rs.getString(1), rs.getInt(2), Integer.parseInt(sections[0]),
                        Integer.parseInt(sections[1]));
                if (valueInfos.contains(valueInfo)) {
                    valueInfos.get(valueInfos.indexOf(valueInfo)).setInDarnedDB(true);
                }

            }
            return valueInfos;

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }


//    private void Exp_num(String darnedTable, String refTable) {
//        try {
//            ResultSet rs = databaseManager.query(refTable, "chrom,pos", "1");
//
//            while (rs.next()) {
//                coordinate.add(rs.getString(1));
//                coordinate.add(rs.getString(2));
//            }
//            for (int i = 0, len = coordinate.size(); i < len; i++) {
//                if (i % 2 == 0) {
//                    chr = coordinate.get(i);
//                } else {
//                    ps = coordinate.get(i);
//                    queryCoverage(refTable, chr, ps);
//                    rs = databaseManager.query(darnedTable, "strand", "chrom='"
//                            + chr + "' and coordinate='" + ps + "'");
//                    fd_alt.add(alt_n);
//                    fd_ref.add(ref_n);
//                    if (rs.next()) {
//                        known_alt += alt_n;
//                        known_ref += ref_n;
//                    } else {
//                        known_alt += 0;
//                        known_ref += (alt_n + ref_n);
//                    }
//                }
//
//            }
//            known_alt /= (coordinate.size() / 2);
//            known_ref /= (coordinate.size() / 2);
//            known_alt = Math.round(known_alt);
//            known_ref = Math.round(known_ref);
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    public static double calPValue(double found_ref, double found_alt,
                                   double known_ref, double known_alt, RCaller caller, RCode code) {
        try {
            double[][] data = new double[][]{{found_ref, found_alt}, {known_ref, known_alt}};
            code.addDoubleMatrix("mydata", data);
            code.addRCode("result <- fisher.test(mydata)");
            code.addRCode("mylist <- list(pval = result$p.value)");
            caller.setRCode(code);
            caller.runAndReturnResultOnline("mylist");
            return caller.getParser().getAsDoubleArray("pval")[0];
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private List<PValueInfo> executePValueFilter(String darnedTable, String darnedResultTable, String refTable,
                                                 RCaller caller, RCode code) {
        System.out.println("Start executing PValueFilter..." + df.format(new Date()));
        List<PValueInfo> valueInfos = getExpectedInfo(darnedTable, refTable);
        double known_alt = 0;
        double known_ref = 0;
        for (PValueInfo info : valueInfos) {
            if (info.isInDarnedDB) {
                known_alt += info.alt;
                known_ref += info.ref;
            } else {
                known_ref += info.alt + info.ref;
            }
        }
        known_alt /= valueInfos.size();
        known_ref /= valueInfos.size();
        DecimalFormat dF = new DecimalFormat("#.###");
        for (int i = 0, len = valueInfos.size(); i < len; i++) {
            String chr = valueInfos.get(i).chr;
            int pos = valueInfos.get(i).pos;
            int alt = valueInfos.get(i).alt;
            int ref = valueInfos.get(i).ref;
            double pValue = calPValue(ref, alt, known_ref, known_alt, caller, code);
            if (pValue < 0.05) {
                double level = (double) alt / (alt + ref);
                valueInfos.get(i).setpValue(pValue);
                try {
                    databaseManager.executeSQL("insert into " + darnedResultTable + "(chrom,pos,ref,alt,level,p_value) values('" + chr
                            + "'," + pos + "," + ref + "," + alt + "," + dF.format(level) + "," + pValue + ")");
                } catch (SQLException e) {
                    System.err.println("Error execute sql clause in " + PValueFilter.class.getName() + ":executePValueFilter()");
                    e.printStackTrace();
                }
            } else {
                valueInfos.remove(i);
            }
        }
        System.out.println("End executing PValueFilter..." + df.format(new Date()));
        return valueInfos;
    }

    public void executeFDRFilter(String darnedTable, String darnedResultTable, String refTable, String rExecutable) {
        System.out.println("Start executing FDRFilter..." + df.format(new Date()));
        try {
            RCaller caller = new RCaller();
            caller.setRExecutable(rExecutable);
            RCode code = new RCode();
            List<PValueInfo> pValueList = executePValueFilter(darnedTable, darnedResultTable, refTable, caller, code);
            double[] pValueArray = new double[pValueList.size()];
            for (int i = 0, len = pValueList.size(); i < len; i++) {
                pValueArray[i] = pValueList.get(i).pValue;
            }
            code.addDoubleArray("parray", pValueArray);
//            code.addRCode("qobj <- qvalue(parray)");
//            code.addRCode("mylist<-list(qval=qobj$qvalues");
            code.addRCode("result<-p.adjust(parray,method='fdr',length(parray))");
            // code.addRCode("mylist <- list(qval = result$q.value)");
            caller.setRCode(code);
            caller.runAndReturnResultOnline("result");

            double[] results = caller.getParser().getAsDoubleArray("result");
            for (int i = 0, len = results.length; i < len; i++) {
                double fdr = results[i];
                if (fdr < 0.05) {
                    databaseManager.executeSQL("update " + darnedResultTable
                            + " set fdr=" + fdr + " where chrom='" + pValueList.get(i).chr
                            + "' and pos=" + pValueList.get(i).pos);
                }
            }
            // clear insert data
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("End executing FDRFilter..." + df.format(new Date()));
    }

    private class PValueInfo {
        public String chr;
        public int pos;
        public int alt;
        public int ref;
        public double pValue;
        public boolean isInDarnedDB;

        public PValueInfo(String chr, int pos, int alt, int ref) {
            this.chr = chr;
            this.pos = pos;
            this.alt = alt;
            this.ref = ref;
        }

        public void setInDarnedDB(boolean isInDarnedDB) {
            this.isInDarnedDB = isInDarnedDB;
        }

        public void setpValue(double pValue) {
            this.pValue = pValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PValueInfo)) return false;
            PValueInfo that = (PValueInfo) o;
            return pos != that.pos && chr.equals(that.chr);
        }

        @Override
        public int hashCode() {
            int result = chr.hashCode();
            result = 31 * result + pos;
            return result;
        }
    }

//    public static void main(String[] args) {
//        RCaller caller = new RCaller();
////        Globals.detect_current_rscript();
//        caller.setRExecutable("C:\\R\\R-3.1.1\\bin\\R.exe");
//        RCode code = new RCode();
//        double[][] data = new double[][]{{233, 21}, {32, 12}};
//        for (int i = 0; i < 10; i++) {
////            code.clear();
//            code.addDoubleMatrix("mydata", data);
//            code.addRCode("result <- fisher.test(mydata)");
//            code.addRCode("mylist <- list(pval = result$p.value)");
//            caller.setRCode(code);
//            caller.runAndReturnResultOnline("mylist");
//            double pValue = caller.getParser().getAsDoubleArray("pval")[0];
//            System.out.println(pValue + "\t");
//        }
//    }
}
