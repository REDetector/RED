package com.xl.display.report;

import com.xl.datatypes.DataStore;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.dialog.CrashReporter;
import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.filefilters.TxtFileFilter;
import com.xl.utils.imagemanager.ImageSaver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * The Class EditingSitesDistributionHistogram shows the distribution of read lengths
 * in a data store.
 */
public class EditingSitesDistributionHistogram extends JDialog implements ActionListener {

    /**
     * The plot panel.
     */
    private HistogramPanel plotPanel;

    /**
     * Instantiates a new read length histogram plot.
     *
     * @param d the data
     */
    public EditingSitesDistributionHistogram(DataStore d) {
        super(REDApplication.getInstance(), "Editing Sites Distribution [" + d.name() + "]");
        setSize(800, 600);
        setLocationRelativeTo(REDApplication.getInstance());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        plotPanel = new HistogramPanel(d.collection().genome(), d.collection().probeSet().getActiveList().getAllProbes());
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(plotPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton cancelButton = new JButton("Close");
        cancelButton.setActionCommand("close");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Save");
        saveButton.setActionCommand("save");
        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);

        JButton exportButton = new JButton("Export Data");
        exportButton.setActionCommand("export");
        exportButton.addActionListener(this);
        buttonPanel.add(exportButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);

    }

    /**
     * Gets the read lengths.
     *
     * @param d the d
     * @return the read lengths
     */
    private double[] getReadLengths(DataStore d) {
        double[] data = new double[d.getTotalReadCount()];

        int offset = 0;

        Chromosome[] chrs = d.collection().genome().getAllChromosomes();

        for (int c = 0; c < chrs.length; c++) {
            SequenceRead[] reads = d.getReadsForChromosome(chrs[c].getName());

            for (int r = 0; r < reads.length; r++) {
                data[offset + r] = reads[r].length();
            }
            offset += reads.length;
        }

        return data;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals("save")) {
            ImageSaver.saveImage(plotPanel.mainHistogramPanel());
        } else if (ae.getActionCommand().equals("export")) {
            JFileChooser chooser = new JFileChooser(LocationPreferences.getInstance().getProjectSaveLocation());
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileFilter(new TxtFileFilter());

            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.CANCEL_OPTION) return;

            File file = chooser.getSelectedFile();
            LocationPreferences.getInstance().setProjectSaveLocation(file.getAbsolutePath());

            if (file.isDirectory()) return;

            if (!file.getPath().toLowerCase().endsWith(".txt")) {
                file = new File(file.getPath() + ".txt");
            }

            // Check if we're stepping on anyone's toes...
            if (file.exists()) {
                int answer = JOptionPane.showOptionDialog(this, file.getName() + " exists.  Do you want to overwrite the existing file?", "Overwrite file?", 0, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Overwrite and Save", "Cancel"}, "Overwrite and Save");

                if (answer > 0) {
                    return;
                }
            }

            try {
                plotPanel.exportData(file);
            } catch (IOException e) {
                new CrashReporter(e);
            }

        }
    }
}
