package com.dw.publicaffairs;

import com.xl.datatypes.probes.Probe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class Query {
    public static Vector<Probe> queryAllEditingSites(String tableName) {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        Vector<Probe> probeVector = new Vector<Probe>();
        ResultSet rs = databaseManager.query(tableName, " chrom, pos,alt ", " 1 ");
        try {
            while (rs.next()) {
                Probe p = new Probe(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0]);
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

        ResultSet rs = databaseManager.query(tableName, " chrom, pos ,alt ", " chrom=" + chrom + " and pos='" + pos + "' ");
        try {
            while (rs.next()) {
                return new Probe(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0]);
            }
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
        ResultSet rs = databaseManager.query(tableName, " chrom, pos ,alt ", " chrom=" + chrom + " ");
        try {
            while (rs.next()) {
                Probe p = new Probe(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0]);
                probeVector.add(p);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return probeVector;
    }


}
