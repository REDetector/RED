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
import com.xl.display.panel.DataIntroductionPanel;
import com.xl.exception.REDException;
import com.xl.filter.denovo.FisherExactTestFilter;
import com.xl.preferences.LocationPreferences;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Vector;

/**
 * The ValuesFilter filters sites based on their associated values from quantiation.  Each site is filtered independently of all other sites.
 */
public class FisherExactTestFilterPanel extends AbstractSiteFilter {

    private String rScriptPath = null;
    private double pvalueThres = 0.05;
    private double fdrThres = 0.05;
    private JTextField rScriptField = null;
    private JTextField pvalueField;
    private JTextField fdrField;
    private PValueFilterOptionPanel optionsPanel = new PValueFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param dataStore The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public FisherExactTestFilterPanel(DataStore dataStore) throws REDException {
        super(dataStore);
    }

    @Override
    public String description() {
        return "Filter editing bases by statistic method (P-Value).";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA editing sites by statistic method (P-Value), please wait...", 0, 0);
        String linearTableName = currentSample + "_" + parentList.getFilterName() + "_" + DatabaseManager
                .PVALUE_FILTER_RESULT_TABLE_NAME + "_" + pvalueThres + "_" + fdrThres;
        TableCreator.createFisherExactTestTable(linearTableName);
        FisherExactTestFilter pv = new FisherExactTestFilter(databaseManager);
        pv.executeFDRFilter(DatabaseManager.DARNED_DATABASE_TABLE_NAME, linearTableName, parentList.getTableName(),
                LocationPreferences.getInstance().getRScriptPath(), pvalueThres, fdrThres);

        Vector<Site> sites = Query.queryAllEditingSites(linearTableName);
        SiteList newList = new SiteList(parentList, listName(), DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME, linearTableName, description());
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
        return parentList != null && rScriptPath != null && rScriptPath.length() != 0;
    }


    @Override
    public String name() {
        return "Fisher's Exact Test Filter";
    }

    @Override
    protected String listName() {
        return "FET Filter";
    }


    /**
     * The ValuesFilterOptionPanel.
     */
    private class PValueFilterOptionPanel extends AbstractOptionPanel implements ActionListener, KeyListener {

        /**
         * Instantiates a new values filter option panel.
         */
        public PValueFilterOptionPanel() {
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
         * @param f        the TextFild from which to take the starting directory
         */
        private void getFile(String dataType, JTextField f) {
            JFileChooser chooser = new JFileChooser(LocationPreferences.getInstance().getProjectSaveLocation());
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
            pvalueField.setText(pvalueThres + "");
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
            fdrField.setText(fdrThres + "");
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
                    pvalueThres = Double.parseDouble(pvalueField.getText());
                }
            } else if (f == fdrField) {
                if (f.getText().length() == 0) {
                    fdrField.setText("");
                } else {
                    fdrThres = Double.parseDouble(fdrField.getText());
                }
            }
            optionsChanged();
        }
    }

}
