package com.dw.dnarna;

/**
 * LLR used for detecting editing sites
 */

import com.dw.dbutils.DatabaseManager;
import com.xl.datatypes.sites.SiteBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LikelihoodRatioFilter {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatabaseManager databaseManager;

    public LikelihoodRatioFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void establishLLRResultTable(String llrResultTable) {
        databaseManager.deleteTable(llrResultTable);
        databaseManager.createFilterTable(llrResultTable);
    }

    public void executeLLRFilter(String llrResultTable, String dnaVcfTable, String refTable, double threshold) {
        try {
            System.out.println("Start executing LikelihoodRatioFilter..." + df.format(new Date()));

            ResultSet rs = databaseManager.query(refTable + "," + dnaVcfTable, refTable + ".chrom," + refTable + ".pos," + refTable + ".AD," +
                    dnaVcfTable + ".qual", refTable + ".chrom=" + dnaVcfTable + ".chrom and " + refTable + ".pos=" + dnaVcfTable + ".pos");
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
                    if (judge >= threshold) {
                        databaseManager.executeSQL("insert into " + llrResultTable + " select * from " + refTable + " where chrom='" + siteBean.getChr() +
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("End executing LikelihoodRatioFilter..." + df.format(new Date()));

    }

}
