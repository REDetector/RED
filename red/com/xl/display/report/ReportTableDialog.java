package com.xl.display.report;

import com.sun.java.TableSorter;
import com.xl.datatypes.genome.Chromosome;
import com.xl.dialog.CrashReporter;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.filefilters.GFFFileFilter;
import com.xl.utils.filefilters.TxtFileFilter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The Class ReportTableDialog is the generic container for all of the
 * different types of report.
 */
public class ReportTableDialog extends JDialog implements MouseListener, ActionListener {

    /**
     * The application.
     */
    private REDApplication application;

    /**
     * The model.
     */
    private TableSorter model;

    /**
     * The report
     */
    private Report report;

    /**
     * Instantiates a new report table dialog.
     *
     * @param application   the application
     * @param report        the report
     * @param originalModel the original model
     */
    public ReportTableDialog(REDApplication application, Report report, TableModel originalModel) {
        super(application, report.name());

        this.report = report;

        this.application = application;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        model = new TableSorter(originalModel);

        JTable table = new JTable(model);
        table.setColumnSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.addMouseListener(this);

        model.setTableHeader(table.getTableHeader());

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton("Close");
        cancelButton.setActionCommand("close");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Save");
        saveButton.setActionCommand("save");
        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(application);
        setVisible(true);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {
        //We're only interested in double clicks
        if (me.getClickCount() != 2) return;
        // This is only linked from the report JTable
        JTable t = (JTable) me.getSource();
        int r = t.getSelectedRow();

        Chromosome chr = null;
        int start = -1;
        int end = -1;

        for (int i = 0; i < model.getColumnCount(); i++) {
            if (model.getColumnName(i).equals("Chr") || model.getColumnName(i).equals("Chromosome")) {

                // The chromosome field can be a Chromosome or String
                // object depending on the report.

                if (model.getValueAt(r, i) instanceof Chromosome) {
                    chr = (Chromosome) model.getValueAt(r, i);
                } else if (model.getValueAt(r, i) instanceof String) {
                    chr = application.dataCollection().genome().getChromosome((String) model.getValueAt(r, i));
                }
            } else if (model.getColumnName(i).equals("Start")) {
                start = (Integer) model.getValueAt(r, i);
            } else if (model.getColumnName(i).equals("End")) {
                end = (Integer) model.getValueAt(r, i);
            }
        }

        if (chr != null && start > 0 && end > 0) {
            DisplayPreferences.getInstance().setLocation(chr, start, end);
        } else {
            System.err.println("Couldn't find a position to jump to.  Closest thing was " + chr + " " + start + "-" + end);
        }

    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals("close")) {
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals("save")) {


            JFileChooser chooser = new JFileChooser(LocationPreferences.getInstance().getProjectSaveLocation());
            chooser.setMultiSelectionEnabled(false);

            if (report.canExportGFF()) {
                chooser.addChoosableFileFilter(new GFFFileFilter());
            }

            TxtFileFilter txtff = new TxtFileFilter();
            chooser.addChoosableFileFilter(txtff);
            chooser.setFileFilter(txtff);

            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.CANCEL_OPTION) return;

            File file = chooser.getSelectedFile();
            LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());

            if (file.isDirectory()) return;

            FileFilter filter = chooser.getFileFilter();

            if (filter instanceof TxtFileFilter) {
                if (!file.getPath().toLowerCase().endsWith(".txt")) {
                    file = new File(file.getPath() + ".txt");
                }
            } else {
                System.err.println("Unknown file filter type " + filter + " when saving image");
                return;
            }

            // Check if we're stepping on anyone's toes...
            if (file.exists()) {
                int answer = JOptionPane.showOptionDialog(this, file.getName() + " exists.  Do you want to overwrite the existing file?", "Overwrite file?",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Overwrite and Save", "Cancel"}, "Overwrite and Save");

                if (answer > 0) {
                    return;
                }
            }

            try {
                if (filter instanceof TxtFileFilter) {
                    saveTextReport(file);
                } else {
                    System.err.println("Unknown file filter type " + filter + " when saving image");
                }
            } catch (IOException e) {
                new CrashReporter(e);
            }
        }

    }

    private void saveTextReport(File file) throws IOException {

        PrintWriter p = new PrintWriter(new FileWriter(file));

        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();

        // Do the headers first
        StringBuffer b = new StringBuffer();
        for (int c = 0; c < colCount; c++) {
            b.append(model.getColumnName(c));
            if (c + 1 != colCount) {
                b.append("\t");
            }
        }

        p.println(b);

        for (int r = 0; r < rowCount; r++) {
            b = new StringBuffer();
            for (int c = 0; c < colCount; c++) {
                b.append(model.getValueAt(r, c));
                if (c + 1 != colCount) {
                    b.append("\t");
                }
            }
            p.println(b);

        }
        p.close();
    }

}
