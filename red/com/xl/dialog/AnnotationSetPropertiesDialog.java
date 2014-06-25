package com.xl.dialog;

import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.display.featureviewer.Feature;
import com.xl.main.REDApplication;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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

    private List<Integer> counts = null;

    private Enumeration<String> chrs = null;

    /**
     * Instantiates a new annotation set properties dialog.
     *
     * @param set The AnnotationSet to use
     */
    public AnnotationSetPropertiesDialog(AnnotationSet set) {
        super(REDApplication.getInstance(), set.name());
        this.set = set;

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

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        int i = 0;
        chrs = set.getChromosomeNames();
        counts = new ArrayList<Integer>();
        while (chrs.hasMoreElements()) {
            Feature[] feature = set.getFeaturesForChr(chrs.nextElement());
            counts.add(feature.length);
            model.fireTableCellUpdated(i++, 1);
        }

    }

    /**
     * Provides a tableModel for the results table
     */
    private class AnnotationSetTableModel extends AbstractTableModel {

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return 2;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            if (counts != null)
                return counts.size();
            else {
                return 0;
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        public String getColumnName(int column) {
            if (column == 0) {
                return "Chromosome";
            } else if (column == 1) {
                return "Feature Count";
            } else
                return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                int index = 0;
                chrs = set.getChromosomeNames();
                while (chrs.hasMoreElements()) {
                    String chr = chrs.nextElement();
                    if (index == rowIndex) {
                        return chr;
                    } else {
                        index++;
                    }
                }
                System.out.println(this.getClass().getName() + ":" + index);
                return null;
            } else if (columnIndex == 1) {
                if (counts == null) {
                    return "Counting...";
                } else {
                    return counts.get(rowIndex);
                }
            }
            return null;
        }

    }


}
