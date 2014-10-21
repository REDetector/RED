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

import com.dw.denovo.RepeatFilter;
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
import java.util.Vector;

/**
 * The ValuesFilter filters probes based on their associated values
 * from quantiation.  Each probe is filtered independently of all
 * other probes.
 */
public class RepeatFilterMenu extends ProbeFilter {

    private RepeatFilterOptionPanel optionsPanel = new RepeatFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public RepeatFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by repeatmasker database.";
    }

    @Override
    protected void generateProbeList() {
        progressUpdated("Filtering RNA-editing sites by repeatmasker database, please wait...", 0, 0);
        RepeatFilter rf = new RepeatFilter(databaseManager);
        rf.establishRepeatResultTable(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);
        rf.establishAluResultTable(DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME);
        rf.executeRepeatFilter(DatabaseManager.REPEAT_FILTER_TABLE_NAME, DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME,
                DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME, parentTable);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);

        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);
        ProbeList newList = new ProbeList(parentList, DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME, "",
                DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);
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
        return "Repeat Filter";
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
        return "Repeat area filter";
    }


    /**
     * The ValuesFilterOptionPanel.
     */
    private class RepeatFilterOptionPanel extends AbstractOptionPanel {

        private JTextArea description = null;

        /**
         * Instantiates a new values filter option panel.
         */
        public RepeatFilterOptionPanel() {
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
            System.out.println(RepeatFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
            }
            optionsChanged();
        }

        @Override
        protected JPanel getOptionPanel() {
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new GridBagLayout());
            choicePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            description = new JTextArea("The repeat filter will remove bases located in such " +
                    "\nareas where are supposed to be unfunctional except " +
                    "\nfor SINE/Alu area.");
//            description.setLineWrap(true);
            description.setEditable(false);
            choicePanel.add(description);
            return choicePanel;
        }
    }
}
