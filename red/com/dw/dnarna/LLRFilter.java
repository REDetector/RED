package com.dw.dnarna;

/**
 * LLR used for detecting editing sites
 */

import com.dw.publicaffairs.DatabaseManager;
import com.xl.datatypes.probes.ProbeBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LLRFilter {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatabaseManager databaseManager;

    public LLRFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void establishLLRResultTable(String llrResultTable) {
        databaseManager.deleteTable(llrResultTable);
        databaseManager.createFilterTable(llrResultTable);
    }

    public void executeLLRFilter(String llrResultTable, String dnaVcfTable, String refTable) {
        try {
            System.out.println("Start executing LLRFilter..." + df.format(new Date()));

            ResultSet rs = databaseManager.query(refTable + "," + dnaVcfTable, refTable + ".chrom," + refTable + ".pos," + refTable + ".AD," +
                    dnaVcfTable + ".qual", refTable + ".chrom=" + dnaVcfTable + ".chrom and " + refTable + ".pos=" + dnaVcfTable + ".pos");
            List<ProbeBean> probeBeans = new ArrayList<ProbeBean>();
            while (rs.next()) {
                String chr = rs.getString(1);
                int pos = rs.getInt(2);
                String ad = rs.getString(3);
                float qual = rs.getFloat(4);
                ProbeBean pb = new ProbeBean(chr, pos);
                pb.setAd(ad);
                pb.setQual(qual);
                probeBeans.add(pb);
            }
            databaseManager.setAutoCommit(false);
            int count = 0;
            for (ProbeBean probeBean : probeBeans) {
                String[] section = probeBean.getAd().split("/");
                int ref = Integer.parseInt(section[0]);
                int alt = Integer.parseInt(section[1]);
                if (ref + alt > 0) {
                    double f_ml = 1.0 * ref / (ref + alt);
                    double y = Math.pow(f_ml, ref) * Math.pow(1 - f_ml, alt);
                    y = Math.log(y) / Math.log(10.0);
                    double judge = y + probeBean.getQual() / 10.0;
                    if (judge >= 4) {
                        databaseManager.executeSQL("insert into " + llrResultTable + " select * from " + refTable + " where chrom='" + probeBean.getChr() +
                                "' and pos=" + probeBean.getPos());
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
        System.out.println("End executing LLRFilter..." + df.format(new Date()));

    }

}
