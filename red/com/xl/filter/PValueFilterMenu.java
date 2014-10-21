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

import com.dw.denovo.PValueFilter;
import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Query;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.exception.REDException;
import com.xl.panel.DataIntroductionPanel;
import com.xl.preferences.LocationPreferences;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

/**
 * The ValuesFilter filters probes based on their associated values
 * from quantiation.  Each probe is filtered independently of all
 * other probes.
 */
public class PValueFilterMenu extends ProbeFilter {

    private String rScriptPath = null;
    private PValueFilterOptionPanel optionsPanel = new PValueFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public PValueFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by statistic method (P-Value).";
    }

    @Override
    protected void generateProbeList() {
        progressUpdated("Filtering RNA-editing sites by statistic method (P-Value), please wait...", 0, 0);
        PValueFilter pv = new PValueFilter(databaseManager);
        pv.estblishPValueTable(DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME);
        pv.executeFDRFilter(DatabaseManager.PVALUE_FILTER_TABLE_NAME,
                DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME, parentTable,
                LocationPreferences.getInstance().getRScriptPath());
        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME);
        ProbeList newList = new ProbeList(parentList, DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME, "",
                DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME);
        int index = 0;
        int probesLength = probes.size();
        for (Probe probe : probes) {
            progressUpdated(index++, probesLength);
            if (cancel) {
                cancel = false;
                progressCancelled();
                return;
            }
            newList.addProbe(probe);
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
        return stores.length != 0 && rScriptPath != null && rScriptPath.length() != 0;
    }


    @Override
    public String name() {
        return "P-Value Filter";
    }


    @Override
    protected String listDescription() {
        StringBuilder b = new StringBuilder();

        b.append("Filter on probes in ");
        b.append(collection.probeSet().getActiveList().name() + " ");

        for (int s = 0; s < stores.length; s++) {
            b.append(stores[s].name());
            if (s < stores.length - 1) {
                b.append(" , ");
            }
        }
        return b.toString();
    }

    @Override
    protected String listName() {
        return "P-Value filter";
    }


    /**
     * The ValuesFilterOptionPanel.
     */
    private class PValueFilterOptionPanel extends AbstractOptionPanel implements ActionListener {

        private JTextField rScriptField = null;

        /**
         * Instantiates a new values filter option panel.
         */
        public PValueFilterOptionPanel() {
            super(collection);
        }


        public Dimension getPreferredSize() {
            return new Dimension(600, 250);
        }


        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(PValueFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
            }
            optionsChanged();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();
            if (action.equals(LocationPreferences.R_EXECUTABALE_PATH)) {
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
        protected JPanel getOptionPanel() {
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new GridBagLayout());
            choicePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0;
            rScriptField = new JTextField();
            JLabel rScriptLable = new JLabel(LocationPreferences.R_EXECUTABALE_PATH);
            rScriptPath = LocationPreferences.getInstance().getRScriptPath();
            if (rScriptPath != null) {
                rScriptField.setText(rScriptPath);
            } else {
                rScriptField.setText("");
            }
            rScriptField.setEditable(false);
            JButton rScriptButton = new JButton("Browse");
            rScriptButton.setActionCommand(LocationPreferences.R_EXECUTABALE_PATH);
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
            return choicePanel;
        }
    }

}
