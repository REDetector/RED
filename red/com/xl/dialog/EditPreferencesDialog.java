package com.xl.dialog;

/**
 * Created by Administrator on 2014/6/25.
 */

import com.xl.help.HelpDialog;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A Dialog to allow the viewing and editing of all SeqMonk preferences.
 */
public class EditPreferencesDialog extends JDialog implements ActionListener {

    /**
     * The genome base.
     */
    private JTextField genomeBase;

    /**
     * The data location.
     */
    private JTextField dataLocation;

    /**
     * The save location.
     */
    private JTextField saveLocation;

    /**
     * The proxy host.
     */
    private JTextField proxyHost;

    /**
     * The proxy port.
     */
    private JTextField proxyPort;

    /**
     * The download location.
     */
    private JTextField downloadLocation;

    /**
     * The check for updates.
     */
    private JCheckBox checkForUpdates;

    /**
     * Whether to compress output
     */
    private JCheckBox compressOutput;

    /**
     * The temp directory.
     */
    private JTextField tempDirectory;


    /**
     * Instantiates a new edits the preferences dialog.
     */
    public EditPreferencesDialog() {
        super(REDApplication.getInstance(), "Edit Preferences...");
        setSize(600, 280);
        setLocationRelativeTo(REDApplication.getInstance());
        setModal(true);
        REDPreferences preferences = REDPreferences.getInstance();

        JTabbedPane tabs = new JTabbedPane();

        JPanel filePanel = new JPanel();
        filePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        filePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        filePanel.add(new JLabel("Genome Base Location"), c);
        c.gridx = 1;
        c.weightx = 0.5;
        genomeBase = new JTextField();
        try {
            genomeBase.setText(preferences.getGenomeBase().getAbsolutePath());
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Couldn't find the folder which was supposed to hold the genomes", "Warning", JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }
        genomeBase.setEditable(false);
        filePanel.add(genomeBase, c);
        c.gridx = 2;
        c.weightx = 0.1;
        JButton genomeButton = new JButton("Browse");
        genomeButton.setActionCommand("genomeBase");
        genomeButton.addActionListener(this);
        filePanel.add(genomeButton, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.1;
        filePanel.add(new JLabel("Default Data Location"), c);
        c.gridx = 1;
        c.weightx = 0.5;
        dataLocation = new JTextField(preferences.getDataLocationPreference().getAbsolutePath());
        dataLocation.setEditable(false);
        filePanel.add(dataLocation, c);
        c.gridx = 2;
        c.weightx = 0.1;
        JButton dataButton = new JButton("Browse");
        dataButton.setActionCommand("dataLocation");
        dataButton.addActionListener(this);
        filePanel.add(dataButton, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.1;
        filePanel.add(new JLabel("Default Save Location"), c);
        c.gridx = 1;
        c.weightx = 0.5;
        saveLocation = new JTextField(preferences.getSaveLocationPreference().getAbsolutePath());
        saveLocation.setEditable(false);
        filePanel.add(saveLocation, c);
        c.gridx = 2;
        c.weightx = 0.1;
        JButton saveLocationButton = new JButton("Browse");
        saveLocationButton.setActionCommand("saveLocation");
        saveLocationButton.addActionListener(this);
        filePanel.add(saveLocationButton, c);

        tabs.addTab("Files", filePanel);

        JPanel memoryPanel = new JPanel();
        memoryPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));


        memoryPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;

        memoryPanel.add(new JLabel("Cache read data to disk"), c);
        c.gridx = 1;
        c.weightx = 0.5;
        JPanel tempDirPanel = new JPanel();
        tempDirPanel.setLayout(new BorderLayout());

        tempDirectory = new JTextField();
        if (preferences.tempDirectory() != null) {
            tempDirectory.setText(preferences.tempDirectory().getAbsolutePath());
        }
        tempDirectory.setEditable(false);
        tempDirPanel.add(tempDirectory, BorderLayout.CENTER);
        JButton tempDirBrowseButton = new JButton("Browse");
        tempDirBrowseButton.setActionCommand("tempDir");
        tempDirBrowseButton.addActionListener(this);
        tempDirPanel.add(tempDirBrowseButton, BorderLayout.EAST);

        memoryPanel.add(tempDirPanel, c);

        c.gridx = 0;
        c.gridy++;
        memoryPanel.add(new JLabel("Compress Output"), c);
        c.gridx = 1;
        compressOutput = new JCheckBox();
        compressOutput.setSelected(preferences.compressOutput());
        memoryPanel.add(compressOutput, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        JTextArea startUpMemory = new JTextArea("See also the help section on memory usage.");
        startUpMemory.setWrapStyleWord(true);
        startUpMemory.setLineWrap(true);
        startUpMemory.setEditable(false);
        startUpMemory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startUpMemory.setToolTipText("Click here to enter help content.");
        startUpMemory.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new HelpDialog(new File(ClassLoader.getSystemResource("Help")
                        .getFile().replaceAll("%20", " "))).requestFocusInWindow();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        startUpMemory.setBackground(memoryPanel.getBackground());
        memoryPanel.add(startUpMemory, c);
        tabs.addTab("Memory", memoryPanel);


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

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.1;
        networkPanel.add(new JLabel("Genome Download URL"), c);
        c.gridx = 1;
        c.weightx = 0.5;
        downloadLocation = new JTextField(preferences.getGenomeDownloadLocation());
        networkPanel.add(downloadLocation, c);
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

        JButton okButton = new JButton("Save");
        okButton.setActionCommand("save");
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Launches a file browser to select a directory
     *
     * @param f the TextFild from which to take the starting directory
     * @return the selected directory
     */
    private void getDir(JTextField f) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(f.getText()));
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            f.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        String c = ae.getActionCommand();

        if (c.equals("genomeBase")) {
            getDir(genomeBase);
        } else if (c.equals("dataLocation")) {
            getDir(dataLocation);
        } else if (c.equals("saveLocation")) {
            getDir(saveLocation);
        } else if (c.equals("tempDir")) {
            getDir(tempDirectory);
        } else if (c.equals("cancel")) {
            setVisible(false);
            dispose();
        } else if (c.equals("save")) {
            File genomeBaseFile = new File(genomeBase.getText());
            if (!genomeBaseFile.exists()) {
                JOptionPane.showMessageDialog(this, "Invalid genome base location", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File dataLocationFile = new File(dataLocation.getText());
            if (!dataLocationFile.exists()) {
                JOptionPane.showMessageDialog(this, "Invalid data location", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File saveLocationFile = new File(saveLocation.getText());
            if (!saveLocationFile.exists()) {
                JOptionPane.showMessageDialog(this, "Invalid save location", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File tempDirFile;
            if (tempDirectory.getText().length() > 0) {
                tempDirFile = new File(tempDirectory.getText());
                if (!tempDirFile.exists()) {
                    JOptionPane.showMessageDialog(this, "Invalid temp dir", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(this, "No temp dir specified", "Error", JOptionPane.ERROR_MESSAGE);
                return;
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


            // OK that's everything which could have gone wrong.  Let's save it
            // to the preferences file

            REDPreferences p = REDPreferences.getInstance();

            p.setCheckForUpdates(checkForUpdates.isSelected());
            p.setDataLocation(dataLocationFile);
            p.setSaveLocation(saveLocationFile);
            p.setGenomeBase(genomeBaseFile);
            p.setProxy(proxyHostValue, proxyPortValue);
            p.setGenomeDownloadLocation(downloadLocation.getText());
            p.setTempDirectory(tempDirFile);
            p.setCompressOutput(compressOutput.isSelected());

            try {
                p.savePreferences();
            } catch (IOException e) {
                new CrashReporter(e);
                return;
            }
            setVisible(false);
        }
    }

}
