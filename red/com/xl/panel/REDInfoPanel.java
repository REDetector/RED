/**
 * Copyright 2010-13 Simon Andrews
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
package com.xl.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.xl.genomes.UpdateChecker;

import com.xl.dialog.CrashReporter;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;
import com.xl.utils.IconUtils;
import com.xl.utils.InfoPanelUtils;

/**
 * This panel is displayed when the program first starts. It shows information
 * about the current RED install
 */
public class REDInfoPanel extends JPanel implements Runnable, ActionListener {

	/*
     * We tried to use the standard OptionPanel icons here, but some systems
	 * didn't have sensible icons and used horrible defaults so now we specify
	 * our own. The error icon is a public domain icon from
	 * http://www.clker.com/clipart-12247.html The others are modifications of
	 * that icon done by Simon Andrews as part of this project. The SVG files
	 * for these icons are in the same folder as the loaded png files.
	 */

    /**
     * The update label.
     */
    private JLabel programUpdateLabel;

    /**
     * The update label text.
     */
    private JLabel programUpdateLabelText;

    private REDApplication application;

    private boolean invalidCacheDirectory = false;

    /**
     * The two dp.
     */
    private DecimalFormat twoDP = new DecimalFormat("#.##");

    /**
     * Instantiates a new seq monk information panel.
     */
    public REDInfoPanel(REDApplication application) {
        this.application = application;
        populatePanel();
        repaint();
    }

