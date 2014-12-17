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
package com.xl.display.panel;

import com.xl.main.Global;
import com.xl.main.REDApplication;
import com.xl.net.crashreport.CrashReporter;
import com.xl.net.genomes.UpdateChecker;
import com.xl.preferences.LocationPreferences;
import com.xl.preferences.REDPreferences;
import com.xl.utils.FileUtils;
import com.xl.utils.namemanager.InfoPanelUtils;
import com.xl.utils.ui.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * This panel is displayed when the program first starts. It shows information about the current RED install
 */
public class REDInfoPanel extends JPanel implements Runnable, ActionListener {

    /**
     * The update label.
     */
    private JLabel programUpdateLabel;

    /**
     * The update label text.
     */
    private JLabel programUpdateLabelText;
    /**
     * The application
     */
    private REDApplication application;
    /**
     * Check if the cache directory is valid or not.
     */
    private boolean invalidCacheDirectory = false;

    /**
     * The two dp.
     */
    private DecimalFormat twoDP = new DecimalFormat("#.##");

    /**
     * Instantiates a new RED information panel.
     */
    public REDInfoPanel(REDApplication application) {
        this.application = application;
        populatePanel();
        repaint();
    }

    private void populatePanel() {

        removeAll();
        validate();

        // We prepare a couple of buttons for optional later use

        JButton setTempDirButton = this.addNewButton(InfoPanelUtils.CACHE_DIRECTORY_SET);
        JButton removeStaleFilesButton = this.addNewButton(InfoPanelUtils.CACHE_OLD_FILES_DETELE);
        JButton setGenomesFolderButton = this.addNewButton(InfoPanelUtils.GENOMES_CUSTOM_FOLDER_SET);
        JButton updateGenomesButton = this.addNewButton(InfoPanelUtils.GENOMES_UPDATE);

        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.001;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        // First the Memory available
        JLabel memoryLabel = new JLabel(IconLoader.ICON_INFO);
        add(memoryLabel, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 0.999;
        double memory = ((double) Runtime.getRuntime().maxMemory()) / (1024 * 1024 * 1024);
        add(new JLabel(twoDP.format(memory) + " GB of memory in jvm is available", JLabel.LEFT), gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        gridBagConstraints.weightx = 0.001;

        // Whether we're running the latest version
        programUpdateLabel = new JLabel(IconLoader.ICON_INFO);
        add(programUpdateLabel, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 0.999;
        programUpdateLabelText = new JLabel(InfoPanelUtils.PROGRAM_UPDATE_CHECK, JLabel.LEFT);
        add(programUpdateLabelText, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        gridBagConstraints.weightx = 0.001;

        gridBagConstraints.gridx = 2;
        gridBagConstraints.weightx = 0.001;
        add(updateGenomesButton, gridBagConstraints);
        updateGenomesButton.setVisible(false);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        gridBagConstraints.weightx = 0.001;

        // Whether we've set a temp directory

        // This will record whether our temp dir is invalid

        File tempDir = new File(LocationPreferences.getInstance().getTempDirectory());

        if (!(tempDir.exists() && tempDir.isDirectory() && tempDir.canRead() && tempDir.canWrite() && tempDir.listFiles() != null)) {
            JLabel tempLabel = new JLabel(IconLoader.ICON_ERROR);
            add(tempLabel, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            gridBagConstraints.weightx = 0.999;
            add(new JLabel(InfoPanelUtils.CACHE_DIRECTORY_CONFIGURE_FAIL, JLabel.LEFT), gridBagConstraints);
            gridBagConstraints.gridx = 2;
            gridBagConstraints.weightx = 0.001;
            add(setTempDirButton, gridBagConstraints);
            invalidCacheDirectory = true;
        } else {
            // Check if we can actually write something into the cache directory (don't just trust that we can)

            try {
                File tempFile = File.createTempFile("red_test_data", ".temp", tempDir);
                FileOutputStream fis = new FileOutputStream(tempFile);
                fis.write(123456789);
                fis.close();
                if (!tempFile.delete()) {
                    throw new IOException();
                }
            } catch (IOException ioe) {
                // Something failed when trying to use the cache directory
                JLabel tempLabel = new JLabel(IconLoader.ICON_ERROR);
                add(tempLabel, gridBagConstraints);
                gridBagConstraints.gridx = 1;
                gridBagConstraints.weightx = 0.999;
                add(new JLabel("A test write to your cache directory failed (" + ioe.getLocalizedMessage() + "). Please configure a cache directory to allow RED to run.",
                        JLabel.LEFT), gridBagConstraints);
                gridBagConstraints.gridx = 2;
                gridBagConstraints.weightx = 0.001;
                add(setTempDirButton, gridBagConstraints);
                invalidCacheDirectory = true;
            }

            if (!invalidCacheDirectory) {
                // Check to see if we have stale red temp files
                File[] tempFiles = tempDir.listFiles();

                int staleFiles = 0;
                if (tempFiles != null) {
                    for (File tempFile : tempFiles) {
                        if (tempFile.isFile() && tempFile.getName().startsWith("red") && tempFile.getName().endsWith(".temp")) {
                            staleFiles++;
                        }
                    }
                }

                if (staleFiles > 0) {
                    JLabel tempLabel = new JLabel(IconLoader.ICON_WARNING);
                    add(tempLabel, gridBagConstraints);
                    gridBagConstraints.gridx = 1;
                    gridBagConstraints.weightx = 0.999;
                    add(new JLabel("Disk caching is available and enabled - but you have " + staleFiles + " stale temp files", JLabel.LEFT), gridBagConstraints);
                    gridBagConstraints.gridx = 2;
                    gridBagConstraints.weightx = 0.001;
                    add(removeStaleFilesButton, gridBagConstraints);
                } else {
                    JLabel tempLabel = new JLabel(IconLoader.ICON_TICK);
                    add(tempLabel, gridBagConstraints);
                    gridBagConstraints.gridx = 1;
                    gridBagConstraints.weightx = 0.999;
                    add(new JLabel("Disk caching is available and enabled", JLabel.LEFT), gridBagConstraints);
                }

                application.cacheFolderChecked();
            }
        }

        gridBagConstraints.gridy++;
        gridBagConstraints.weightx = 0.001;

        // Check the genomes directory
        JLabel genomesLabel = new JLabel(IconLoader.ICON_ERROR);
        JLabel genomesLabelText = new JLabel(InfoPanelUtils.GENOMES_CHECK_FOLDER_DISABLE);

        // They're using a custom genomes folder
        File gb = new File(LocationPreferences.getInstance().getGenomeDirectory());

        if (!gb.exists()) {
            // There is no default genomes folder
            genomesLabel.setIcon(IconLoader.ICON_ERROR);
            genomesLabelText.setText(InfoPanelUtils.GENOMES_FOLDER_GET_FAIL);
            gridBagConstraints.gridx = 2;
            gridBagConstraints.weightx = 0.001;
            add(setGenomesFolderButton, gridBagConstraints);
        } else if (!gb.canRead()) {
            // The default genomes folder is present but useless
            genomesLabel.setIcon(IconLoader.ICON_ERROR);
            genomesLabelText.setText(InfoPanelUtils.PERMISSION_DENIED_READ);
            gridBagConstraints.gridx = 2;
            gridBagConstraints.weightx = 0.001;
            add(setGenomesFolderButton, gridBagConstraints);
        } else if (!gb.canWrite()) {
            // The default genomes folder is present, but we can't import
            // new genomes
            genomesLabel.setIcon(IconLoader.ICON_WARNING);
            genomesLabelText.setText(InfoPanelUtils.PERMISSION_DENIED_WRITE);
            gridBagConstraints.gridx = 2;
            gridBagConstraints.weightx = 0.001;
            add(setGenomesFolderButton, gridBagConstraints);
        } else {
            // Everything is OK
            genomesLabel.setIcon(IconLoader.ICON_INFO);
            genomesLabelText.setText(InfoPanelUtils.GENOMES_USE_CUSTOM_FOLDER);
        }

        gridBagConstraints.gridx = 0;
        add(genomesLabel, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 0.999;
        add(genomesLabelText, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        gridBagConstraints.weightx = 0.001;

        // We can start the update checker if they've allowed us to
        if (REDPreferences.getInstance().checkForUpdates()) {
            Thread t = new Thread(this);
            t.start();
        } else {
            programUpdateLabel.setIcon(IconLoader.ICON_WARNING);
            programUpdateLabelText.setText(InfoPanelUtils.PROGRAM_CHECK_UPDATE_DISABLE);
        }

        validate();
        repaint();

    }

    public boolean cacheDirectoryValid() {
        return !invalidCacheDirectory;
    }

    private JButton addNewButton(String button) {
        JButton jb = new JButton(button);
        jb.setActionCommand(button);
        jb.addActionListener(this);
        return jb;
    }

    @Override
    public void run() {
        try {
            if (UpdateChecker.isUpdateAvailable()) {
                String latestVersion = UpdateChecker.getLatestVersionNumber();
                programUpdateLabel.setIcon(IconLoader.ICON_WARNING);
                programUpdateLabelText.setText("A newer version of RED (v" + latestVersion + ") is available");
            } else {
                if (Global.VERSION.contains("dev")) {
                    programUpdateLabel.setIcon(IconLoader.ICON_WARNING);
                    programUpdateLabelText.setText("You are running a current development version of RED");
                } else {
                    programUpdateLabel.setIcon(IconLoader.ICON_TICK);
                    programUpdateLabelText.setText("You are running the latest version of RED");
                }
            }
        } catch (Exception e) {
            programUpdateLabel.setIcon(IconLoader.ICON_ERROR);
            programUpdateLabelText.setText("Failed to check for RED updates");
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(InfoPanelUtils.CACHE_DIRECTORY_SET)) {
            JFileChooser chooser = new JFileChooser(LocationPreferences.getInstance().getCacheDirectory());
            chooser.setDialogTitle("Select a Cache Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                LocationPreferences.getInstance().setTempDirectory(
                        chooser.getSelectedFile().getAbsolutePath());
                try {
                    REDPreferences.getInstance().savePreferences();
                    populatePanel();
                } catch (IOException ioe) {
                    new CrashReporter(ioe);
                }
            }
        }
        if (e.getActionCommand().equals(InfoPanelUtils.GENOMES_CUSTOM_FOLDER_SET)) {
            JFileChooser chooser = new JFileChooser(LocationPreferences.getInstance().getGenomeDirectory());
            chooser.setDialogTitle("Select a Genomes Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                LocationPreferences.getInstance().setGenomeDirectory(
                        chooser.getSelectedFile().getAbsolutePath());
                try {
                    REDPreferences.getInstance().savePreferences();
                    populatePanel();
                } catch (IOException ioe) {
                    new CrashReporter(ioe);
                }
            }
        } else if (e.getActionCommand().equals(InfoPanelUtils.CACHE_OLD_FILES_DETELE)) {
            int answer = JOptionPane.showConfirmDialog(this, "Please close any other running instances of RED before cleaning the cache", "Cleaning cache",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.CANCEL_OPTION)
                return;

            FileUtils.deleteDirectory(LocationPreferences.getInstance().getTempDirectory());
        }

        populatePanel();
    }
}
