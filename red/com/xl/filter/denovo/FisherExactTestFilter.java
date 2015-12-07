/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 * 
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.filter.denovo;

import com.xl.database.DatabaseManager;
import com.xl.database.Query;
import com.xl.database.TableCreator;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteBean;
import com.xl.filter.Filter;
import com.xl.utils.EmptyChecker;
import com.xl.utils.NegativeType;
import com.xl.utils.Timer;
import net.sf.snver.pileup.util.math.FisherExact;
import rcaller.RCaller;
import rcaller.RCode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * The Class FisherExactTestFilter is a statistical filter to reduce the errors in detecting RNA editing sites caused by
 * technical artifacts (e.g., sequencing errors).
 */
public class FisherExactTestFilter implements Filter {

    public static final String PARAMS_STRING_R_SCRIPT_PATH = "rscript";
    public static final String PARAMS_STRING_P_VALUE_THRESHOLD = "pvalue";
    public static final String PARAMS_STRING_FDR_THRESHOLD = "fdr";
    public static final String PARAMS_STRING_EDITING_TYPE = "editingtype";
    private boolean[] darnedInfos;
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Calculate expected count of reference base and alternative base from DARNED database.
     *
     * @param refTable Previous table name
     * @return a list that contains all information from a given table.
     */
    private List<SiteBean> getExpectedInfo(String refTable, String editingType) {
        String knownRnaEditingTable = DatabaseManager.KNOWN_RNA_EDITING_TABLE_NAME;
        try {
            char[] editingTypes = editingType.toCharArray();
            char[] negativeEditingTypes = NegativeType.getNegativeStrandEditingType(editingType).toCharArray();

            Vector<SiteBean> siteBeans = Query.queryAllEditingInfo(refTable, null,
                "(ref=? and alt=? and strand=?) or (ref=? and alt=? and strand=?)", new String[] { editingTypes[0] + "",
                    editingTypes[1] + "", "+", negativeEditingTypes[0] + "", negativeEditingTypes[1] + "", "-" });
            darnedInfos = new boolean[siteBeans.size()];
            Arrays.fill(darnedInfos, false);

            if (EmptyChecker.isEmptyList(siteBeans)) {
                return siteBeans;
            }

            StringBuilder stringBuilder = new StringBuilder("select ");
            stringBuilder.append(refTable);
            stringBuilder.append(".* from ");
            stringBuilder.append(refTable);
            stringBuilder.append(" INNER JOIN ");
            stringBuilder.append(knownRnaEditingTable);
            stringBuilder.append(" ON ");
            stringBuilder.append(refTable).append(".chrom=").append(knownRnaEditingTable).append(".chrom AND ");
            stringBuilder.append(refTable).append(".pos=").append(knownRnaEditingTable).append(".pos AND ");
            stringBuilder.append("(").append(knownRnaEditingTable).append(".ref='").append(editingTypes[0])
                .append("' AND ").append(knownRnaEditingTable).append(".alt='").append(editingTypes[1]).append("')");
            // select refTable.* from refTable INNER JOIN pvalueTable ON refTable.chrom=pvalueTable.chrom and
            // refTable.pos=pvalueTable.coordinate
            ResultSet rs = databaseManager.query(stringBuilder.toString());
            while (rs.next()) {
                for (int i = 0, len = siteBeans.size(); i < len; i++) {
                    SiteBean info = siteBeans.get(i);
                    if (info.getChr().equals(rs.getString(1)) && info.getPos() == rs.getInt(2)) {
                        darnedInfos[i] = true;
                        break;
                    }
                }

            }
            return siteBeans;

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<SiteBean>();
        }
    }

