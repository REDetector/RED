/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.xl.filter;

import com.dw.dbutils.DatabaseManager;
import com.dw.dbutils.Query;
import com.dw.denovo.KnownSNPFilter;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.REDException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * The ValuesFilter filters sites based on their associated values
 * from quantiation.  Each site is filtered independently of all
 * other sites.
 */
public class KnownSNPFilterMenu extends AbstractSiteFilter {

    private DbSNPFilterOptionPanel optionsPanel = new DbSNPFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public KnownSNPFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter RNA-editing sites by dbSNP database.";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA-editing sites by dbSNP filter, please wait...", 0, 0);
        KnownSNPFilter dbsnpFilter = new KnownSNPFilter(databaseManager);
        dbsnpFilter.establishDbSNPResultTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
//        new Thread(new ThreadCountRow(this,DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME)).start();
        dbsnpFilter.executeDbSNPFilter(DatabaseManager.DBSNP_FILTER_TABLE_NAME, DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME, parentTable);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
        Vector<Site> sites = Query.queryAllEditingSites(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
        SiteList newList = new SiteList(parentList, DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME, description(),
                DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
        int index = 0;
        int sitesLength = sites.size();
        for (Site site : sites) {
            progressUpdated(index++, sitesLength);
            if (cancel) {
                cancel = false;
                progressCancelled();
                return;
            }
            newList.addSite(site);
        }
        filterFinished(newList);
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    public boolean hasOptionsPanel() {
        return true;
    }

    @Override
    public boolean isReady() {
        return stores.length != 0;
    }

    @Override
    public String name() {
        return "Known SNP Filter";
    }

    @Override
    protected String listName() {
        return "Known SNP Filter";
    }


    /**
     * The ValuesFilterOptionPanel.
     */
    private class DbSNPFilterOptionPanel extends AbstractOptionPanel implements KeyListener {

        /**
         * Instantiates a new values filter option panel.
         */
        public DbSNPFilterOptionPanel() {
            super(collection);
        }

        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(KnownSNPFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
            }
            optionsChanged();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            int keyChar = e.getKeyChar();
            if (!(keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9))
                e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            JTextField f = (JTextField) e.getSource();
            if (f.getText().length() == 0) {
                return;
            }
            optionsChanged();
        }

        @Override
        protected boolean hasChoicePanel() {
            return false;
        }

        @Override
        protected JPanel getChoicePanel() {
            return null;
        }

        @Override
        protected String getPanelDescription() {
            return "RNA-seq variants that were known SNPs in DNA level are excluded for eliminating germline variants from dbSNP database " +
                    "(e.g., db-snp_138.hg19.vcf).";
        }
    }
}
