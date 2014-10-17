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

import com.dw.denovo.QCFilter;
import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Query;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.exception.REDException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * The ValuesFilter filters probes based on their associated values
 * from quantiation.  Each probe is filtered independently of all
 * other probes.
 */
public class QCFilterMenu extends ProbeFilter {


    private int qualityInt = -1;
    private int coverageInt = -1;
    private QCFilterOptionPanel optionsPanel = new QCFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws REDException if the dataCollection isn't quantitated.
     */
    public QCFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by quality and coverage.";
    }

    @Override
    protected void generateProbeList() {
        QCFilter bf = new QCFilter(databaseManager);
        bf.establishQCTable(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
        // The first parameter means quality and the second means depth
        bf.executeQCFilter(parentTable, DatabaseManager.QC_FILTER_RESULT_TABLE_NAME, qualityInt, coverageInt);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
        ProbeList newList = new ProbeList(parentList, DatabaseManager.QC_FILTER_RESULT_TABLE_NAME, "",
                DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
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
        return stores.length != 0 && qualityInt != -1 && coverageInt != -1;
    }

    @Override
    public String name() {
        return "QC Filter";
    }

    @Override
    protected String listDescription() {
        StringBuilder b = new StringBuilder();

        b.append("Filter on probes in ");
        b.append(collection.probeSet().getActiveList().name()).append(" ");

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
        return "Q>=" + qualityInt + " & C>=" + coverageInt;
    }

    /**
     * The ValuesFilterOptionPanel.
     */
    private class QCFilterOptionPanel extends AbstractOptionPanel implements KeyListener {

        private JTextField quality;
        private JTextField coverage;

        /**
         * Instantiates a new values filter option panel.
         */
        public QCFilterOptionPanel() {
            super(collection);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(600, 250);
        }

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(QCFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
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

        @Override
        protected JPanel getOptionPanel() {
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
            return choicePanel;
        }
    }
}
