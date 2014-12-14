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
import com.xl.filter.denovo.EditingTypeFilter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.util.Vector;

/**
 * The ValuesFilter filters sites based on their associated values from quantiation.  Each site is filtered independently of all other sites.
 */
public class EditingTypeFilterPanel extends AbstractSiteFilter {

    private JComboBox refBase = null;
    private JComboBox altBase = null;
    private SpecificFilterOptionPanel optionsPanel = new SpecificFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param dataStore The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public EditingTypeFilterPanel(DataStore dataStore) throws REDException {
        super(dataStore);
    }

    @Override
    public String description() {
        return "Filter RNA editing sites by editing type,such as 'A'->'G','C'->'T',etc.";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA editing sites by RNA editing type, please wait...", 0, 0);

        String refBaseString = refBase.getSelectedItem().toString();
        String altBaseString = altBase.getSelectedItem().toString();
        String linearTableName = currentSample + "_" + parentList.getFilterName() + "_" + DatabaseManager
                .EDITING_TYPE_FILTER_RESULT_TABLE_NAME + "_" + refBaseString + "_" + altBaseString;
        TableCreator.createFilterTable(linearTableName);
        EditingTypeFilter editingTypeFilter = new EditingTypeFilter(databaseManager);
        editingTypeFilter.executeEditingTypeFilter(linearTableName, parentList.getTableName(), refBaseString, altBaseString);
        DatabaseManager.getInstance().distinctTable(linearTableName);

        Vector<Site> sites = Query.queryAllEditingSites(linearTableName);
        SiteList newList = new SiteList(parentList, listName(), DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME, linearTableName, description());
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
        return "Editing Type Filter";
    }

    @Override
    protected String listName() {
        return "Focus on " + refBase.getSelectedItem() + " to " + altBase.getSelectedItem();
    }

    /**
     * The ValuesFilterOptionPanel.
     */
    private class SpecificFilterOptionPanel extends AbstractOptionPanel {

        /**
         * Instantiates a new values filter option panel.
         */
        public SpecificFilterOptionPanel() {
            super(dataStore);
        }

        @Override
        protected boolean hasChoicePanel() {
            return true;
        }

        @Override
        protected JPanel getChoicePanel() {
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));

            JPanel choicePanel2 = new JPanel();
            choicePanel2.add(new JLabel("Focus on editing type from"));
            choicePanel.add(choicePanel2);

            JPanel choicePanel3 = new JPanel();
            choicePanel3.add(new JLabel("reference base "));
            refBase = new JComboBox(new Character[]{'A', 'G', 'C', 'T'});
            refBase.setSelectedItem('A');
            refBase.setActionCommand("ref");
            choicePanel3.add(refBase);
            choicePanel.add(choicePanel3);

            JPanel choicePanel4 = new JPanel();
            choicePanel4.add(new JLabel("to alternative base "));
            altBase = new JComboBox(new Character[]{'A', 'G', 'C', 'T'});
            altBase.setSelectedItem('G');
            altBase.setActionCommand("alt");
            choicePanel4.add(altBase);
            choicePanel.add(choicePanel4);
            return choicePanel;
        }

        @Override
        protected String getPanelDescription() {
            return "Mostly, we focus on A->G change since over 95% RNA editing sites are of A->G. If 'A' in reference base and 'G' in alternative base are " +
                    "chosen (default option), the sites of non A->G change will be filtered.";
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