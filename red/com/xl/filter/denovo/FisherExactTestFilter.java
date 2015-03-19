/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.filter.denovo;


import com.xl.database.DatabaseManager;
import com.xl.datatypes.sites.SiteBean;
import com.xl.display.dialog.ProgressDialog;
import com.xl.display.dialog.REDProgressBar;
import com.xl.exception.DataLoadException;
import com.xl.utils.Timer;
import net.sf.snver.pileup.util.math.FisherExact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rcaller.RCaller;
import rcaller.RCode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class FisherExactTestFilter is a statistical filter to reduce the errors in detecting RNA editing sites caused by technical artifacts (e.g., sequencing
 * errors).
 */
public class FisherExactTestFilter {
    private final Logger logger = LoggerFactory.getLogger(FisherExactTestFilter.class);
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager;
    /**
     * The progress bar for loading DARNED database.
     */
    private REDProgressBar progressBar = REDProgressBar.getInstance();

    /**
     * Initiate a new fisher's exact test filter.
     *
     * @param databaseManager the database manager
     */
    public FisherExactTestFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Check whether the DARNED file has been loaded into database by calculating the row count of this table.
     *
     * @param darnedTable The DARNED database table name, it is constant.
     * @return true if DARNED data exists in the database.
     */
    public boolean hasEstablishedDarnedTable(String darnedTable) {
        return databaseManager.getRowCount(darnedTable) > 0;
    }

