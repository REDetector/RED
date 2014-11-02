package com.xl.filter;

import com.dw.dbutils.DatabaseManager;
import com.dw.dbutils.Query;
import com.dw.denovo.EditingTypeFilter;
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
public class EditingTypeFilterMenu extends AbstractSiteFilter {

    private JComboBox refBase = null;
    private JComboBox altBase = null;
    private SpecificFilterOptionPanel optionsPanel = new SpecificFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public EditingTypeFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter RNA-editing sites by editing type,such as 'A'->'G','C'->'T',etc.";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA-editing sites by editing type, please wait...", 0, 0);
        EditingTypeFilter editingTypeFilter = new EditingTypeFilter(databaseManager);
        editingTypeFilter.establishSpecificTable(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
        editingTypeFilter.executeSpecificFilter(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME, parentTable,
                refBase.getSelectedItem().toString(), altBase.getSelectedItem().toString());
        DatabaseManager.getInstance().distinctTable(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
        Vector<Site> sites = Query.queryAllEditingSites(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
        SiteList newList = new SiteList(parentList, DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME, description(),
                DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
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
            super(collection);
        }

        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(QualityControlFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
            }
            optionsChanged();
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
    }
}