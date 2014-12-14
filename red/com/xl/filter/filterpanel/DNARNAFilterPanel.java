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
package com.xl.filter.filterpanel;

import com.xl.database.DatabaseManager;
import com.xl.database.Query;
import com.xl.database.TableCreator;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.REDException;
import com.xl.filter.dnarna.DNARNAFilter;
import com.xl.preferences.DatabasePreferences;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.util.Vector;

/**
 * The ValuesFilter filters sites based on their associated values from quantiation.  Each site is filtered independently of all other sites.
 */
public class DNARNAFilterPanel extends AbstractSiteFilter {

    private DNARNAFilterOptionPanel optionsPanel = new DNARNAFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param dataStore The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public DNARNAFilterPanel(DataStore dataStore) throws REDException {
        super(dataStore);
    }

    @Override
    public String description() {
        return "Filter RNA editing sites by comparing RNA and DNA.";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA editing sites by DNA-RNA filter, please wait...", 0, 0);
        String linearTableName = currentSample + "_" + parentList.getFilterName() + "_" + DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME;
        TableCreator.createFilterTable(linearTableName);
        DNARNAFilter dnaRnaFilter = new DNARNAFilter(databaseManager);
        String sampleName = DatabasePreferences.getInstance().getCurrentSample();
        dnaRnaFilter.executeDnaRnaFilter(linearTableName, sampleName + "_" + DatabaseManager.DNA_VCF_RESULT_TABLE_NAME, parentList.getTableName());
        DatabaseManager.getInstance().distinctTable(linearTableName);

        Vector<Site> sites = Query.queryAllEditingSites(linearTableName);
        SiteList newList = new SiteList(parentList, listName(), DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME, linearTableName, description());
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
        return parentList != null;
    }

    @Override
    public String name() {
        return "DNA-RNA Filter";
    }

    @Override
    protected String listName() {
        return "DNA-RNA Filter";
    }


    private class DNARNAFilterOptionPanel extends AbstractOptionPanel {

        /**
         * Instantiates a new values filter option panel.
         */
        public DNARNAFilterOptionPanel() {
            super(dataStore);
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

        @Override
        public void valueChanged(TreeSelectionEvent tse) {
            System.out.println(QualityControlFilterPanel.class.getName() + ":valueChanged()");
            Object selectedItem = siteTree.getSelectionPath().getLastPathComponent();
            if (selectedItem instanceof SiteList) {
                parentList = (SiteList) selectedItem;
            }
            optionsChanged();
        }
    }
}
