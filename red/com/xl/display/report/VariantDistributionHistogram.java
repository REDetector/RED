/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.display.report;

import com.xl.datatypes.DataStore;
import com.xl.main.REDApplication;
import com.xl.net.crashreport.CrashReporter;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.filefilters.TxtFileFilter;
import com.xl.utils.imagemanager.ImageSaver;
import com.xl.utils.ui.OptionDialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * The Class EditingSitesDistributionHistogram shows the distribution of read lengths in a data store.
 */
public class VariantDistributionHistogram extends JDialog implements ActionListener {

    /**
     * The plot panel.
     */
    private VariantHistogramPanel plotPanel;

    private DataStore d;

    /**
     * Instantiates a new read length histogram plot.
     *
     * @param d the data
     */
    public VariantDistributionHistogram(DataStore d) {
        super(REDApplication.getInstance(), "Editing Sites Distribution [" + d.name() + "]");
        this.d = d;
        setSize(800, 600);
        setLocationRelativeTo(REDApplication.getInstance());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        plotPanel = new VariantHistogramPanel(d.siteSet().getActiveList().getAllSites());
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

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals("save")) {
            ImageSaver.saveImage(plotPanel.mainHistogramPanel(), "variant_distribution_" + d.name());
        } else if (ae.getActionCommand().equals("export")) {
            JFileChooser chooser = new JFileChooser(LocationPreferences.getInstance().getProjectSaveLocation());
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileFilter(new TxtFileFilter());

            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.CANCEL_OPTION) return;

            File file = chooser.getSelectedFile();
            LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());

            if (file.isDirectory()) return;

            if (!file.getPath().toLowerCase().endsWith(".txt")) {
                file = new File(file.getPath() + ".txt");
            }

            // Check if we're stepping on anyone's toes...
            if (file.exists()) {
                int answer = OptionDialogUtils.showFileExistDialog(this, file.getName());

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
