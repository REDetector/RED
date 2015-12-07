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
import com.xl.display.dialog.JFileChooserExt;
import com.xl.display.panel.DataIntroductionPanel;
import com.xl.exception.RedException;
import com.xl.filter.Filter;
import com.xl.filter.denovo.FisherExactTestFilter;
import com.xl.main.RedApplication;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.ui.OptionDialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * The Class FisherExactTestFilterPanel is a statistical filter panel to provide some parameters to be set as user's
 * preference if there is any choice.
 */
public class FisherExactTestFilterPanel extends AbstractFilterPanel {
    private final Logger logger = LoggerFactory.getLogger(FisherExactTestFilterPanel.class);
    /**
     * The R script or executable path, which is used to calculate FDR value.
     */
    private String rScriptPath = null;
    /**
     * The threshold of p-value.
     */
    private double pvalueThreshold = 0.05;
    /**
     * The threshold of FDR value.
     */
    private double fdrThreshold = 0.05;
    /**
     * The text field of R script or executable path.
     */
    private JTextField rScriptField = null;
    /**
     * The text field of threshold of p-value.
     */
    private JTextField pvalueField;
    /**
     * The text field of threshold of FDR value.
     */
    private JTextField fdrField;
    /**
     * The fisher's exact test filter option panel.
     */
    private FETFilterOptionPanel optionsPanel = new FETFilterOptionPanel();

    /**
     * Instantiates a new fisher's exact test filter.
     *
     * @param dataStore The data store to filter.
     */
    public FisherExactTestFilterPanel(DataStore dataStore) throws RedException {
        super(dataStore);
    }

    @Override
    protected String listName() {
        return "FET Filter";
    }

    @Override
    protected void generateSiteList() throws SQLException {
        progressUpdated("Filtering RNA editing sites by statistic method (P-Value), please wait...", 0, 0);
        logger.info("Filtering RNA editing sites by statistic method (P-Value).");
        String pvalue = pvalueField.getText().replace(".", "");
        String fdr = fdrField.getText().replace(".", "");
        String linearTableName =
            currentSample + "_" + parentList.getFilterName() + "_" + DatabaseManager.FET_FILTER_RESULT_TABLE_NAME + "_"
                + pvalue + "_" + fdr;

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

        Filter filter = new FisherExactTestFilter();
        Map<String, String> params = new HashMap<String, String>();
        params.put(FisherExactTestFilter.PARAMS_STRING_EDITING_TYPE, "AG");
        params.put(FisherExactTestFilter.PARAMS_STRING_FDR_THRESHOLD, fdrThreshold + "");
        params.put(FisherExactTestFilter.PARAMS_STRING_P_VALUE_THRESHOLD, pvalueThreshold + "");
        params.put(FisherExactTestFilter.PARAMS_STRING_R_SCRIPT_PATH, LocationPreferences.getInstance()
            .getRScriptPath());
        filter.performFilter(parentList.getTableName(), linearTableName, params);
        DatabaseManager.getInstance().distinctTable(linearTableName);
        Vector<Site> sites = Query.queryAllEditingSites(linearTableName);
        SiteList newList =
            new SiteList(parentList, listName(), DatabaseManager.FET_FILTER_RESULT_TABLE_NAME, linearTableName,
                description());
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
        return parentList != null && rScriptPath != null && rScriptPath.length() != 0;
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
        return "Fisher's Exact Test Filter";
    }

    @Override
    public String description() {
        return "Filter RNA editing sites by statistic method (p-value).";
    }

    /**
     * The fisher's exact test filter option panel.
     */
    private class FETFilterOptionPanel extends AbstractFilterOptionPanel implements ActionListener, KeyListener {

        /**
         * Instantiates a new fisher's exact test filter option panel.
         */
        public FETFilterOptionPanel() {
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
        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();
            if (action.equals(LocationPreferences.R_SCRIPT_PATH)) {
                getFile(action, rScriptField);
                LocationPreferences.getInstance().setRScriptPath(rScriptField.getText());
                rScriptPath = rScriptField.getText();
                optionsChanged();
            }
        }

        /**
         * Launches a file browser to select a directory
         *
         * @param dataType The action.
         * @param f the TextFild from which to take the starting directory
         */
        private void getFile(String dataType, JTextField f) {
            JFileChooser chooser =
                new JFileChooserExt(LocationPreferences.getInstance().getProjectSaveLocation(), null);
            chooser.setCurrentDirectory(new File(f.getText()));
            chooser.setAccessory(new DataIntroductionPanel(dataType));
            chooser.setDialogTitle("Select Directory");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                f.setText(chooser.getSelectedFile().getAbsolutePath().replaceAll("\\\\", "/"));
                File file = chooser.getSelectedFile();
                LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());
            }
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
            rScriptField = new JTextField();
            JLabel rScriptLable = new JLabel(LocationPreferences.R_SCRIPT_PATH);
            rScriptPath = LocationPreferences.getInstance().getRScriptPath();
            if (rScriptPath != null) {
                rScriptField.setText(rScriptPath);
            } else {
                rScriptField.setText("");
            }
            rScriptField.setEditable(false);
            JButton rScriptButton = new JButton("Browse");
            rScriptButton.setActionCommand(LocationPreferences.R_SCRIPT_PATH);
            rScriptButton.addActionListener(this);
            c.gridx = 0;
            c.weightx = 0.1;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(rScriptLable, c);
            c.gridx = 1;
            c.weightx = 0.5;
            choicePanel.add(rScriptField, c);
            c.gridx = 2;
            c.weightx = 0.1;
            choicePanel.add(rScriptButton, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Filter sites with p-value > "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            pvalueField = new JTextField(5);
            pvalueField.setText(pvalueThreshold + "");
            pvalueField.addKeyListener(this);
            choicePanel.add(pvalueField, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Filter sites with fdr > "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            fdrField = new JTextField(5);
            fdrField.setText(fdrThreshold + "");
            fdrField.addKeyListener(this);
            choicePanel.add(fdrField, c);

            return choicePanel;
        }

        @Override
        protected String getPanelDescription() {
            return "Fisher's exact test is used to reduce the errors in detecting RNA editing sites caused by technical artifacts (e.g., sequencing errors).";
        }

        @Override
        public void keyTyped(KeyEvent e) {
            int keyChar = e.getKeyChar();
            if (!((keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9) || (keyChar == KeyEvent.VK_PERIOD)))
                e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            JTextField f = (JTextField) e.getSource();
            if (f == pvalueField) {
                if (f.getText().length() == 0) {
                    pvalueField.setText("");
                } else {
                    pvalueThreshold = Double.parseDouble(pvalueField.getText());
                }
            } else if (f == fdrField) {
                if (f.getText().length() == 0) {
                    fdrField.setText("");
                } else {
                    fdrThreshold = Double.parseDouble(fdrField.getText());
                }
            }
            optionsChanged();
        }
    }

}
