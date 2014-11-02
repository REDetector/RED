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
import com.dw.dnarna.DNARNAFilter;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.REDException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.Vector;

/**
 * The ValuesFilter filters sites based on their associated values
 * from quantiation.  Each site is filtered independently of all
 * other sites.
 */
public class DNARNAFilterMenu extends AbstractSiteFilter {

    private DNARNAFilterOptionPanel optionsPanel = new DNARNAFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public DNARNAFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter RNA-editing sites by comparing RNA and DNA.";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA-editing sites by DNA/RNA filter, please wait...", 0, 0);
        DNARNAFilter dnaRnaFilter = new DNARNAFilter(databaseManager);
        dnaRnaFilter.establishDnaRnaTable(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);
        dnaRnaFilter.executeDnaRnaFilter(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME, DatabaseManager.DNA_VCF_RESULT_TABLE_NAME,
                parentTable);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);

        Vector<Site> sites = Query.queryAllEditingSites(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);
        SiteList newList = new SiteList(parentList, DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME, description(),
                DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);
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
        return "DNA-RNA Filter";
    }

    @Override
    protected String listName() {
        return "RNA-DNA filter";
    }


    private class DNARNAFilterOptionPanel extends AbstractOptionPanel {

        /**
         * Instantiates a new values filter option panel.
         */
        public DNARNAFilterOptionPanel() {
            super(collection);
        }

        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(DNARNAFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
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
            return "RNA-seq variants where its counterparts in genomic DNA is not reference homozygote (e.g., AA) would be excluded if DNA sequencing data is" +
                    " available.";
        }
    }
}