    private void populatePanel() {

        removeAll();
        validate();
        invalidCacheDirectory = false;

        // We prepare a couple of buttons for optional later use

        JButton setTempDirButton = this
                .addNewButton(InfoPanelUtils.CACHE_DIRECTORY_SET);
        JButton removeStaleFilesButton = this
                .addNewButton(InfoPanelUtils.CACHE_OLD_FILES_DETELE);
        JButton setGenomesFolderButton = this
                .addNewButton(InfoPanelUtils.GENOMES_CUSTOM_FOLDER_SET);
        JButton updateGenomesButton = this
                .addNewButton(InfoPanelUtils.GENOMES_UPDATE);

        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.001;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        // First the Memory available
        JLabel memoryLabel = new JLabel(IconUtils.INFOICON);
        add(memoryLabel, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 0.999;
        Runtime rt = Runtime.getRuntime();
        double memory = ((double) Runtime.getRuntime().maxMemory())
                / (1024 * 1024 * 1024);
        add(new JLabel(twoDP.format(memory) + " GB of memory in jvm is available",
                JLabel.LEFT), gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        gridBagConstraints.weightx = 0.001;

        // Whether we're running the latest version
        programUpdateLabel = new JLabel(IconUtils.INFOICON);
        add(programUpdateLabel, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 0.999;
        programUpdateLabelText = new JLabel(
                InfoPanelUtils.PROGRAM_UPDATE_CHECK, JLabel.LEFT);
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

        File tempDir = REDPreferences.getInstance().tempDirectory();

        if (tempDir == null) {
            JLabel tempLabel = new JLabel(IconUtils.ERRORICON);
            add(tempLabel, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            gridBagConstraints.weightx = 0.999;
            add(new JLabel(InfoPanelUtils.CACHE_DIRECTORY_CONFIGURE,
                    JLabel.LEFT), gridBagConstraints);
            gridBagConstraints.gridx = 2;
            gridBagConstraints.weightx = 0.001;
            add(setTempDirButton, gridBagConstraints);
            invalidCacheDirectory = true;
        } else if (!(tempDir.exists() && tempDir.isDirectory()
                && tempDir.canRead() && tempDir.canWrite() && tempDir
                .listFiles() != null)) {
            JLabel tempLabel = new JLabel(IconUtils.ERRORICON);
            add(tempLabel, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            gridBagConstraints.weightx = 0.999;
            add(new JLabel(InfoPanelUtils.CACHE_DIRECTORY_CONFIGURE_FAIL,
                    JLabel.LEFT), gridBagConstraints);
            gridBagConstraints.gridx = 2;
            gridBagConstraints.weightx = 0.001;
            add(setTempDirButton, gridBagConstraints);
            invalidCacheDirectory = true;
        } else {
            // Check if we can actually write something into the cache directory
            // (don't just trust that we can)

            try {
                File tempFile = File.createTempFile("red_test_data",
                        ".temp", REDPreferences.getInstance().tempDirectory());
                FileOutputStream fis = new FileOutputStream(tempFile);
                fis.write(123456789);
                fis.close();
                tempFile.delete();
            } catch (IOException ioe) {
                // Something failed when trying to use the cache directory
                JLabel tempLabel = new JLabel(IconUtils.ERRORICON);
                add(tempLabel, gridBagConstraints);
                gridBagConstraints.gridx = 1;
                gridBagConstraints.weightx = 0.999;
                add(new JLabel(
                        "A test write to your cache directory failed ("
                                + ioe.getLocalizedMessage()
                                + "). Please configure a cache directory to allow RED to run.",
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
                for (int f = 0; f < tempFiles.length; f++) {
                    if (tempFiles[f].isFile()
                            && tempFiles[f].getName().startsWith("red")
                            && tempFiles[f].getName().endsWith(".temp")) {
                        staleFiles++;
                    }
                }
                if (staleFiles > 0) {
                    JLabel tempLabel = new JLabel(IconUtils.WARNINGICON);
                    add(tempLabel, gridBagConstraints);
                    gridBagConstraints.gridx = 1;
                    gridBagConstraints.weightx = 0.999;
                    add(new JLabel(
                            "Disk caching is available and enabled - but you have "
                                    + staleFiles + " stale temp files",
                            JLabel.LEFT), gridBagConstraints);
                    gridBagConstraints.gridx = 2;
                    gridBagConstraints.weightx = 0.001;
                    add(removeStaleFilesButton, gridBagConstraints);
                } else {
                    JLabel tempLabel = new JLabel(IconUtils.TICKICON);
                    add(tempLabel, gridBagConstraints);
                    gridBagConstraints.gridx = 1;
                    gridBagConstraints.weightx = 0.999;
                    add(new JLabel("Disk caching is available and enabled",
                            JLabel.LEFT), gridBagConstraints);
                }

                application.cacheFolderChecked();
            }
        }

        gridBagConstraints.gridy++;
        gridBagConstraints.weightx = 0.001;

        // Check the genomes directory

        JLabel genomesLabel = new JLabel(IconUtils.ERRORICON);
        JLabel genomesLabelText = new JLabel(
                InfoPanelUtils.GENOMES_CHECK_FOLDER_DISABLE);

        if (REDPreferences.getInstance().customGenomeBaseUsed()) {
            // They're using a custom genomes folder
            File gb = null;
            try {
                gb = REDPreferences.getInstance().getGenomeBase();
            } catch (FileNotFoundException e) {
                // There is no default genomes folder
                genomesLabel.setIcon(IconUtils.ERRORICON);
                genomesLabelText
                        .setText(InfoPanelUtils.GENOMES_FOLDER_GET_FAIL);
                gridBagConstraints.gridx = 2;
                gridBagConstraints.weightx = 0.001;
                add(setGenomesFolderButton, gridBagConstraints);
            }

            if (!gb.exists()) {
                // There is no default genomes folder
                genomesLabel.setIcon(IconUtils.ERRORICON);
                genomesLabelText
                        .setText(InfoPanelUtils.GENOMES_FOLDER_GET_FAIL);
                gridBagConstraints.gridx = 2;
                gridBagConstraints.weightx = 0.001;
                add(setGenomesFolderButton, gridBagConstraints);
            } else if (!gb.canRead()) {
                // The default genomes folder is present but useless
                genomesLabel.setIcon(IconUtils.ERRORICON);
                genomesLabelText.setText(InfoPanelUtils.PERMISSION_DENIED_READ);
                gridBagConstraints.gridx = 2;
                gridBagConstraints.weightx = 0.001;
                add(setGenomesFolderButton, gridBagConstraints);
            } else if (!gb.canWrite()) {
                // The default genomes folder is present, but we can't import
                // new genomes
                genomesLabel.setIcon(IconUtils.WARNINGICON);
                genomesLabelText
                        .setText(InfoPanelUtils.PERMISSION_DENIED_WRITE);
                gridBagConstraints.gridx = 2;
                gridBagConstraints.weightx = 0.001;
                add(setGenomesFolderButton, gridBagConstraints);
            } else {
                // Everything is OK
                genomesLabel.setIcon(IconUtils.INFOICON);
                genomesLabelText
                        .setText(InfoPanelUtils.GENOMES_USE_CUSTUM_FOLDER);
            }
        } else {
            // They're using the default
            File gb = null;
            try {
                gb = REDPreferences.getInstance().getGenomeBase();

                if (!gb.canRead()) {
                    // The default genomes folder is present but useless
                    genomesLabel.setIcon(IconUtils.ERRORICON);
                    genomesLabelText
                            .setText(InfoPanelUtils.GENOMES_USE_CUSTUM_FOLDER_READ_FAIL);
                    gridBagConstraints.gridx = 2;
                    gridBagConstraints.weightx = 0.001;
                    add(setGenomesFolderButton, gridBagConstraints);
                } else if (!gb.canWrite()) {
                    // The default genomes folder is present, but we can't
                    // import new genomes
                    genomesLabel.setIcon(IconUtils.WARNINGICON);
                    genomesLabelText
                            .setText(InfoPanelUtils.GENOMES_USE_CUSTUM_FOLDER_WRITE_FAIL);
                    gridBagConstraints.gridx = 2;
                    gridBagConstraints.weightx = 0.001;
                    add(setGenomesFolderButton, gridBagConstraints);
                } else {
                    // Everything is OK
                    genomesLabel.setIcon(IconUtils.INFOICON);
                    genomesLabelText
                            .setText(InfoPanelUtils.GENOMES_USE_DEFALT_FOLDER);
                    gridBagConstraints.gridx = 2;
                    gridBagConstraints.weightx = 0.001;
                    add(setGenomesFolderButton, gridBagConstraints);
                }

            } catch (FileNotFoundException e) {
                // There is no default genomes folder
                genomesLabel.setIcon(IconUtils.ERRORICON);
                genomesLabelText
                        .setText(InfoPanelUtils.GENOMES_FOLDER_NOT_EXIST);
                gridBagConstraints.gridx = 2;
                gridBagConstraints.weightx = 0.001;
                add(setGenomesFolderButton, gridBagConstraints);
            }
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
            programUpdateLabel.setIcon(IconUtils.WARNINGICON);
            programUpdateLabelText
                    .setText(InfoPanelUtils.PROGRAM_CHECK_UPDATE_DISABLE);
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {

        // try {
        // Thread.sleep(500);
        // }
        // catch (InterruptedException e1) {}
        // Check for an available update to the SeqMonk Program
        try {

            if (UpdateChecker.isUpdateAvailable()) {

                String latestVersion = UpdateChecker.getLatestVersionNumber();

                programUpdateLabel.setIcon(IconUtils.WARNINGICON);
                programUpdateLabelText.setText("A newer version of RED (v"
                        + latestVersion + ") is available");
            } else {
                if (REDApplication.VERSION.contains("devel")) {
                    programUpdateLabel.setIcon(IconUtils.WARNINGICON);
                    programUpdateLabelText
                            .setText("You are running a current development version of RED");
                } else {
                    programUpdateLabel.setIcon(IconUtils.TICKICON);
                    programUpdateLabelText
                            .setText("You are running the latest version of RED");
                }
            }
        } catch (Exception e) {
            programUpdateLabel.setIcon(IconUtils.ERRORICON);
            programUpdateLabelText
                    .setText("Failed to check for SeqMonk updates");
            e.printStackTrace();
        }

    }

    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals(InfoPanelUtils.CACHE_DIRECTORY_SET)) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select a Cache Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                REDPreferences.getInstance().setTempDirectory(
                        chooser.getSelectedFile());
                try {
                    REDPreferences.getInstance().savePreferences();
                    populatePanel();
                } catch (IOException ioe) {
                    new CrashReporter(ioe);
                }
            }
        }
        if (e.getActionCommand().equals(
                InfoPanelUtils.GENOMES_CUSTOM_FOLDER_SET)) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select a Genomes Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                REDPreferences.getInstance().setGenomeBase(
                        chooser.getSelectedFile());
                try {
                    REDPreferences.getInstance().savePreferences();
                    populatePanel();
                } catch (IOException ioe) {
                    new CrashReporter(ioe);
                }
            }
        } else if (e.getActionCommand().equals(
                InfoPanelUtils.CACHE_OLD_FILES_DETELE)) {
            int answer = JOptionPane
                    .showConfirmDialog(
                            this,
                            "Please close any other running instances of RED before cleaning the cache",
                            "Cleaning cache", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.CANCEL_OPTION)
                return;

            File[] tempFiles = REDPreferences.getInstance().tempDirectory()
                    .listFiles();

            // int deletedCount = 0;
            for (int f = 0; f < tempFiles.length; f++) {
                if (tempFiles[f].isFile()
                        && tempFiles[f].getName().startsWith("red")
                        && tempFiles[f].getName().endsWith(".temp")) {
                    if (tempFiles[f].delete()) {
                        // ++deletedCount;
                    }
                }
            }

            // JOptionPane.showMessageDialog(this,
            // "Deleted "+deletedCount+" old cache files", "Cleaned Cache",
            // JOptionPane.INFORMATION_MESSAGE);
            populatePanel();
        }
    }
}