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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.Vector;

/**
 * The ValuesFilter filters probes based on their associated values
 * from quantiation.  Each probe is filtered independently of all
 * other probes.
 */
public class BasicFilterMenu extends ProbeFilter {

    private DataStore[] stores = new DataStore[0];
    private ValuesFilterOptionPanel optionsPanel = new ValuesFilterOptionPanel();

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
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try {
            databaseManager.connectDatabase();
            databaseManager.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("You haven't connected to database ever.");
            e.printStackTrace();
        }
        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);
        ProbeList newList = new ProbeList(startingList, DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME, "",
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
        return stores.length != 0;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#name()
     */
    @Override
    public String name() {
        return "Probe Values Filter";
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#listDescription()
     */
    @Override
    protected String listDescription() {
        StringBuffer b = new StringBuffer();

        b.append("Filter on probes in ");
        b.append(collection.probeSet().getActiveList().name());

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
        StringBuffer b = new StringBuffer();

        b.append("Basic Filter by ");

        return b.toString();
    }

    /**
     * The ValuesFilterOptionPanel.
     */
    private class ValuesFilterOptionPanel extends JPanel implements ListSelectionListener, KeyListener, ActionListener {

        private JList<DataStore> dataList;
        private JTextField quality;
        private JTextField coverage;
        private JTextField chosenNumberField;
        private JLabel dataAvailableNumber;

        /**
         * Instantiates a new values filter option panel.
         */
        public ValuesFilterOptionPanel() {
            setLayout(new BorderLayout());
            JPanel dataPanel = new JPanel();
            dataPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            dataPanel.setLayout(new BorderLayout());
            dataPanel.add(new JLabel("Data Sets/Groups", JLabel.CENTER), BorderLayout.NORTH);

            DefaultListModel<DataStore> dataModel = new DefaultListModel<DataStore>();

            DataStore[] stores = collection.getAllDataStores();

            for (int i = 0; i < stores.length; i++) {
                dataModel.addElement(stores[i]);
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
            choicePanel.add(new JLabel("Least Quality"), c);
            c.gridx = 1;
            c.weightx = 0.1;
            quality = new JTextField(3);
            quality.addKeyListener(this);
            choicePanel.add(quality, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Coverage"), c);
            c.gridx = 1;
            c.weightx = 0.1;
            coverage = new JTextField(3);
            coverage.addKeyListener(this);
            choicePanel.add(coverage, c);

            add(new JScrollPane(choicePanel), BorderLayout.CENTER);
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#getPreferredSize()
         */
        public Dimension getPreferredSize() {
            return new Dimension(600, 300);
        }

        /* (non-Javadoc)
         * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
         */
        public void keyTyped(KeyEvent arg0) {
        }

        /* (non-Javadoc)
         * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
         */
        public void keyPressed(KeyEvent ke) {

        }

        /* (non-Javadoc)
         * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
         */
        public void keyReleased(KeyEvent ke) {

            JTextField f = (JTextField) ke.getSource();

            try {
//                if (f == lowerLimitField) {
//                    if (f.getText().length() == 0) {
//                        lowerLimit = null;
//                    } else if (f.getText().equals("-")) {
//                        lowerLimit = 0d;
//                    } else {
//                        lowerLimit = Double.parseDouble(f.getText());
//                    }
//                } else if (f == upperLimitField) {
//                    if (f.getText().length() == 0) {
//                        upperLimit = null;
//                    } else if (f.getText().equals("-")) {
//                        upperLimit = 0d;
//                    } else {
//                        upperLimit = Double.parseDouble(f.getText());
//                    }
//                } else if (f == chosenNumberField) {
//                    if (f.getText().length() == 0) {
//                        chosenNumber = -1; // Won't allow filter to register as ready
//                    } else {
//                        chosenNumber = Integer.parseInt(f.getText());
//                    }
//                }
            } catch (NumberFormatException e) {
                f.setText(f.getText().substring(0, f.getText().length() - 1));
            }

            optionsChanged();
        }

        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent lse) {
            Object[] o = dataList.getSelectedValues();
            stores = new DataStore[o.length];
            for (int i = 0; i < o.length; i++) {
                stores[i] = (DataStore) o[i];
            }
            dataAvailableNumber.setText("" + dataList.getSelectedIndices().length);

            optionsChanged();
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            // This comes from the limitTypeBox
//            if (limitTypeBox.getSelectedItem().equals("Exactly"))
//                limitType = EXACTLY;
//            else if (limitTypeBox.getSelectedItem().equals("At least"))
//                limitType = AT_LEAST;
//            else if (limitTypeBox.getSelectedItem().equals("No more than"))
//                limitType = NO_MORE_THAN;
//            else
//                throw new IllegalArgumentException("Unexpected value " + limitTypeBox.getSelectedItem() + " for limit type");

            optionsChanged();
        }

    }
}
