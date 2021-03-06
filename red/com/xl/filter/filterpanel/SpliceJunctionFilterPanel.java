/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 * 
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.xl.filter.filterpanel;

import com.xl.database.DatabaseManager;
import com.xl.database.Query;
import com.xl.database.TableCreator;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.RedException;
import com.xl.filter.Filter;
import com.xl.filter.denovo.SpliceJunctionFilter;
import com.xl.main.RedApplication;
import com.xl.utils.ui.OptionDialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * The Class SpliceJunctionFilterPanel is a rule-based filter panel to provide some parameters to be set as user's
 * preference if there is any choice.
 */
public class SpliceJunctionFilterPanel extends AbstractFilterPanel {
    private final Logger logger = LoggerFactory.getLogger(SpliceJunctionFilterPanel.class);
    /**
     * The threshold of splice junction.
     */
    private int sjThreshold = 2;
    /**
     * The text field of threshold of splice junction.
     */
    private JTextField edgeField = null;
    /**
     * The splice junction filter option panel.
     */
    private SpliceJunctionFilterOptionPanel optionsPanel = new SpliceJunctionFilterOptionPanel();

    /**
     * Instantiates a new splice junction filter.
     *
     * @param dataStore The data store to filter.
     */
    public SpliceJunctionFilterPanel(DataStore dataStore) throws RedException {
        super(dataStore);
    }

    @Override
    protected String listName() {
        return "Splice Junction: " + sjThreshold;
    }

    @Override
    protected void generateSiteList() throws SQLException {
        progressUpdated("Filtering RNA editing sites by splice-junction, please wait...", 0, 0);
        logger.info("Filtering RNA editing sites by splice-junction.");
        String linearTableName =
            currentSample + "_" + parentList.getFilterName() + "_"
                + DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME + "_" + sjThreshold;

        if (databaseManager.existTable(linearTableName)) {
            logger.info("Table has been existed!");
            int answer = OptionDialogUtils.showTableExistDialog(RedApplication.getInstance(), linearTableName);
            if (answer <= 0) {
                databaseManager.deleteTable(linearTableName);
            } else {
                return;
            }

        }
        TableCreator.createFilterTable(parentList.getTableName(), linearTableName);

        Filter filter = new SpliceJunctionFilter();
        Map<String, String> params = new HashMap<String, String>();
        // The first parameter means quality and the second means depth
        params.put(SpliceJunctionFilter.PARAMS_INT_EDGE, sjThreshold + "");
        filter.performFilter(parentList.getTableName(), linearTableName, params);
        DatabaseManager.getInstance().distinctTable(linearTableName);
        Vector<Site> sites = Query.queryAllEditingSites(linearTableName);
        SiteList newList =
            new SiteList(parentList, listName(), DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME,
                linearTableName, description());
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
        return parentList != null && edgeField.getText().length() != 0;
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
        return "Splice-junction Filter";
    }

    @Override
    public String description() {
        return "Filter RNA editing sites by splice-junction.";
    }

    /**
     * The splice junction filter option panel.
     */
    private class SpliceJunctionFilterOptionPanel extends AbstractFilterOptionPanel implements KeyListener {

        /**
         * Instantiates a new splice junction filter option panel.
         */
        public SpliceJunctionFilterOptionPanel() {
            super(dataStore);
        }

        @Override
        public void valueChanged(TreeSelectionEvent tse) {
            Object selectedItem = siteTree.getSelectionPath().getLastPathComponent();
            if (selectedItem instanceof SiteList) {
                parentList = (SiteList) selectedItem;
            }
            optionsChanged();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            int keyChar = e.getKeyChar();
            if (!(keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9)) {
                e.consume();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            JTextField f = (JTextField) e.getSource();
            if (f.getText().length() == 0) {
                edgeField.setText("");
            } else if (f == edgeField) {
                sjThreshold = Integer.parseInt(edgeField.getText());
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
            choicePanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.gridy = 0;
            c.gridx = 0;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Splice-junction length: "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            edgeField = new JTextField(3);
            edgeField.setText(sjThreshold + "");
            edgeField.addKeyListener(this);
            choicePanel.add(edgeField, c);
            return choicePanel;
        }

        @Override
        protected String getPanelDescription() {
            return "Variants that are within +/-k bp (e.g., k = 2) of the splice junction, which are supposed to be unreliable , are excluded;";
        }
    }
}
