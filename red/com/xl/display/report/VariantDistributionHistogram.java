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
import com.xl.utils.namemanager.MenuUtils;
import com.xl.utils.ui.OptionDialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * The Class VariantDistributionHistogram shows the distribution of variant types.
 */
public class VariantDistributionHistogram extends JDialog implements ActionListener {

    /**
     * The plot panel.
     */
    private VariantHistogramPanel plotPanel;
    /**
     * The data store
     */
    private DataStore dataStore;

    /**
     * Instantiates a new read length histogram plot.
     *
     * @param dataStore the data
     */
    public VariantDistributionHistogram(DataStore dataStore) {
        super(REDApplication.getInstance(), "Variant Type Distribution [" + dataStore.name() + "]");
        this.dataStore = dataStore;
        setSize(800, 600);
        setLocationRelativeTo(REDApplication.getInstance());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        plotPanel = new VariantHistogramPanel(dataStore);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(plotPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton cancelButton = new JButton(MenuUtils.CLOSE_BUTTON);
        cancelButton.setActionCommand(MenuUtils.CLOSE_BUTTON);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton(MenuUtils.SAVE_BUTTON);
        saveButton.setActionCommand(MenuUtils.SAVE_BUTTON);
        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);

        JButton exportButton = new JButton(MenuUtils.EXPORT_BUTTON);
        exportButton.setActionCommand(MenuUtils.EXPORT_BUTTON);
        exportButton.addActionListener(this);
        buttonPanel.add(exportButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals(MenuUtils.CLOSE_BUTTON)) {
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals(MenuUtils.SAVE_BUTTON)) {
            ImageSaver.saveImage(plotPanel.mainHistogramPanel(), "variant_distribution_" + dataStore.name());
        } else if (ae.getActionCommand().equals(MenuUtils.EXPORT_BUTTON)) {
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
