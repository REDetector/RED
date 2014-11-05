package com.dw.dbutils;

import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class Query {
    private static DatabaseManager databaseManager = DatabaseManager.getInstance();

    public static Vector<Site> queryAllEditingSites(String tableName) {
        Vector<Site> siteVector = new Vector<Site>();
        ResultSet rs = databaseManager.query(tableName, "chrom, pos, ref, alt", "1");
        try {
            while (rs.next()) {
                Site p = new Site(rs.getString(1), rs.getInt(2), rs.getString(3).charAt(0), rs.getString(4).charAt(0));
                siteVector.add(p);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return siteVector;
    }

    public static Vector<SiteBean> queryAllEditingInfo(String tableName) {
        Vector<SiteBean> siteBeans = new Vector<SiteBean>();
        ResultSet rs = databaseManager.query(tableName, "*", "1");
        try {
            while (rs.next()) {
                SiteBean p = new SiteBean(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4).charAt(0), rs.getString(5).charAt(0),
                        rs.getFloat(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12),
                        rs.getString(13), rs.getString(14));
                if (tableName.equals(DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME)) {
                    p.setLevel(rs.getDouble(15));
                    p.setPValue(rs.getDouble(16));
                    p.setFdr(rs.getDouble(17));
                }
                siteBeans.add(p);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return siteBeans;
    }

    public static Site queryEditingSite(String tableName, String chrom, int pos) {
        ResultSet rs = databaseManager.query(tableName, " chrom, pos , ref, alt ", " chrom=" + chrom + " and pos='" + pos + "' ");
        try {
            if (rs.next())
                return new Site(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0], rs.getString(4).toCharArray()[0]);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static Vector<Site> queryEditingSitesForChr(String tableName, String chrom) {
        Vector<Site> siteVector = new Vector<Site>();
        ResultSet rs = databaseManager.query(tableName, " chrom, pos , ref, alt ", " chrom=" + chrom + " ");
        try {
            while (rs.next()) {
                Site p = new Site(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0], rs.getString(4).toCharArray()[0]);
                siteVector.add(p);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return siteVector;
    }

    public static int queryMaxDepthFromVCFFile(String tableName) {
        ResultSet rs = databaseManager.query(tableName, "max(DP)", "1");
        try {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }


}
