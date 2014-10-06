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

import com.dw.denovo.BasicFilter;
import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Query;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.dialog.TypeColourRenderer;
import com.xl.exception.REDException;
import com.xl.utils.ListDefaultSelector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * The ValuesFilter filters probes based on their associated values
 * from quantiation.  Each probe is filtered independently of all
 * other probes.
 */
public class BasicFilterMenu extends ProbeFilter {

    private DataStore[] stores = new DataStore[0];
    private BasicFilterOptionPanel optionsPanel = new BasicFilterOptionPanel();
    private int qualityInt = -1;
    private int coverageInt = -1;

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws REDException if the dataCollection isn't quantitated.
     */
    public BasicFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by quality and coverage.";
    }

    @Override
    protected void generateProbeList() {
        BasicFilter bf = new BasicFilter(databaseManager);
        bf.establishBasicTable(DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);
        // The first parameter means quality and the second means depth
        bf.executeBasicFilter(parentTable, DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME, qualityInt, coverageInt);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);
        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);
        ProbeList newList = new ProbeList(parentList, DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME, "",
                DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);
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

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#getOptionsPanel()
     */
    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#hasOptionsPanel()
     */
    @Override
    public boolean hasOptionsPanel() {
        return true;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#isReady()
     */
    @Override
    public boolean isReady() {
        return stores.length != 0 && qualityInt != -1 && coverageInt != -1;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#name()
     */
    @Override
    public String name() {
        return "Basic Filter";
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#listDescription()
     */
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

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#listName()
     */
    @Override
    protected String listName() {
        return "Filter by Q>=" + qualityInt + " & Cov>=" + coverageInt;
    }

    /**
     * The ValuesFilterOptionPanel.
     */
    private class BasicFilterOptionPanel extends JPanel implements ListSelectionListener, KeyListener {

        private JList<DataStore> dataList;
        private JTextField quality;
        private JTextField coverage;

        /**
         * Instantiates a new values filter option panel.
         */
        public BasicFilterOptionPanel() {
            setLayout(new BorderLayout());
            JPanel dataPanel = new JPanel();
            dataPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            dataPanel.setLayout(new BorderLayout());
            dataPanel.add(new JLabel("Data Sets/Groups", JLabel.CENTER), BorderLayout.NORTH);

            DefaultListModel<DataStore> dataModel = new DefaultListModel<DataStore>();

            DataStore[] stores = collection.getAllDataStores();

            for (DataStore store : stores) {
                dataModel.addElement(store);
            }

            dataList = new JList<DataStore>(dataModel);
            ListDefaultSelector.selectDefaultStores(dataList);
            dataList.setCellRenderer(new TypeColourRenderer());
            dataList.addListSelectionListener(this);
            dataPanel.add(new JScrollPane(dataList), BorderLayout.CENTER);

            add(dataPanel, BorderLayout.WEST);

            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new GridBagLayout());
            choicePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            GridBagConstraints c = new GridBagConstraints();

            c.gridy = 0;
            c.gridx = 0;
            c.weightx = 0.2;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Quality >= "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            quality = new JTextField(3);
            quality.addKeyListener(this);
            choicePanel.add(quality, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 0.2;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Coverage >= "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            coverage = new JTextField(3);
            coverage.addKeyListener(this);
            choicePanel.add(coverage, c);
            valueChanged(null);
            add(new JScrollPane(choicePanel), BorderLayout.CENTER);
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#getPreferredSize()
         */
        public Dimension getPreferredSize() {
            return new Dimension(600, 250);
        }

        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(BasicFilterMenu.class.getName() + ":valueChanged()");
            java.util.List<DataStore> lists = dataList.getSelectedValuesList();
            stores = new DataStore[lists.size()];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = lists.get(i);
            }
            optionsChanged();
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
            if (f.getText().length() == 0) {
                return;
            }
            if (f == quality) {
                qualityInt = Integer.parseInt(quality.getText());
            } else if (f == coverage) {
                coverageInt = Integer.parseInt(coverage.getText());
            }
            optionsChanged();
        }
    }
}
