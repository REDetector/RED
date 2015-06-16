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
import com.xl.filter.denovo.QualityControlFilter;
import com.xl.utils.ui.OptionDialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.Vector;

/**
 * The Class QualityControlFilterPanel is a rule-based filter panel to provide some parameters to be set as user's preference if there is any choice.
 */
public class QualityControlFilterPanel extends AbstractSiteFilter {
    private final Logger logger = LoggerFactory.getLogger(QualityControlFilterPanel.class);
    /**
     * The threshold of quality.
     */
    private int qualityThreshold = 20;
    /**
     * The threshold of coverage of depth.
     */
    private int depthThreshold = 6;
    /**
     * The text field of threshold of quality.
     */
    private JTextField qualityField;
    /**
     * The text field of threshold of coverage of depth.
     */
    private JTextField depthField;
    /**
     * The quality control filter option panel.
     */
    private QCFilterOptionPanel optionsPanel = new QCFilterOptionPanel();

    /**
     * Instantiates a new quality control filter.
     *
     * @param dataStore The data store to filter.
     */
    public QualityControlFilterPanel(DataStore dataStore) throws REDException {
        super(dataStore);
    }

    @Override
    protected String listName() {
        return "Q>=" + qualityThreshold + " & DP>=" + depthThreshold;
    }

    @Override
    protected void generateSiteList() throws SQLException {
        if (!isValidInput()) {
            OptionDialogUtils.showWarningDialog(getOptionsPanel(), "The quality or depth of coverage is invalid, which must be between 0-255, please try again.", "Invalid " +
                    "input.");
            progressCancelled();
            return;
        }

        progressUpdated("Filtering RNA editing sites by quality and coverage, please wait...", 0, 0);
        logger.info("Filtering RNA editing sites by quality and coverage.");
        String linearTableName = currentSample + "_" + parentList.getFilterName() + "_" + DatabaseManager
                .QC_FILTER_RESULT_TABLE_NAME + "_" + qualityThreshold + "_" + depthThreshold;
        if (!TableCreator.createFilterTable(parentList.getTableName(), linearTableName)) {
            progressCancelled();
            return;
        }
        QualityControlFilter bf = new QualityControlFilter(databaseManager);
        // The first parameter means quality and the second means depth
        bf.executeQCFilter(parentList.getTableName(), linearTableName, qualityThreshold, depthThreshold);
        DatabaseManager.getInstance().distinctTable(linearTableName);

        Vector<Site> sites = Query.queryAllEditingSites(linearTableName);
        SiteList newList = new SiteList(parentList, listName(), DatabaseManager.QC_FILTER_RESULT_TABLE_NAME, linearTableName, description());
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

    private boolean isValidInput() {
        return qualityThreshold > 0 && qualityThreshold <= 255 && depthThreshold > 0 && depthThreshold <= 255;
    }

    @Override
    public boolean isReady() {
        return parentList != null && qualityField.getText().length() != 0 && depthField.getText().length() != 0;
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
        return "Quality Control Filter";
    }

    @Override
    public String description() {
        return "Filter RNA editing bases by quality and depth.";
    }

    /**
     * The quality control filter option panel.
     */
    private class QCFilterOptionPanel extends AbstractOptionPanel implements KeyListener {


        /**
         * Instantiates a new quality control filter option panel.
         */
        public QCFilterOptionPanel() {
            super(dataStore);
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
            if (f == qualityField) {
                if (f.getText().length() == 0) {
                    qualityField.setText("");
                } else {
                    qualityThreshold = Integer.parseInt(qualityField.getText());
                }
            } else if (f == depthField) {
                if (f.getText().length() == 0) {
                    depthField.setText("");
                } else {
                    depthThreshold = Integer.parseInt(depthField.getText());
                }
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
            choicePanel.add(new JLabel("Quality >= "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            qualityField = new JTextField(3);
            qualityField.addKeyListener(this);
            choicePanel.add(qualityField, c);
            c.gridy++;
            c.gridx = 0;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Depth of coverage >= "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            depthField = new JTextField(3);
            depthField.addKeyListener(this);
            choicePanel.add(depthField, c);
            return choicePanel;
        }

        @Override
        protected String getPanelDescription() {
            return "Two measures of base quality (Q, range of 1-255) and depth of coverage (DP, range of 1-255) are used in the QC filter. For example, " +
                    "a given site will be removed if it was of a low quality (Q< 20) or with a low depth of coverage (DP< 6).";
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
