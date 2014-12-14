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
import com.xl.filter.denovo.RepeatRegionsFilter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.util.Vector;

/**
 * The ValuesFilter filters sites based on their associated values from quantiation.  Each site is filtered independently of all other sites.
 */
public class RepeatRegionsFilterPanel extends AbstractSiteFilter {

    private RepeatFilterOptionPanel optionsPanel = new RepeatFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param dataStore The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public RepeatRegionsFilterPanel(DataStore dataStore) throws REDException {
        super(dataStore);
    }

    @Override
    public String description() {
        return "Filter editing bases by repeatmasker database.";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA editing sites by repeatmasker database, please wait...", 0, 0);
        String linearRepeatTableName = currentSample + "_" + parentList.getFilterName() + "_" + DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME;
        String linearAluTableName = currentSample + "_" + parentList.getFilterName() + "_" + DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME;
        TableCreator.createFilterTable(linearRepeatTableName);
        TableCreator.createFilterTable(linearAluTableName);
        RepeatRegionsFilter rf = new RepeatRegionsFilter(databaseManager);
        rf.executeRepeatFilter(DatabaseManager.REPEAT_MASKER_TABLE_NAME, linearRepeatTableName, linearAluTableName, parentList.getTableName());
        DatabaseManager.getInstance().distinctTable(linearRepeatTableName);

        Vector<Site> sites = Query.queryAllEditingSites(linearRepeatTableName);
        SiteList newList = new SiteList(parentList, listName(), DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME, linearRepeatTableName, description());
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
        return "Repeat Regions Filter";
    }

    @Override
    protected String listName() {
        return "Repeat Regions Filter";
    }


    /**
     * The ValuesFilterOptionPanel.
     */
    private class RepeatFilterOptionPanel extends AbstractOptionPanel {

        /**
         * Instantiates a new values filter option panel.
         */
        public RepeatFilterOptionPanel() {
            super(dataStore);
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
            return "Variants that are within repeat regions are excluded. However, sites in SINE/Alu regions are remained since A->I RNA editing is " +
                    "pervasive in Alu repeats and it has been implicated in non-Alu RNA editing sites.";
        }
    }
}
