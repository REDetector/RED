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

import com.dw.denovo.ComprehensiveFilter;
import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Query;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.dialog.TypeColourRenderer;
import com.xl.exception.REDException;
import com.xl.preferences.REDPreferences;
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
public class ComprehensiveFilterMenu extends ProbeFilter {

    private DataStore[] stores = new DataStore[0];
    private ComprehensiveFilterOptionPanel optionsPanel = new ComprehensiveFilterOptionPanel();
    private int sequenceEdge = -1;

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public ComprehensiveFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by sequence edge.";
    }

    @Override
    protected void generateProbeList() {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        if (REDPreferences.getInstance().isDenovo()) {
            databaseManager.useDatabase(DatabaseManager.DENOVO_DATABASE_NAME);
        } else {
            databaseManager.useDatabase(DatabaseManager.NON_DENOVO_DATABASE_NAME);
        }
        String parentTable = startingList.getTableName();
        ComprehensiveFilter cf = new ComprehensiveFilter(databaseManager);
        cf.establishComprehensiveResultTable(DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME);
        cf.executeComprehensiveFilter(DatabaseManager.COMPREHENSIVE_FILTER_TABLE_NAME,
                DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME, parentTable, 2);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME);
        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME);
        ProbeList newList = new ProbeList(startingList, DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME, "",
                DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME);
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
        return stores.length != 0 && sequenceEdge != -1;
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
        return "Filter by sequence edge length " + sequenceEdge;
    }

    /**
     * The ValuesFilterOptionPanel.
     */
    private class ComprehensiveFilterOptionPanel extends JPanel implements ListSelectionListener, KeyListener {

        private JList<DataStore> dataList;
        private JTextField edgeField;

        /**
         * Instantiates a new values filter option panel.
         */
        public ComprehensiveFilterOptionPanel() {
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
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Edge"), c);
            c.gridx = 1;
            c.weightx = 0.1;
            edgeField = new JTextField(3);
            edgeField.addKeyListener(this);
            choicePanel.add(edgeField, c);

            valueChanged(null);
            add(new JScrollPane(choicePanel), BorderLayout.CENTER);
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#getPreferredSize()
         */
        public Dimension getPreferredSize() {
            return new Dimension(600, 300);
        }

        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(ComprehensiveFilterMenu.class.getName() + ":valueChanged()");
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
            if (f == edgeField) {
                sequenceEdge = Integer.parseInt(edgeField.getText());
            }
            optionsChanged();
        }
    }
}
