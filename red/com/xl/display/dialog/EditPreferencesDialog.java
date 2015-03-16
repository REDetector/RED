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

package com.xl.display.dialog;

import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;
import com.xl.preferences.REDPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Xing Li on 2014/6/25.
 * <p/>
 * A Dialog to allow viewing and editing of all RED preferences.
 */
public class EditPreferencesDialog extends JDialog implements ActionListener {
    /**
     * RED preference.
     */
    REDPreferences preferences = REDPreferences.getInstance();
    /**
     * Location preference.
     */
    LocationPreferences locationPreferences = LocationPreferences.getInstance();
    /**
     * The data location.
     */
    private JTextField projectDataDirectory;
    /**
     * Fasta file directory.
     */
    private JTextField fastaDirectory;
    /**
     * The genome base directory.
     */
    private JTextField genomeDirectory;
    /**
     * RNA directory.
     */
    private JTextField rnaDirectory;
    /**
     * DNA directory.
     */
    private JTextField dnaDirectory;
    /**
     * Annotation directory.
     */
    private JTextField annotationDirectory;
    /**
     * The temp directory.
     */
    private JTextField tempDirectory;
    /**
     * The others directory.
     */
    private JTextField othersDirectory;
    /**
     * The proxy host.
     */
    private JTextField proxyHost;
    /**
     * The proxy port.
     */
    private JTextField proxyPort;
    /**
     * The check for updates.
     */
    private JCheckBox checkForUpdates;

