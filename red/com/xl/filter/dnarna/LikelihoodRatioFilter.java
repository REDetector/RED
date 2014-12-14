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

package com.xl.filter.dnarna;

import com.xl.database.DatabaseManager;
import com.xl.datatypes.sites.SiteBean;
import com.xl.utils.Timer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class LikelihoodRatioFilter is a statistical filter to reduce the errors in detecting RNA editing sites caused by technical artifacts (e.g., sequencing
 * errors).
 */
public class LikelihoodRatioFilter {
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager;

    /**
     * Initiate a new likelihood ratio filter.
     *
     * @param databaseManager the database manager
     */
    public LikelihoodRatioFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Perform Likelihood Ratio test filter as user's preference.
     * <p/>
     * The log likelihood ratio (LLR) was defined as: LLR = log10[maxfP(Djf)=P(Djf = 1)]. Variation sites with LLR < m were excluded, where m is self-defined
     * and m = 4 is suggested. The LLR >= 4 indicated that the probability of editing event happened is 10e4 times more than that of non-editing in reality.
     *
     * @param llrResultTable The result table
     * @param dnaVcfTable    The DNA VCF table
     * @param previousTable  The previous table
     * @param llrThreshold   The likelihood ratio threshold
     */
    public void executeLLRFilter(String llrResultTable, String dnaVcfTable, String previousTable, double llrThreshold) {
        try {
            System.out.println("Start executing Likelihood Ratio Filter..." + Timer.getCurrentTime());
//            ResultSet rs = databaseManager.query(previousTable + "," + dnaVcfTable, new String[]{previousTable + ".chrom", previousTable + ".pos", previousTable + ".AD",
//                    dnaVcfTable + ".qual"}, previousTable + ".chrom=? AND " + previousTable + ".pos=?", new String[]{dnaVcfTable + ".chrom", dnaVcfTable + ".pos"});
            ResultSet rs = databaseManager.query("select " + previousTable + ".chrom," + previousTable + ".pos," + previousTable + ".AD," +
                    "" + dnaVcfTable + ".qual from " + previousTable + "," + dnaVcfTable + " WHERE " + previousTable + ".chrom=" + dnaVcfTable + ".chrom AND " +
                    previousTable + ".pos=" + dnaVcfTable + ".pos");
            List<SiteBean> siteBeans = new ArrayList<SiteBean>();
            while (rs.next()) {
                String chr = rs.getString(1);
                int pos = rs.getInt(2);
                String ad = rs.getString(3);
                float qual = rs.getFloat(4);
                SiteBean pb = new SiteBean(chr, pos);
                pb.setAd(ad);
                pb.setQual(qual);
                siteBeans.add(pb);
            }
            databaseManager.setAutoCommit(false);
            int count = 0;
            for (SiteBean siteBean : siteBeans) {
                String[] section = siteBean.getAd().split("/");
                int ref = Integer.parseInt(section[0]);
                int alt = Integer.parseInt(section[1]);
                if (ref + alt > 0) {
                    double f_ml = 1.0 * ref / (ref + alt);
                    double y = Math.pow(f_ml, ref) * Math.pow(1 - f_ml, alt);
                    y = Math.log(y) / Math.log(10.0);
                    double judge = y + siteBean.getQual() / 10.0;
                    if (judge >= llrThreshold) {
                        databaseManager.insert("insert into " + llrResultTable + " select * from " + previousTable + " where chrom='" + siteBean.getChr() +
                                "' and pos=" + siteBean.getPos());
                        if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0) {
                            databaseManager.commit();
                        }
                    }
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("End executing Likelihood Ratio Filter..." + Timer.getCurrentTime());

    }

}