    /**
     * Load DARNED file into database.
     *
     * @param darnedTable The DARNED database table name, it is constant.
     * @param darnedPath  The DARNED file path.
     */
    public void loadDarnedTable(String darnedTable, String darnedPath) throws DataLoadException {
        logger.info("Start loading DarnedTable... {}", Timer.getCurrentTime());
        progressBar.addProgressListener(new ProgressDialog("Import DARNED file into database..."));
        progressBar.progressUpdated("Start loading DARNED data from " + darnedPath + " to " + darnedTable + " table", 0, 0);
        if (!hasEstablishedDarnedTable(darnedTable)) {
            BufferedReader rin = null;
            try {
                int count = 0;
                databaseManager.setAutoCommit(false);
                rin = new BufferedReader(new InputStreamReader(new FileInputStream(darnedPath)));
                String line;
                // Skip the first row.
                rin.readLine();
                while ((line = rin.readLine()) != null) {
                    String[] sections = line.trim().split("\\t");
                    // insert into
                    StringBuilder stringBuilder = new StringBuilder("insert into ");
                    // insert into darned_database
                    stringBuilder.append(darnedTable);
                    // insert into darned_database(chrom,coordinate,strand,inchr,inrna) values(
                    stringBuilder.append("(chrom,coordinate,strand,inchr,inrna) values(");
                    for (int i = 0; i < 5; i++) {
                        // insert into darned_database(chrom,coordinate,strand,inchr,inrna) values(value1,value2,value3,value4,value5,
                        if (i == 0) {
                            stringBuilder.append("'chr").append(sections[i]).append("',");
                        } else if (i == 1) {
                            stringBuilder.append(sections[i]).append(",");
                        } else {
                            stringBuilder.append("'").append(sections[i]).append("',");
                        }
                    }
                    // insert into darned_database(chrom,coordinate,strand,inchr,inrna) values(value1,value2,value3,value4,value5)
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1).append(")");
                    if (count % 1000 == 0) {
                        progressBar.progressUpdated("Importing " + count + " lines from " + darnedPath + " to " + darnedTable + " table", 0, 0);
                    }
                    databaseManager.executeSQL(stringBuilder.toString());
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            } catch (IOException e) {
                DataLoadException de = new DataLoadException("Error load file", darnedPath);
                logger.error("Error load file from " + darnedPath + " to file stream", de);
                throw de;
            } finally {
                if (rin != null) {
                    try {
                        rin.close();
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
            }
        }
        progressBar.progressComplete("darned_loaded", null);
        logger.info("End loading DarnedTable... {}", Timer.getCurrentTime());
    }

    /**
     * Calculate expected count of reference base and alternative base from DARNED database.
     *
     * @param darnedTable   The DARNED database table name, it is constant.
     * @param previousTable Previous table name
     * @return a list that contains all information from a given table.
     */
    private List<PValueInfo> getExpectedInfo(String darnedTable, String previousTable) throws SQLException {
        List<PValueInfo> values = new ArrayList<PValueInfo>();
        // First, we put all sites with all site information into a list, no matter whether the site is in DARNED database or not.
        ResultSet rs = databaseManager.query(previousTable, null, null, null);
        while (rs.next()) {
            //1.CHROM varchar(30),2.POS int,3.ID varchar(30),4.REF varchar(3),5.ALT varchar(5),6.QUAL float(8,2),7.FILTER text,8.INFO text,9.GT text,
            // 10.AD text,11.DP text,12.GQ text,13.PL text,14.ALU varchar(1)
            PValueInfo info = new PValueInfo(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4).charAt(0), rs.getString(5).charAt(0),
                    rs.getFloat(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12),
                    rs.getString(13), rs.getString(14));
            // Parse 'AD' column because we need them to calculate the expected number.
            String[] sections = info.getAd().split("/");
            info.refCount = Integer.parseInt(sections[0]);
            info.altCount = Integer.parseInt(sections[1]);
            values.add(info);
        }
        // select previousTable.* from previousTable INNER JOIN darnedTable ON previousTable.chrom=darnedTable.chrom and previousTable.pos=darnedTable.coordinate
        // Next, we find the sites existed commonly in the previous table and DARNED table,
        rs = databaseManager.query("select " + previousTable + ".* from " + previousTable + " INNER JOIN " + darnedTable + " ON " + previousTable + "" +
                ".chrom=" + darnedTable + ".chrom and " + previousTable + ".pos=" + darnedTable + ".coordinate and " + darnedTable + ".inchr='A' and ("
                + darnedTable + ".inrna='I' or " + darnedTable + ".inrna='G')");
        while (rs.next()) {
            for (PValueInfo value : values) {
                if (value.getChr().equals(rs.getString(1)) && value.getPos() == rs.getInt(2)) {
                    value.inDarnedDB(true);
                    break;
                }
            }
        }
        return values;
    }

    /**
     * Calculate p-values for all sites from previous filter.
     *
     * @param darnedTable    The DARNED database table name, it is constant.
     * @param fetResultTable The fisher exact test table
     * @param previousTable  Previous table name
     * @return the list which have added level and p-value information from the last list (expected list).
     */
    private List<PValueInfo> executePValueFilter(String darnedTable, String fetResultTable, String previousTable) throws SQLException {
        logger.info("Start executing Fisher Exact Test Filter... {}", Timer.getCurrentTime());
        List<PValueInfo> values = getExpectedInfo(darnedTable, previousTable);
        int knownAlt = 0;
        int knownRef = 0;
        for (PValueInfo value : values) {
            if (value.inDarnedDB) {
                knownAlt += value.altCount;
                knownRef += value.refCount;
            } else {
                knownRef += value.altCount + value.refCount;
            }
        }
        knownAlt = (int) Math.ceil(knownAlt / values.size());
        knownRef = (int) Math.ceil(knownRef / values.size());
        FisherExact fisherExact = new FisherExact(1000);
        DecimalFormat dF = new DecimalFormat("#.###");
        for (PValueInfo pValueInfo : values) {
            int alt = pValueInfo.altCount;
            int ref = pValueInfo.refCount;
            double pValue = fisherExact.getTwoTailedP(ref, alt, knownRef, knownAlt);
            double level = (double) alt / (alt + ref);
            pValueInfo.setPValue(pValue);
            pValueInfo.setLevel(level);
            databaseManager.executeSQL("insert into " + fetResultTable + "(chrom,pos,id,ref,alt,qual,filter,info,gt,ad,dp,gq,pl,alu,level," +
                    "pvalue) values( " + pValueInfo.toString() + "," + dF.format(level) + "," + pValue + ")");
        }
        logger.info("End executing Fisher Exact Test Filter... {}", Timer.getCurrentTime());
        return values;
    }

    /**
     * Calculate false discovery ratio to fix the p-value result, and then filter out the editing sites which don't meet the threshold.
     *
     * @param darnedTable     The DARNED database table name, it is constant.
     * @param fetResultTable  The fisher exact test table
     * @param previousTable   Previous table name
     * @param rScript         The R script path, which is used to calculate FDR.
     * @param pvalueThreshold The threshold of p-value
     * @param fdrThreshold    The threshold of FDR
     */
    public void executeFDRFilter(String darnedTable, String fetResultTable, String previousTable, String rScript, double pvalueThreshold, double fdrThreshold) throws SQLException {
        logger.info("Start executing FDRFilter... {}", Timer.getCurrentTime());
        RCaller caller = new RCaller();
        if (rScript.trim().toLowerCase().contains("script")) {
            caller.setRscriptExecutable(rScript);
        } else {
            caller.setRExecutable(rScript);
        }
        RCode code = new RCode();
        List<PValueInfo> pValueList = executePValueFilter(darnedTable, fetResultTable, previousTable);
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
            databaseManager.executeSQL("update " + fetResultTable + " set fdr=" + results[i] + " where chrom='" + pValueList.get(i).getChr() + "' " +
                    "and pos=" + pValueList.get(i).getPos());
        }
        databaseManager.commit();
        databaseManager.setAutoCommit(true);
        // Filter the sites.
        databaseManager.executeSQL("delete from " + fetResultTable + " where pvalue > " + pvalueThreshold + " || fdr > " + fdrThreshold);
        logger.info("End executing FDRFilter... {}", Timer.getCurrentTime());
    }

    /**
     * A bean only for calculating p-value.
     */
    private class PValueInfo extends SiteBean {
        public boolean inDarnedDB = false;
        public int refCount = 0;
        public int altCount = 0;

        public PValueInfo(String chr, int pos, String id, char ref, char alt, float qual, String filter, String info, String gt, String ad, String dp, String gq,
                          String pl, String alu) {
            super(chr, pos, id, ref, alt, qual, filter, info, gt, ad, dp, gq, pl, alu);
        }

        public void inDarnedDB(boolean inDarnedDB) {
            this.inDarnedDB = inDarnedDB;
        }

        @Override
        public String toString() {

            return "'" + getChr() + "'," + getPos() + ",'" + getId() + "','" + getRef() + "','" + getAlt() + "'," + getQual() + ",'" + getFilter() + "'," +
                    "'" + getInfo() + "','" + getGt() + "','" + getAd() + "','" + getDp() + "','" + getGq() + "','" + getPl() + "','" + getIsAlu() + "'";
        }
    }
}