    /**
     * Instantiates a new preferences dialog.
     */
    public EditPreferencesDialog() {
        super(REDApplication.getInstance(), "Edit Preferences...");
        setSize(600, 300);
        setLocationRelativeTo(REDApplication.getInstance());
        setModal(true);

        JTabbedPane tabs = new JTabbedPane();

        JPanel filePanel = new JPanel();
        filePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        filePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        JLabel projectDataLabel = new JLabel("Project Data Directory");
        projectDataDirectory = new JTextField();
        projectDataDirectory.setText(locationPreferences.getProjectDataDirectory());
        projectDataDirectory.setEditable(false);
        JButton projectDataButton = new JButton("Browse");
        projectDataButton.setActionCommand(LocationPreferences.PROJECT_DATA_DIRECTORY);
        projectDataButton.addActionListener(this);
        addItem(c, filePanel, projectDataLabel, projectDataDirectory, projectDataButton);

        c.gridy++;
        JLabel genomeLabel = new JLabel("Genome Directory");
        genomeDirectory = new JTextField();
        genomeDirectory.setText(locationPreferences.getGenomeDirectory());
        genomeDirectory.setEditable(false);
        JButton genomeButton = new JButton("Browse");
        genomeButton.setActionCommand(LocationPreferences.GENOME_DIRECTORY);
        genomeButton.addActionListener(this);
        addItem(c, filePanel, genomeLabel, genomeDirectory, genomeButton);

        c.gridy++;
        JLabel fastaLabel = new JLabel("Fasta Directory");
        fastaDirectory = new JTextField();
        fastaDirectory.setText(locationPreferences.getFastaDirectory());
        fastaDirectory.setEditable(false);
        JButton fastaButton = new JButton("Browse");
        fastaButton.setActionCommand(LocationPreferences.FASTA_DIRECTORY);
        fastaButton.addActionListener(this);
        addItem(c, filePanel, fastaLabel, fastaDirectory, fastaButton);

        c.gridy++;
        JLabel rnaLabel = new JLabel("RNA Directory");
        rnaDirectory = new JTextField();
        rnaDirectory.setText(locationPreferences.getRnaDirectory());
        rnaDirectory.setEditable(false);
        JButton rnaButton = new JButton("Browse");
        rnaButton.setActionCommand(LocationPreferences.RNA_DIRECTORY);
        rnaButton.addActionListener(this);
        addItem(c, filePanel, rnaLabel, rnaDirectory, rnaButton);

        c.gridy++;
        JLabel dnaLabel = new JLabel("DNA Directory");
        dnaDirectory = new JTextField();
        dnaDirectory.setText(locationPreferences.getDnaDirectory());
        dnaDirectory.setEditable(false);
        JButton dnaButton = new JButton("Browse");
        dnaButton.setActionCommand(LocationPreferences.DNA_DIRECTORY);
        dnaButton.addActionListener(this);
        addItem(c, filePanel, dnaLabel, dnaDirectory, dnaButton);

        c.gridy++;
        JLabel annotationLabel = new JLabel("Annotation Directory");
        annotationDirectory = new JTextField();
        annotationDirectory.setText(locationPreferences.getAnnotationDirectory());
        annotationDirectory.setEditable(false);
        JButton annotationButton = new JButton("Browse");
        annotationButton.setActionCommand(LocationPreferences.ANNOTATION_DIRECTORY);
        annotationButton.addActionListener(this);
        addItem(c, filePanel, annotationLabel, annotationDirectory, annotationButton);

        c.gridy++;
        JLabel othersLabel = new JLabel("Others Directory");
        othersDirectory = new JTextField();
        othersDirectory.setText(locationPreferences.getOthersDirectory());
        othersDirectory.setEditable(false);
        JButton othersButton = new JButton("Browse");
        othersButton.setActionCommand(LocationPreferences.OTHERS_DIRECTORY);
        othersButton.addActionListener(this);
        addItem(c, filePanel, othersLabel, othersDirectory, othersButton);

        c.gridy++;
        JLabel tempLabel = new JLabel("Temp Directory");
        tempDirectory = new JTextField();
        tempDirectory.setText(locationPreferences.getTempDirectory());
        tempDirectory.setEditable(false);
        JButton tempButton = new JButton("Browse");
        tempButton.setActionCommand(LocationPreferences.TEMP_DIRECTORY);
        tempButton.addActionListener(this);
        addItem(c, filePanel, tempLabel, tempDirectory, tempButton);

        tabs.addTab("Files", filePanel);

        JPanel networkPanel = new JPanel();
        networkPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        networkPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;

        networkPanel.add(new JLabel("HTTP Proxy server"), c);
        c.gridx = 1;
        c.weightx = 0.5;
        proxyHost = new JTextField(preferences.proxyHost());
        networkPanel.add(proxyHost, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.1;
        networkPanel.add(new JLabel("HTTP Proxy port"), c);
        c.gridx = 1;
        c.weightx = 0.5;
        proxyPort = new JTextField("" + preferences.proxyPort());
        networkPanel.add(proxyPort, c);

        tabs.addTab("Network", networkPanel);

        JPanel updatesPanel = new JPanel();
        updatesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        updatesPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;

        updatesPanel.add(new JLabel("Check for updates on startup"), c);
        c.gridx = 1;
        c.weightx = 0.5;
        checkForUpdates = new JCheckBox();
        checkForUpdates.setSelected(preferences.checkForUpdates());
        updatesPanel.add(checkForUpdates, c);

        tabs.addTab("Updates", updatesPanel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JButton okButton = new JButton("Save");
        okButton.setActionCommand("save");
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addItem(GridBagConstraints c, JPanel filePanel, JLabel jLabel, JTextField jTextField, JButton jButton) {
        c.gridx = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        filePanel.add(jLabel, c);
        c.gridx = 1;
        c.weightx = 0.5;
        filePanel.add(jTextField, c);
        c.gridx = 2;
        c.weightx = 0.1;
        filePanel.add(jButton, c);
    }

    /**
     * Launches a file browser to select a directory
     *
     * @param action The action.
     * @param f      the TextFild from which to take the starting directory
     */
    private void getDir(String action, JTextField f) {
        JFileChooser chooser = new JFileChooser(locationPreferences.getProjectSaveLocation());
        chooser.setCurrentDirectory(new File(f.getText()));
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            f.setText(chooser.getSelectedFile().getAbsolutePath());
            File file = chooser.getSelectedFile();
            LocationPreferences.getInstance().getDirectories().put(action, file.getAbsolutePath());
            LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        String action = ae.getActionCommand();

        if (action.equals(LocationPreferences.PROJECT_DATA_DIRECTORY)) {
            getDir(action, projectDataDirectory);
        } else if (action.equals(LocationPreferences.GENOME_DIRECTORY)) {
            getDir(action, genomeDirectory);
        } else if (action.equals(LocationPreferences.FASTA_DIRECTORY)) {
            getDir(action, fastaDirectory);
        } else if (action.equals(LocationPreferences.RNA_DIRECTORY)) {
            getDir(action, rnaDirectory);
        } else if (action.equals(LocationPreferences.DNA_DIRECTORY)) {
            getDir(action, dnaDirectory);
        } else if (action.equals(LocationPreferences.ANNOTATION_DIRECTORY)) {
            getDir(action, annotationDirectory);
        } else if (action.equals(LocationPreferences.OTHERS_DIRECTORY)) {
            getDir(action, othersDirectory);
        } else if (action.equals(LocationPreferences.TEMP_DIRECTORY)) {
            getDir(action, tempDirectory);
        } else if (action.equals("cancel")) {
            setVisible(false);
            dispose();
        } else if (action.equals("save")) {
            Collection<String> allDirectories = locationPreferences.getDirectories().values();
            for (String directory : allDirectories) {
                File f = new File(directory);
                if (!f.exists()) {
                    JOptionPane.showMessageDialog(this, "Invalid location :" + directory, "Error", JOptionPane.ERROR_MESSAGE);
                    locationPreferences.initialDirectories();
                    return;
                }
            }

            String proxyHostValue = proxyHost.getText();
            int proxyPortValue = 0;
            if (proxyPort.getText().length() > 0) {
                try {
                    proxyPortValue = Integer.parseInt(proxyPort.getText());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Proxy port number was not an integer", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (proxyHostValue.length() > 0 && proxyPort.getText().length() == 0) {
                JOptionPane.showMessageDialog(this, "You specified a proxy server address, but did not provide the port number (default is usually 80 or 8080)", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // OK that's everything which could have gone wrong.  Let's save it to the preferences file
            preferences.setCheckForUpdates(checkForUpdates.isSelected());
            preferences.setProxy(proxyHostValue, proxyPortValue);

            locationPreferences.updateDirectories();

            try {
                preferences.savePreferences();
            } catch (IOException e) {
                new CrashReporter(e);
                return;
            }
            setVisible(false);
        }
    }

}
