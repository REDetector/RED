/**
 * Copyright Copyright 2006-13 Simon Andrews
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
package com.xl.display.HistogramPlot;

import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.dialog.CrashReporter;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;
import com.xl.utils.ImageSaver;
import com.xl.utils.filefilters.TxtFileFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * The ProbeLengthHistogramPlot allows you to construct a histogram from
 * the range of probe lengths in a ProbeList
 */
public class ProbeLengthHistogramPlot extends JDialog implements ActionListener, Runnable {

    private HistogramPanel plotPanel;
    private final JLabel calculatingLabel = new JLabel("Calculating Plot...", JLabel.CENTER);
    private ProbeList p;

    /**
     * Instantiates a new probe length histogram plot.
     *
     * @param p The probe list to plot
     */
    public ProbeLengthHistogramPlot(ProbeList p) {

        super(REDApplication.getInstance(), "Probe Length Plot [" + p.name() + "]");
        setSize(800, 600);
        setLocationRelativeTo(REDApplication.getInstance());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.p = p;

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(calculatingLabel, BorderLayout.CENTER);

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

        Thread t = new Thread(this);
        t.start();

    }

    /**
     * Gets the data.
     *
     * @param pl the pl
     * @return the data
     */
    private double[] getData(ProbeList pl) {
        Probe[] probes = pl.getAllProbes();

        double[] data = new double[probes.length];

        for (int p = 0; p < probes.length; p++) {
            data[p] = probes[p].length();
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
            JFileChooser chooser = new JFileChooser(REDPreferences.getInstance().getSaveLocation());
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileFilter(new TxtFileFilter());

            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.CANCEL_OPTION) return;

            File file = chooser.getSelectedFile();
            REDPreferences.getInstance().setLastUsedSaveLocation(file);

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

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        plotPanel = new HistogramPanel(getData(p));
        getContentPane().remove(calculatingLabel);
        getContentPane().add(plotPanel, BorderLayout.CENTER);
        getContentPane().validate();
    }
}
