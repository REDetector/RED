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


import com.xl.display.panel.DataIntroductionPanel;
import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;
import com.xl.thread.ThreadDenovoInput;
import com.xl.thread.ThreadDnaRnaInput;
import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by Xing Li on 2014/6/25.
 * <p/>
 * A Dialog for importing data to MySQL database.
 */
public class DataImportDialog extends JDialog implements ActionListener {
    /**
     * Location preferences.
     */
    private LocationPreferences preferences = LocationPreferences.getInstance();
    /**
     * RScript or RExecutable path.
     */
    private JTextField rScriptPath;
    private JTextField denovoRScriptPath;
    /**
     * RNA VCF file path.
     */
    private JTextField rnaVcfFileField;
    private JTextField denovoRnaVcfFileField;
    /**
     * DNA VCF file path.
     */
    private JTextField dnaVcfFileField;
    /**
     * Repeat regions file path.
     */
    private JTextField repeatFileField;
    private JTextField denovoRepeatFileField;
    /**
     * Gene annotation file path.
     */
    private JTextField refSeqFileField;
    private JTextField denovoRefSeqFileField;
    /**
     * dbSNP database file path.
     */
    private JTextField dbSNPFileField;
    private JTextField denovoDbSNPFileField;
    /**
     * DARNED database file path.
     */
    private JTextField darnedFileField;
    private JTextField denovoDarnedFileField;

    private JTabbedPane tabs = new JTabbedPane();

