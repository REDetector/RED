package com.xl.dialog;

import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.feature.Feature;
import com.xl.main.REDApplication;
import com.xl.utils.ChromosomeNameComparator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeSet;

/**
 * Displays a dialog showing the different types of feature contained
 * in an annotation set and the counts for each.  The counts are
 * calculated asynchronously so the dialog displays quickly even if
 * lots of number crunching needs to be done to get the annotation back
 * off disk.
 */
public class AnnotationSetPropertiesDialog extends JDialog implements Runnable {

    private AnnotationSet set = null;

    private AnnotationSetTableModel model = null;

    private int[] counts = null;

    private String[] chrNames = null;

    /**
     * Instantiates a new annotation set properties dialog.
     *
     * @param set The AnnotationSet to use
     */
    public AnnotationSetPropertiesDialog(AnnotationSet set) {
        super(REDApplication.getInstance(), set.name());
        this.set = set;
        TreeSet<String> treeSet = new TreeSet<String>(ChromosomeNameComparator.getInstance());
        treeSet.addAll(set.getChromosomeNames());
        chrNames = treeSet.toArray(new String[0]);
        counts = new int[chrNames.length];
        model = new AnnotationSetTableModel();
        JTable table = new JTable(model);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(closeButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(300, 300);
        setLocationRelativeTo(REDApplication.getInstance());
        setVisible(true);

        Thread t = new Thread(this);
        t.start();

    }

    public void run() {
        for (int i = 0, len = chrNames.length; i < len; i++) {
            java.util.List<Feature> feature = set.getFeaturesForChr(chrNames[i]);
            counts[i] = feature.size();
            model.fireTableCellUpdated(i, 1);
        }
    }

    /**
     * Provides a tableModel for the results table
     */
    private class AnnotationSetTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return counts.length;
        }

        public String getColumnName(int column) {
            if (column == 0) {
                return "Chromosome";
            } else if (column == 1) {
                return "Feature Count";
            } else
                return null;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return chrNames[rowIndex];
            } else if (columnIndex == 1) {
                if (counts[rowIndex] == 0) {
                    return "Counting...";
                } else {
                    return counts[rowIndex];
                }
            }
            return null;
        }

    }


}
