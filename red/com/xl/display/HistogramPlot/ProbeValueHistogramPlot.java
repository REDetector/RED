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

import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.dialog.CrashReporter;
import com.xl.exception.REDException;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;
import com.xl.utils.filefilters.TxtFileFilter;
import com.xl.utils.imagemanager.ImageSaver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * The Class ProbeValueHistogramPlot shows the distribution of values in a datastore
 * for a probe list.
 */
public class ProbeValueHistogramPlot extends JDialog implements ActionListener, Runnable {

    /**
     * The plot panel.
     */
    private HistogramPanel plotPanel;

    /**
     * The calculating label.
     */
    private final JLabel calculatingLabel = new JLabel("Calculating Plot...", JLabel.CENTER);

    /**
     * The d.
     */
    private DataStore d;

    /**
     * The p.
     */
    private ProbeList p;

    /**
     * Instantiates a new probe value histogram plot.
     *
     * @param d the data
     * @throws REDException the seq monk exception
     */
    public ProbeValueHistogramPlot(DataStore d, ProbeList p) throws REDException {
        super(REDApplication.getInstance(), "Probe Values Plot [" + d.name() + "]");
        setSize(800, 600);
        setLocationRelativeTo(REDApplication.getInstance());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.d = d;
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
     * @param d  the d
     * @param pl the pl
     * @return the data
     */
    private double[] getData(DataStore d, ProbeList pl) {
        Probe[] probes = pl.getAllProbes();

        double[] data = new double[probes.length];

        for (int p = 0; p < probes.length; p++) {
            if (p % 50000 == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
            try {
                data[p] = d.getValueForProbe(probes[p]);
            } catch (REDException e) {
                data[p] = 0;
            }
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
        plotPanel = new HistogramPanel(getData(d, p));
        getContentPane().remove(calculatingLabel);
        getContentPane().add(plotPanel, BorderLayout.CENTER);
        getContentPane().validate();
    }
}
