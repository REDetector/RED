package com.dw.denovo;

import com.dw.dbutils.DatabaseManager;
import com.xl.datatypes.sites.SiteBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Basic process for QC filter means we set threshold on quality and depth
 */
public class QualityControlFilter {
    private DatabaseManager databaseManager;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int count = 0;

    public QualityControlFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void establishQCTable(String qcTable) {
        databaseManager.deleteTable(qcTable);
        databaseManager.createFilterTable(qcTable);
    }

    public void executeQCFilter(String refTable, String basicTable, double quality, int depth) {
        try {
            System.out.println("Start executing QC filter..." + df.format(new Date()));
            ResultSet rs = databaseManager.query(refTable, "chrom,pos,AD", "1");
            List<SiteBean> siteBeans = new ArrayList<SiteBean>();
            while (rs.next()) {
                SiteBean siteBean = new SiteBean(rs.getString(1), rs.getInt(2));
                siteBean.setAd(rs.getString(3));
                siteBeans.add(siteBean);
            }
            databaseManager.setAutoCommit(false);
            for (SiteBean siteBean : siteBeans) {
                String[] sections = siteBean.getAd().split("/");
                int ref_n = Integer.parseInt(sections[0]);
                int alt_n = Integer.parseInt(sections[1]);
                if (ref_n + alt_n >= depth) {
                    databaseManager.executeSQL("insert into " + basicTable + " (select * from " + refTable + " where filter='PASS' and pos=" + siteBean
                            .getPos() + " and qual >=" + quality + " and chrom='" + siteBean.getChr() + "')");
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
            System.out.println("End executing QC filter..." + df.format(new Date()));
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