    /**
     * Calculate p-values for all sites from previous filter.
     *
     * @param previousTable Previous table name
     * @return the list which have added level and p-value information from the last list (expected list).
     */
    private List<SiteBean> executeFETFilter(String previousTable, String refAlt) {
        logger.info("Start performing Fisher's Exact Test Filter...\t" + Timer.getCurrentTime());
        List<SiteBean> siteBeans = getExpectedInfo(previousTable, refAlt);
        if (EmptyChecker.isEmptyList(siteBeans)) {
            logger.warn("Empty list of site beans");
            return siteBeans;
        }
        int knownAlt = 0;
        int knownRef = 0;
        for (int i = 0, len = siteBeans.size(); i < len; i++) {
            if (darnedInfos[i]) {
                knownAlt += siteBeans.get(i).getAltCount();
                knownRef += siteBeans.get(i).getRefCount();
            } else {
                knownRef += siteBeans.get(i).getAltCount() + siteBeans.get(i).getRefCount();
            }
        }

        knownAlt = Math.round(knownAlt / siteBeans.size());
        knownRef = Math.round(knownRef / siteBeans.size());
        FisherExact fisherExact = new FisherExact(1000);
        DecimalFormat dF = new DecimalFormat("#.###");
        for (SiteBean siteBean : siteBeans) {
            int altCount = siteBean.getAltCount();
            int refCount = siteBean.getRefCount();
            double pValue = fisherExact.getTwoTailedP(refCount, altCount, knownRef, knownAlt);
            double level = (double) altCount / (altCount + refCount);
            siteBean.setPValue(pValue);
            siteBean.setLevel(Double.parseDouble(dF.format(level)));
        }
        logger.info("End performing Fisher's Exact Test Filter...\t" + Timer.getCurrentTime());
        return siteBeans;
    }

    /**
     * Calculate false discovery ratio to fix the p-value result, and then filter out the editing sites which don't meet
     * the threshold.
     *
     * @param previousTable Previous table name
     * @param currentTable The fisher exact test table
     * @param params The R script path, which is used to calculate FDR;The threshold of p-value;The threshold of FDR;The
     *            Editing type of RNA editing.
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return;
        } else if (params.size() != 4) {
            logger.error(
                "Args " + params.toString() + " for Fisher's Exact Test Filter are incomplete, please have a check");
            throw new IllegalArgumentException(
                "Args " + params.toString() + " for Fisher's Exact Test Filter are incomplete, please have a check");
        }
        String rScript = params.get(PARAMS_STRING_R_SCRIPT_PATH);
        String pvalueThreshold = params.get(PARAMS_STRING_P_VALUE_THRESHOLD);
        String fdrThreshold = params.get(PARAMS_STRING_FDR_THRESHOLD);
        String type = params.get(PARAMS_STRING_EDITING_TYPE);
        logger.info("Start performing False Discovery Rate Filter...\t" + Timer.getCurrentTime());
        RCaller caller = new RCaller();
        if (rScript.trim().toLowerCase().contains("script")) {
            caller.setRscriptExecutable(rScript);
        } else {
            caller.setRExecutable(rScript);
        }
        RCode code = new RCode();
        List<SiteBean> siteBeans;
        if (type.equalsIgnoreCase("all")) {
            String[] editingTypes =
                new String[] { "AG", "AC", "AT", "CG", "CT", "CA", "GA", "GC", "GT", "TC", "TG", "TA" };
            siteBeans = new ArrayList<SiteBean>();
            for (String editingType : editingTypes) {
                List<SiteBean> valueInfo = executeFETFilter(previousTable, editingType);
                siteBeans.addAll(valueInfo);
            }
        } else {
            siteBeans = executeFETFilter(previousTable, type);
        }
        double[] pValueArray = new double[siteBeans.size()];
        for (int i = 0, len = siteBeans.size(); i < len; i++) {
            pValueArray[i] = siteBeans.get(i).getPvalue();
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
        for (int i = 0; i < results.length; i++) {
            siteBeans.get(i).setFdr(results[i]);
        }
        caller.deleteTempFiles();
        databaseManager.setAutoCommit(false);
        DatabaseManager.getInstance().deleteTable(currentTable + "_raw");
        TableCreator.createFilterTable(previousTable, currentTable + "_raw");
        try {
            int count = 0;
            for (SiteBean site : siteBeans) {
                if (site.getPvalue() >= 0 && site.getPvalue() <= 1 && site.getFdr() >= 0 && site.getFdr() <= 1) {
                    databaseManager.insertSiteBean(currentTable + "_raw", site);
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0) {
                        databaseManager.commit();
                    }
                } else {
                    logger.info(site.getPvalue() + " " + site.getFdr());
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
            databaseManager.executeSQL("insert into " + currentTable + " select * from " + currentTable
                + "_raw where (p_value <= " + pvalueThreshold + ") and (fdr <= " + fdrThreshold + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info("Start performing False Discovery Rate Filter...\t" + Timer.getCurrentTime());
    }

    @Override
    public String getName() {
        return DatabaseManager.FET_FILTER_RESULT_TABLE_NAME;
    }
}
