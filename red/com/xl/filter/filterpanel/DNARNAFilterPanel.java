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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.sql.SQLException;
import java.util.Vector;

/**
 * The Class DNARNAFilterPanel is a rule-based filter panel to provide some parameters to be set as user's preference if there is any choice.
 */
public class DNARNAFilterPanel extends AbstractSiteFilter {
    private final Logger logger = LoggerFactory.getLogger(DNARNAFilterPanel.class);
    /**
     * The DNA-RNA filter option panel.
     */
    private DNARNAFilterOptionPanel optionsPanel = new DNARNAFilterOptionPanel();

    /**
     * Instantiates a new DNA-RNA filter.
     *
     * @param dataStore The data store to filter
     */
    public DNARNAFilterPanel(DataStore dataStore) throws REDException {
        super(dataStore);
    }

    @Override
    protected String listName() {
        return "DNA-RNA Filter";
    }

    @Override
    protected void generateSiteList() throws SQLException {
        progressUpdated("Filtering RNA editing sites by DNA-RNA filter, please wait...", 0, 0);
        logger.info("Filtering RNA editing sites by DNA-RNA filter.");
        String linearTableName = currentSample + "_" + parentList.getFilterName() + "_" + DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME;
        if (!TableCreator.createFilterTable(parentList.getTableName(), linearTableName)) {
            progressCancelled();
            return;
        }
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
    public boolean isReady() {
        return parentList != null;
    }

    @Override
    public boolean hasOptionsPanel() {
        return true;
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    public String name() {
        return "DNA-RNA Filter";
    }

    @Override
    public String description() {
        return "Filter RNA editing sites by comparing RNA and DNA.";
    }

    /**
     * The DNA-RNA filter option panel.
     */
    private class DNARNAFilterOptionPanel extends AbstractOptionPanel {

        /**
         * Instantiates a new DNA-RNA filter option panel.
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
            Object selectedItem = siteTree.getSelectionPath().getLastPathComponent();
            if (selectedItem instanceof SiteList) {
                parentList = (SiteList) selectedItem;
            }
            optionsChanged();
        }
    }
}