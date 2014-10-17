package com.xl.filter;

import com.dw.denovo.EditingTypeFilter;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * The ValuesFilter filters probes based on their associated values
 * from quantiation.  Each probe is filtered independently of all
 * other probes.
 */
public class EditingTypeFilterMenu extends ProbeFilter {

    private JComboBox refBase = null;
    private JComboBox altBase = null;
    private SpecificFilterOptionPanel optionsPanel = new SpecificFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public EditingTypeFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by editing base information,such as 'A'->'G','C'->'T',etc.";
    }

    @Override
    protected void generateProbeList() {
        EditingTypeFilter editingTypeFilter = new EditingTypeFilter(databaseManager);
        editingTypeFilter.establishSpecificTable(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
        editingTypeFilter.executeSpecificFilter(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME, parentTable,
                refBase.getSelectedItem().toString(), altBase.getSelectedItem().toString());
        DatabaseManager.getInstance().distinctTable(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
        ProbeList newList = new ProbeList(parentList, DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME, "",
                DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
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
        return stores.length != 0;
    }

    @Override
    public String name() {
        return "Specific Filter";
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
        return "Focus on " + refBase.getSelectedItem() + " to " + altBase.getSelectedItem();
    }

    /**
     * The ValuesFilterOptionPanel.
     */
    private class SpecificFilterOptionPanel extends AbstractOptionPanel implements ActionListener {

        /**
         * Instantiates a new values filter option panel.
         */
        public SpecificFilterOptionPanel() {
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
            System.out.println(QCFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
            }
            optionsChanged();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            String action = e.getActionCommand();
//            if (action.equals("ref")) {
//                if ((Character) refBase.getSelectedItem() == 'A') {
//                    altBase.setSelectedItem('G');
//                } else if (refBase.getSelectedItem().equals('G')) {
//                    altBase.setSelectedItem('A');
//                } else if (refBase.getSelectedItem().equals('C')) {
//                    altBase.setSelectedItem('T');
//                } else if (refBase.getSelectedItem().equals('T')) {
//                    altBase.setSelectedItem('C');
//                }
//            } else if (action.equals("alt")) {
//
//                if (altBase.getSelectedItem().equals('A')) {
//                    refBase.setSelectedItem('G');
//                } else if (altBase.getSelectedItem().equals('G')) {
//                    refBase.setSelectedItem('A');
//                } else if (altBase.getSelectedItem().equals('T')) {
//                    refBase.setSelectedItem('C');
//                } else if (altBase.getSelectedItem().equals('C')) {
//                    refBase.setSelectedItem('T');
//                }
//            }
        }

        @Override
        protected JPanel getOptionPanel() {
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));

            JPanel choicePanel2 = new JPanel();
            choicePanel2.add(new JLabel("Focus on editing event from"));
            choicePanel.add(choicePanel2);

            JPanel choicePanel3 = new JPanel();
            choicePanel3.add(new JLabel("reference base "));
            refBase = new JComboBox(new Character[]{'A', 'G', 'C', 'T'});
            refBase.setSelectedItem('A');
            refBase.setActionCommand("ref");
            refBase.addActionListener(this);
            choicePanel3.add(refBase);
            choicePanel.add(choicePanel3);

            JPanel choicePanel4 = new JPanel();
            choicePanel4.add(new JLabel("to alternative base "));
            altBase = new JComboBox(new Character[]{'A', 'G', 'C', 'T'});
            altBase.setSelectedItem('G');
            altBase.setActionCommand("alt");
            altBase.addActionListener(this);
            choicePanel4.add(altBase);
            choicePanel.add(choicePanel4);
            return choicePanel;
        }
    }
}