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

package com.xl.database;

import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteBean;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteSet;
import com.xl.exception.REDException;
import com.xl.filter.FilterNameRetriever;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * Class Query provides a simple way to query data from database. It is an adapter between database and RED.
 */
public class Query {
    /**
     * A reference of DatabaseManager.
     */
    private static DatabaseManager databaseManager = DatabaseManager.getInstance();


    /**
     * Query Chromosome, Position, Reference Base and Alternative Base of RNA editing sites from a given table.
     *
     * @param tableName The table name
     * @return A collection which contains the above information.
     */
    public static Vector<Site> queryAllEditingSites(String tableName) {
        Vector<Site> siteVector = new Vector<Site>();
        try {
            ResultSet rs = databaseManager.query(tableName, new String[]{"chrom", "pos", "ref", "alt"}, null, null);
            while (rs.next()) {
                Site p = new Site(rs.getString(1), rs.getInt(2), rs.getString(3).charAt(0), rs.getString(4).charAt(0));
                siteVector.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return siteVector;
    }

    /**
     * Query all information of RNA editing sites from a given table.
     *
     * @param tableName The table name
     * @return A collection which contains all information about a site.
     */
    public static Vector<SiteBean> queryAllEditingInfo(String tableName) {
        Vector<SiteBean> siteBeans = new Vector<SiteBean>();
        try {
            ResultSet rs = databaseManager.query(tableName, null, null, null);
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
            e.printStackTrace();
        }
        return siteBeans;
    }

    /**
     * Query a RNA editing site from a given position.
     *
     * @param tableName The given table name.
     * @param chrom     The given chromosome.
     * @param pos       The given position.
     * @return A Site which contains Chromosome, Position, Reference Base and Alternative Base information.
     */
    public static Site queryEditingSite(String tableName, String chrom, int pos) {
        try {
            ResultSet rs = databaseManager.query(tableName, new String[]{"chrom", "pos", "ref", "alt"}, " chrom=? AND pos=?", new String[]{chrom, pos + ""});
            if (rs.next())
                return new Site(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0], rs.getString(4).toCharArray()[0]);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Query a RNA editing site from a given chromosome.
     *
     * @param tableName The given table name.
     * @param chrom     The given chromosome.
     * @return A Site which contains Chromosome, Position, Reference Base and Alternative Base information.
     */
    public static Vector<Site> queryEditingSitesForChr(String tableName, String chrom) {
        Vector<Site> siteVector = new Vector<Site>();
        try {
            ResultSet rs = databaseManager.query(tableName, new String[]{"chrom", "pos", "ref", "alt"}, " chrom=?", new String[]{chrom});
            while (rs.next()) {
                Site p = new Site(rs.getString(1), rs.getInt(2), rs.getString(3).toCharArray()[0], rs.getString(4).toCharArray()[0]);
                siteVector.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return siteVector;
    }

    /**
     * Retrieve a SiteSet from database by a given sample name. We first query all table names from database and select all tables relative to this sample. Then
     * we initiate the root of this site set model (i.e., the SiteSet) using the sample RNA VCF file. Next, we retrieve the leaves and the leaf params of this
     * tree according to the table name.
     * <p/>
     * For example, there is a sample named BJ22. Tables in database are BJ22_rnavcf, BJ22_rnavcf_etfilter_A_G, BJ22_etfilter_qcfilter_20_6,
     * BJ22_qcfilter_rrfilter. First, we regard BJ22_rnavcf as a root of the tree model, then the link between rnavcf and etfilter can be seen by
     * BJ22_rnavcf_etfilter_A_G table, and its parameter is A->G. So we can retrieve a leaf of the root as 'ALl Sites'->'Focus on A to G'. As a result, we can
     * retrieve all leaves as the following tree model:
     * <p/>
     * BJ22_rnavcf
     * <p/>
     * - Focus on A to G
     * <p/>
     * -- Q >= 20 & DP >= 6
     * <p/>
     * --- Repeat Regions Filter
     *
     * @param sampleName The sample name.
     * @return A Site set which can be regarded as a tree model.
     * @throws REDException If table name has been renamed or deleted in database, and the method can not find them, then throw this exception.
     */
    public synchronized static SiteSet getSiteSetFromDatabase(String sampleName) throws REDException {
        List<String> tableNames = DatabaseManager.getInstance().queryTablesForSample(sampleName);
        //First, we get site set from a RNA VCF file.
        String rnaVcf = null;
        for (String table : tableNames) {
            if (table.endsWith(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME)) {
                rnaVcf = table;
                tableNames.remove(table);
                break;
            }
        }
        SiteSet siteSet = new SiteSet(sampleName, "The original RNA editing sites from RNA VCF file.", queryAllEditingSites(rnaVcf).toArray(new Site[0]));

        //Next, we need to remove DNA VCF file from filter list
        for (String table : tableNames) {
            if (table.endsWith(DatabaseManager.DNA_VCF_RESULT_TABLE_NAME)) {
                tableNames.remove(table);
                break;
            }
        }
        //Next, we need to remove ALU Filter from filter list
        for (String table : tableNames) {
            if (table.endsWith(DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME)) {
                tableNames.remove(table);
                break;
            }
        }

        int n = tableNames.size();
        int k = n;
        int foundFilter = 0;
        //Then we get all site list from the remain table name
        SiteList[] linkages = new SiteList[n + 1];

        linkages[0] = siteSet;

        for (int i = 0; i < n; i++) {
            String tableName = tableNames.get(i);
            String[] sections = tableName.split("_");
            int count = 0;
            String previousFilter = null;
            String currentFilter = null;
            for (String section : sections) {
                if (section.contains(DatabaseManager.FILTER) || section.contains(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME)) {
                    if (count == 0) {
                        previousFilter = section;
                        count++;
                    } else {
                        currentFilter = section;
                    }
                }
            }
            if (previousFilter == null || currentFilter == null) {
                throw new REDException("Unknown filter table:" + tableName);
            }
            String listName = FilterNameRetriever.retrieveParams(currentFilter, sections);

            for (int j = 0; j < k; j++) {
                SiteList linkage = linkages[j];
                if (linkage != null && linkage.getFilterName().equals(previousFilter)) {
                    foundFilter++;
                    linkages[foundFilter] = new SiteList(linkage, listName, tableName, "");
                    Vector<Site> sites = queryAllEditingSites(tableName);
                    for (Site site : sites) {
                        linkages[foundFilter].addSite(site);
                    }
                    tableNames.remove(tableName);
                    for (SiteList link : linkages) {
                        if (link == null) {
                            i = -1;
                            j = k;
                            n = tableNames.size();
                            break;
                        }
                    }
                }
            }
        }
        return siteSet;
    }
}