    /**
     * Instantiates a new data import dialog.
     */
    public DataImportDialog(REDApplication application) {
        super(application, "Import Data into Database...");
        setSize(600, 300);
        setLocationRelativeTo(REDApplication.getInstance());
        setModal(true);
        getRootPane().setLayout(new BorderLayout());

        String rScript = preferences.getRScriptPath();
        String rnaVcfFile = preferences.getRnaVcfFile();
        String dnaVcfFile = preferences.getDnaVcfFile();
        String repeatFile = preferences.getRepeatFile();
        String refSeqFile = preferences.getRefSeqFile();
        String dbSNPFile = preferences.getDbSNPFile();
        String darnedFile = preferences.getDarnedFile();

        JPanel nonDenovoPanel = new JPanel();
        nonDenovoPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        nonDenovoPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        JLabel rScriptLabel = new JLabel(LocationPreferences.R_SCRIPT_PATH);
        rScriptPath = new JTextField();
        if (rScript != null) {
            rScriptPath.setText(rScript);
        } else {
            rScriptPath.setText("");
        }
        rScriptPath.setEditable(false);
        JButton rScriptButton = new JButton(MenuUtils.BROWSE_BUTTON);
        rScriptButton.setActionCommand(LocationPreferences.R_SCRIPT_PATH);
        rScriptButton.addActionListener(this);
        addItem(c, nonDenovoPanel, rScriptLabel, rScriptPath, rScriptButton);

        c.gridy++;
        JLabel rnaVcfLabel = new JLabel(LocationPreferences.RNA_VCF_FILE);
        rnaVcfFileField = new JTextField();
        if (rnaVcfFile != null) {
            rnaVcfFileField.setText(rnaVcfFile);
        } else {
            rnaVcfFileField.setText("");
        }
        rnaVcfFileField.setEditable(false);
        JButton rnaVcfButton = new JButton(MenuUtils.BROWSE_BUTTON);
        rnaVcfButton.setActionCommand(LocationPreferences.RNA_VCF_FILE);
        rnaVcfButton.addActionListener(this);
        addItem(c, nonDenovoPanel, rnaVcfLabel, rnaVcfFileField, rnaVcfButton);

        c.gridy++;
        JLabel dnaVcfLabel = new JLabel(LocationPreferences.DNA_VCF_FILE);
        dnaVcfFileField = new JTextField();
        if (dnaVcfFile != null) {
            dnaVcfFileField.setText(dnaVcfFile);
        } else {
            dnaVcfFileField.setText("");
        }
        dnaVcfFileField.setEditable(false);
        JButton dnaVcfButton = new JButton(MenuUtils.BROWSE_BUTTON);
        dnaVcfButton.setActionCommand(LocationPreferences.DNA_VCF_FILE);
        dnaVcfButton.addActionListener(this);
        addItem(c, nonDenovoPanel, dnaVcfLabel, dnaVcfFileField, dnaVcfButton);

        c.gridy++;
        JLabel repeatLabel = new JLabel(LocationPreferences.REPEAT_FILE);
        repeatFileField = new JTextField();
        if (repeatFile != null) {
            repeatFileField.setText(repeatFile);
        } else {
            repeatFileField.setText("");
        }
        repeatFileField.setEditable(false);
        JButton repeatButton = new JButton(MenuUtils.BROWSE_BUTTON);
        repeatButton.setActionCommand(LocationPreferences.REPEAT_FILE);
        repeatButton.addActionListener(this);
        addItem(c, nonDenovoPanel, repeatLabel, repeatFileField, repeatButton);

        c.gridy++;
        JLabel refSeqLabel = new JLabel(LocationPreferences.REF_SEQ_FILE);
        refSeqFileField = new JTextField();
        if (refSeqFile != null) {
            refSeqFileField.setText(refSeqFile);
        } else {
            refSeqFileField.setText("");
        }
        refSeqFileField.setEditable(false);
        JButton refSeqButton = new JButton(MenuUtils.BROWSE_BUTTON);
        refSeqButton.setActionCommand(LocationPreferences.REF_SEQ_FILE);
        refSeqButton.addActionListener(this);
        addItem(c, nonDenovoPanel, refSeqLabel, refSeqFileField, refSeqButton);

        c.gridy++;
        JLabel dbSNPLabel = new JLabel(LocationPreferences.DBSNP_FILE);
        dbSNPFileField = new JTextField();
        if (dbSNPFile != null) {
            dbSNPFileField.setText(dbSNPFile);
        } else {
            dbSNPFileField.setText("");
        }
        dbSNPFileField.setEditable(false);
        JButton dbSNPButton = new JButton(MenuUtils.BROWSE_BUTTON);
        dbSNPButton.setActionCommand(LocationPreferences.DBSNP_FILE);
        dbSNPButton.addActionListener(this);
        addItem(c, nonDenovoPanel, dbSNPLabel, dbSNPFileField, dbSNPButton);

        c.gridy++;
        JLabel darnedLabel = new JLabel(LocationPreferences.DARNED_FILE);
        darnedFileField = new JTextField();
        if (darnedFile != null) {
            darnedFileField.setText(darnedFile);
        } else {
            darnedFileField.setText("");
        }
        darnedFileField.setEditable(false);
        JButton darnedButton = new JButton(MenuUtils.BROWSE_BUTTON);
        darnedButton.setActionCommand(LocationPreferences.DARNED_FILE);
        darnedButton.addActionListener(this);
        addItem(c, nonDenovoPanel, darnedLabel, darnedFileField, darnedButton);
        tabs.addTab("DNA-RNA Mode", nonDenovoPanel);


        JPanel denovoPanel = new JPanel();
        denovoPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        denovoPanel.setLayout(new GridBagLayout());

        c = new GridBagConstraints();
        c.gridy = 0;
        JLabel denovoRSciptLabel = new JLabel(LocationPreferences.R_SCRIPT_PATH);
        denovoRScriptPath = new JTextField();
        if (rScript != null) {
            denovoRScriptPath.setText(rScript);
        } else {
            denovoRScriptPath.setText("");
        }
        denovoRScriptPath.setEditable(false);
        JButton denovoRScriptButton = new JButton(MenuUtils.BROWSE_BUTTON);
        denovoRScriptButton.setActionCommand(LocationPreferences.R_SCRIPT_PATH);
        denovoRScriptButton.addActionListener(this);
        addItem(c, denovoPanel, denovoRSciptLabel, denovoRScriptPath, denovoRScriptButton);

        c.gridy++;
        JLabel denovoRnaVcfLabel = new JLabel(LocationPreferences.RNA_VCF_FILE);
        denovoRnaVcfFileField = new JTextField();
        denovoRnaVcfFileField.setText(preferences.getRnaVcfFile());
        if (rnaVcfFile != null) {
            denovoRnaVcfFileField.setText(rnaVcfFile);
        } else {
            denovoRnaVcfFileField.setText("");
        }
        denovoRnaVcfFileField.setEditable(false);
        JButton devonoRnaVcfButton = new JButton(MenuUtils.BROWSE_BUTTON);
        devonoRnaVcfButton.setActionCommand(LocationPreferences.RNA_VCF_FILE);
        devonoRnaVcfButton.addActionListener(this);
        addItem(c, denovoPanel, denovoRnaVcfLabel, denovoRnaVcfFileField, devonoRnaVcfButton);

        c.gridy++;
        JLabel denovoRepeatLabel = new JLabel(LocationPreferences.REPEAT_FILE);
        denovoRepeatFileField = new JTextField();
        if (repeatFile != null) {
            denovoRepeatFileField.setText(repeatFile);
        } else {
            denovoRepeatFileField.setText("");
        }
        denovoRepeatFileField.setEditable(false);
        JButton denovoRepeatButton = new JButton(MenuUtils.BROWSE_BUTTON);
        denovoRepeatButton.setActionCommand(LocationPreferences.REPEAT_FILE);
        denovoRepeatButton.addActionListener(this);
        addItem(c, denovoPanel, denovoRepeatLabel, denovoRepeatFileField, denovoRepeatButton);

        c.gridy++;
        JLabel denovoRefSeqLabel = new JLabel(LocationPreferences.REF_SEQ_FILE);
        denovoRefSeqFileField = new JTextField();
        if (refSeqFile != null) {
            denovoRefSeqFileField.setText(refSeqFile);
        } else {
            denovoRefSeqFileField.setText("");
        }
        denovoRefSeqFileField.setEditable(false);
        JButton denovoRefSeqButton = new JButton(MenuUtils.BROWSE_BUTTON);
        denovoRefSeqButton.setActionCommand(LocationPreferences.REF_SEQ_FILE);
        denovoRefSeqButton.addActionListener(this);
        addItem(c, denovoPanel, denovoRefSeqLabel, denovoRefSeqFileField, denovoRefSeqButton);

        c.gridy++;
        JLabel denovoDbSNPLabel = new JLabel(LocationPreferences.DBSNP_FILE);
        denovoDbSNPFileField = new JTextField();
        if (dbSNPFile != null) {
            denovoDbSNPFileField.setText(dbSNPFile);
        } else {
            denovoDbSNPFileField.setText("");
        }
        denovoDbSNPFileField.setEditable(false);
        JButton denovoDbSNPButton = new JButton(MenuUtils.BROWSE_BUTTON);
        denovoDbSNPButton.setActionCommand(LocationPreferences.DBSNP_FILE);
        denovoDbSNPButton.addActionListener(this);
        addItem(c, denovoPanel, denovoDbSNPLabel, denovoDbSNPFileField, denovoDbSNPButton);

        c.gridy++;
        JLabel denovoDarnedLabel = new JLabel(LocationPreferences.DARNED_FILE);
        denovoDarnedFileField = new JTextField();
        if (darnedFile != null) {
            denovoDarnedFileField.setText(darnedFile);
        } else {
            denovoDarnedFileField.setText("");
        }
        denovoDarnedFileField.setEditable(false);
        JButton denovoDarnedButton = new JButton(MenuUtils.BROWSE_BUTTON);
        denovoDarnedButton.setActionCommand(LocationPreferences.DARNED_FILE);
        denovoDarnedButton.addActionListener(this);
        addItem(c, denovoPanel, denovoDarnedLabel, denovoDarnedFileField, denovoDarnedButton);

        tabs.addTab("Denovo Mode", denovoPanel);

        getRootPane().add(tabs, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JButton okButton = new JButton("Import");
        okButton.setActionCommand("import");
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        getRootPane().add(buttonPanel, BorderLayout.SOUTH);

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
     * @param dataType The action.
     * @param f        the TextField from which to take the starting directory
     */
    private void getFile(String dataType, JTextField f) {
        JFileChooser chooser = new JFileChooser(preferences.getProjectSaveLocation());
        chooser.setCurrentDirectory(new File(f.getText()));
        chooser.setAccessory(new DataIntroductionPanel(dataType));
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            f.setText(chooser.getSelectedFile().getAbsolutePath().replaceAll("\\\\", "/"));
            File file = chooser.getSelectedFile();
            if (!dataType.equals(LocationPreferences.getInstance().getRScriptPath()))
                LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());
        }
    }

