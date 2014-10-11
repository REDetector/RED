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

import com.dw.denovo.DbsnpFilter;
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
public class DbSNPFilterMenu extends ProbeFilter {

    private DbSNPFilterOptionPanel optionsPanel = new DbSNPFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public DbSNPFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by dbSNP database.";
    }

    @Override
    protected void generateProbeList() {
        DbsnpFilter dbsnpFilter = new DbsnpFilter(databaseManager);
        dbsnpFilter.establishDbSNPResultTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
        dbsnpFilter.executeDbSNPFilter(DatabaseManager.DBSNP_FILTER_TABLE_NAME,
                DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME, parentTable);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
        ProbeList newList = new ProbeList(parentList, DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME, "",
                DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
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
        return "dbSNP Filter";
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#listDescription()
     */
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

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Filters.ProbeFilter#listName()
     */
    @Override
    protected String listName() {
        return "dbSNP Filter";
    }


    /**
     * The ValuesFilterOptionPanel.
     */
    private class DbSNPFilterOptionPanel extends AbstractOptionPanel implements KeyListener {

        private JTextArea description = null;

        /**
         * Instantiates a new values filter option panel.
         */
        public DbSNPFilterOptionPanel() {
            super(collection);
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
            System.out.println(DbSNPFilterMenu.class.getName() + ":valueChanged()");
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
            optionsChanged();
        }

        @Override
        protected JPanel getOptionPanel() {
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new GridBagLayout());
            choicePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            description = new JTextArea("DnSNP Filter will filter out bases which are already \nsnp in DNA level.");
            description.setEditable(false);
            choicePanel.add(description);
            return choicePanel;
        }
    }
}
