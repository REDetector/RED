package com.xl.dialog;

/**
 * Created by Administrator on 2014/6/25.
 */

import com.xl.help.HelpDialog;
import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;
import com.xl.preferences.REDPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * A Dialog to allow the viewing and editing of all SeqMonk preferences.
 */
public class EditPreferencesDialog extends JDialog implements ActionListener {

    REDPreferences preferences = REDPreferences.getInstance();
    LocationPreferences locationPreferences = LocationPreferences.getInstance();

    /**
     * The data location.
     */
    private JTextField projectDataDirectory;

    private JTextField fastaDirectory;

    /**
     * The genome base.
     */
    private JTextField genomeDirectory;

    private JTextField rnaDirectory;

    private JTextField dnaDirectory;

    private JTextField annotationDirectory;

    /**
     * The temp directory.
     */
    private JTextField tempDirectory;

    private JTextField othersDirectory;

    private JTextField cacheDirectory;

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
    private JTextField downloadLists;

    /**
     * The check for updates.
     */
    private JCheckBox checkForUpdates;

    private JTextField crashEmail;

    /**
     * Whether to compress output
     */
    private JCheckBox compressOutput;


    /**
     * Instantiates a new edits the preferences dialog.
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
        JLabel projectDataLable = new JLabel("Project Data Directory");
        projectDataDirectory = new JTextField();
        projectDataDirectory.setText(locationPreferences.getProjectDataDirectory());
        projectDataDirectory.setEditable(false);
        JButton projectDataButton = new JButton("Browse");
        projectDataButton.setActionCommand(LocationPreferences.PROJECT_DATA_DIRECTORY);
        projectDataButton.addActionListener(this);
        addItem(c, filePanel, projectDataLable, projectDataDirectory, projectDataButton);

        c.gridy++;
        JLabel genomeLable = new JLabel("Genome Directory");
        genomeDirectory = new JTextField();
        genomeDirectory.setText(locationPreferences.getGenomeDirectory());
        genomeDirectory.setEditable(false);
        JButton genomeButton = new JButton("Browse");
        genomeButton.setActionCommand(LocationPreferences.GENOME_DIRECTORY);
        genomeButton.addActionListener(this);
        addItem(c, filePanel, genomeLable, genomeDirectory, genomeButton);

        c.gridy++;
        JLabel fastaLable = new JLabel("Fasta Directory");
        fastaDirectory = new JTextField();
        fastaDirectory.setText(locationPreferences.getFastaDirectory());
        fastaDirectory.setEditable(false);
        JButton fastaButton = new JButton("Browse");
        fastaButton.setActionCommand(LocationPreferences.FASTA_DIRECTORY);
        fastaButton.addActionListener(this);
        addItem(c, filePanel, fastaLable, fastaDirectory, fastaButton);

        c.gridy++;
        JLabel rnaLable = new JLabel("RNA Directory");
        rnaDirectory = new JTextField();
        rnaDirectory.setText(locationPreferences.getRnaDirectory());
        rnaDirectory.setEditable(false);
        JButton rnaButton = new JButton("Browse");
        rnaButton.setActionCommand(LocationPreferences.RNA_DIRECTORY);
        rnaButton.addActionListener(this);
        addItem(c, filePanel, rnaLable, rnaDirectory, rnaButton);

        c.gridy++;
        JLabel dnaLable = new JLabel("DNA Directory");
        dnaDirectory = new JTextField();
        dnaDirectory.setText(locationPreferences.getDnaDirectory());
        dnaDirectory.setEditable(false);
        JButton dnaButton = new JButton("Browse");
        dnaButton.setActionCommand(LocationPreferences.DNA_DIRECTORY);
        dnaButton.addActionListener(this);
        addItem(c, filePanel, dnaLable, dnaDirectory, dnaButton);

        c.gridy++;
        JLabel annotationLable = new JLabel("Annotation Directory");
        annotationDirectory = new JTextField();
        annotationDirectory.setText(locationPreferences.getAnnotationDirectory());
        annotationDirectory.setEditable(false);
        JButton annotationButton = new JButton("Browse");
        annotationButton.setActionCommand(LocationPreferences.ANNOTATION_DIRECTORY);
        annotationButton.addActionListener(this);
        addItem(c, filePanel, annotationLable, annotationDirectory, annotationButton);

        c.gridy++;
        JLabel othersLable = new JLabel("Others Directory");
        othersDirectory = new JTextField();
        othersDirectory.setText(locationPreferences.getOthersDirectory());
        othersDirectory.setEditable(false);
        JButton othersButton = new JButton("Browse");
        othersButton.setActionCommand(LocationPreferences.OTHERS_DIRECTORY);
        othersButton.addActionListener(this);
        addItem(c, filePanel, othersLable, othersDirectory, othersButton);

        c.gridy++;
        JLabel tempLable = new JLabel("Temp Directory");
        tempDirectory = new JTextField();
        tempDirectory.setText(locationPreferences.getTempDirectory());
        tempDirectory.setEditable(false);
        JButton tempButton = new JButton("Browse");
        tempButton.setActionCommand(LocationPreferences.TEMP_DIRECTORY);
        tempButton.addActionListener(this);
        addItem(c, filePanel, tempLable, tempDirectory, tempButton);

        tabs.addTab("Files", filePanel);


        JPanel memoryPanel = new JPanel();
        memoryPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        memoryPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();

        c.gridy = 0;
        JLabel cacheLable = new JLabel("Cache read data to disk");
        cacheDirectory = new JTextField();
        cacheDirectory.setText(locationPreferences.getCacheDirectory());
        cacheDirectory.setEditable(false);
        JButton cacheButton = new JButton("Browse");
        cacheButton.setActionCommand(LocationPreferences.CACHE_DIRECTORY);
        cacheButton.addActionListener(this);
        addItem(c, memoryPanel, cacheLable, cacheDirectory, cacheButton);

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
        downloadLists = new JTextField(locationPreferences.getGenomeDownloadLists());
        networkPanel.add(downloadLists, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.1;
        networkPanel.add(new JLabel("Email Address:"), c);
        c.gridx = 1;
        c.weightx = 0.5;
        crashEmail = new JTextField(preferences.getCrashEmail());
        networkPanel.add(crashEmail, c);

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

    private void addItem(GridBagConstraints c, JPanel filePanel, JLabel jLable, JTextField jTextField, JButton jButton) {
        c.gridx = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        filePanel.add(jLable, c);
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
        } else if (action.equals(LocationPreferences.CACHE_DIRECTORY)) {
            getDir(action, cacheDirectory);
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


            // OK that's everything which could have gone wrong.  Let's save it
            // to the preferences file


            preferences.setCheckForUpdates(checkForUpdates.isSelected());
            preferences.setCompressOutput(compressOutput.isSelected());
            preferences.setProxy(proxyHostValue, proxyPortValue);
            preferences.setCrashEmail(crashEmail.getText());

            locationPreferences.setGenomeDownloadLists(downloadLists.getText());
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