    public void actionPerformed(ActionEvent ae) {
        String action = ae.getActionCommand();
        final int NON_DENOVO_INDEX = 0;
        final int DENOVO_INDEX = 1;
        int currentIndex = tabs.getSelectedIndex();
        if (action.equals(LocationPreferences.RNA_VCF_FILE)) {
            if (currentIndex == NON_DENOVO_INDEX) {
                getFile(action, rnaVcfFileField);
                preferences.setRnaVcfFile(rnaVcfFileField.getText());
            } else {
                getFile(action, denovoRnaVcfFileField);
                preferences.setRnaVcfFile(denovoRnaVcfFileField.getText());
            }
        } else if (action.equals(LocationPreferences.DNA_VCF_FILE)) {
            getFile(action, dnaVcfFileField);
            preferences.setDnaVcfFile(dnaVcfFileField.getText());
        } else if (action.equals(LocationPreferences.REPEAT_FILE)) {
            if (currentIndex == NON_DENOVO_INDEX) {
                getFile(action, repeatFileField);
                preferences.setRepeatFile(repeatFileField.getText());
            } else {
                getFile(action, denovoRepeatFileField);
                preferences.setRepeatFile(denovoRepeatFileField.getText());
            }
        } else if (action.equals(LocationPreferences.REF_SEQ_FILE)) {
            if (currentIndex == NON_DENOVO_INDEX) {
                getFile(action, refSeqFileField);
                preferences.setRefSeqFile(refSeqFileField.getText());
            } else {
                getFile(action, denovoRefSeqFileField);
                preferences.setRefSeqFile(denovoRefSeqFileField.getText());
            }
        } else if (action.equals(LocationPreferences.DBSNP_FILE)) {
            if (currentIndex == NON_DENOVO_INDEX) {
                getFile(action, dbSNPFileField);
                preferences.setDbSNPFile(dbSNPFileField.getText());
            } else {
                getFile(action, denovoDbSNPFileField);
                preferences.setDbSNPFile(denovoDbSNPFileField.getText());
            }
        } else if (action.equals(LocationPreferences.DARNED_FILE)) {
            if (currentIndex == NON_DENOVO_INDEX) {
                getFile(action, darnedFileField);
                preferences.setDarnedFile(darnedFileField.getText());
            } else {
                getFile(action, denovoDarnedFileField);
                preferences.setDarnedFile(denovoDarnedFileField.getText());
            }

        } else if (action.equals(LocationPreferences.R_SCRIPT_PATH)) {
            if (currentIndex == NON_DENOVO_INDEX) {
                getFile(action, rScriptPath);
                preferences.setRScriptPath(rScriptPath.getText());
            } else {
                getFile(action, denovoRScriptPath);
                preferences.setRScriptPath(denovoRScriptPath.getText());
            }

        } else if (action.equals("cancel")) {
            setVisible(false);
            dispose();
        } else if (action.equals("import")) {
            switch (currentIndex) {
                //DNA-RNA index
                case NON_DENOVO_INDEX:
                    new Thread(new ThreadDnaRnaInput()).start();
                    break;
                //denovo index
                case DENOVO_INDEX:
                    new Thread(new ThreadDenovoInput()).start();
                    break;
            }
            setVisible(false);
            dispose();
        }
    }
}
