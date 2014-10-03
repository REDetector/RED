package com.dw.denovo;

import com.dw.publicaffairs.DatabaseManager;
import com.xl.datatypes.probes.ProbeBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Basic process for basic filter means we set threshold on quality and depth
 */
public class BasicFilter {
    private DatabaseManager databaseManager;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int count = 0;

    public BasicFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void establishBasicTable(String basicTable) {
        databaseManager.deleteTable(basicTable);
        databaseManager.createFilterTable(basicTable);
    }

    public void executeBasicFilter(String refTable, String basicTable, double quality, int depth) {
        try {
            System.out.println("Start executing basic filter..." + df.format(new Date()));
            ResultSet rs = databaseManager.query(refTable, "chrom,pos,AD", "1");
            List<ProbeBean> probeBeans = new ArrayList<ProbeBean>();
            while (rs.next()) {
                ProbeBean probeBean = new ProbeBean(rs.getString(1), rs.getInt(2));
                probeBean.setAd(rs.getString(3));
                probeBeans.add(probeBean);
            }
            databaseManager.setAutoCommit(false);
            for (ProbeBean probeBean : probeBeans) {
                String[] sections = probeBean.getAd().split("/");
                int ref_n = Integer.parseInt(sections[0]);
                int alt_n = Integer.parseInt(sections[1]);
                if (ref_n + alt_n >= depth) {
                    databaseManager.executeSQL("insert into " + basicTable
                            + " (select * from " + refTable
                            + " where filter='PASS' and pos=" + probeBean.getPos()
                            + " and qual >=" + quality + " and chrom='"
                            + probeBean.getChr() + "')");
                    count++;
                    if (count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
            System.out.println("End executing basic filter..." + df.format(new Date()));
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
