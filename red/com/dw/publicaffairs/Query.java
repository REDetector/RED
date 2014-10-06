package com.dw.publicaffairs;

import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class Query {
    public static Vector<Probe> queryAllEditingSites(String tableName) {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        Vector<Probe> probeVector = new Vector<Probe>();
        ResultSet rs = databaseManager.query(tableName, "chrom, pos, ref, alt", "1");
        try {
            while (rs.next()) {
                Probe p = new Probe(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0], rs.getString(4).toCharArray()[0]);
                probeVector.add(p);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return probeVector;
    }

    public static Vector<ProbeBean> queryAllEditingInfo(String tableName) {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        Vector<ProbeBean> probeVector = new Vector<ProbeBean>();
        ResultSet rs = databaseManager.query(tableName, "*", "1");
        try {
            while (rs.next()) {
                ProbeBean p = new ProbeBean(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4).charAt(0), rs.getString(5).charAt(0),
                        rs.getFloat(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12),
                        rs.getString(13));
                if (tableName.equals(DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME)) {
                    p.setLevel(rs.getDouble(14));
                    p.setPValue(rs.getDouble(15));
                    p.setFdr(rs.getDouble(16));
                }
                probeVector.add(p);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return probeVector;
    }

    public static Probe queryEditingSite(String tableName, String chrom, int pos) {
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        ResultSet rs = databaseManager.query(tableName, " chrom, pos , ref, alt ", " chrom=" + chrom + " and pos='" + pos + "' ");
        try {
            if (rs.next())
                return new Probe(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0], rs.getString(4).toCharArray()[0]);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static Vector<Probe> queryEditingSitesForChr(String tableName, String chrom) {
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        Vector<Probe> probeVector = new Vector<Probe>();
        ResultSet rs = databaseManager.query(tableName, " chrom, pos , ref, alt ", " chrom=" + chrom + " ");
        try {
            while (rs.next()) {
                Probe p = new Probe(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0], rs.getString(4).toCharArray()[0]);
                probeVector.add(p);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return probeVector;
    }


}
