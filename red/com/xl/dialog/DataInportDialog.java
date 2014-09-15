package com.xl.dialog;

/**
 * Created by Administrator on 2014/6/25.
 */

import com.xl.main.REDApplication;
import com.xl.panel.DataIntroductionPanel;
import com.xl.preferences.LocationPreferences;
import com.xl.thread.ThreadDenovoInput;
import com.xl.thread.ThreadNonDenovoInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * A Dialog to allow the viewing and editing of all SeqMonk preferences.
 */
public class DataInportDialog extends JDialog implements ActionListener {
    private final int NON_DENOVO_INDEX = 0;
    private final int DENOVO_INDEX = 1;
    private REDApplication application;
    private LocationPreferences preferences = LocationPreferences.getInstance();

    private JTextField rScriptPath;

    private JTextField rnaVcfFileField;

    private JTextField dnaVcfFileField;

    private JTextField repeatFileField;

    private JTextField refSeqFileField;

    private JTextField dbSNPFileField;

    private JTextField denovoRScriptPath;

    private JTextField darnedFileField;

    private JTextField denovoRnaVcfFileField;

    private JTextField denovoRepeatFileField;

    private JTextField denovoRefSeqFileField;

    private JTextField denovoDbSNPFileField;

    private JTextField denovoDarnedFileField;

    private JTabbedPane tabs = new JTabbedPane();

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
     * Instantiates a new edits the preferences dialog.
     */
    public DataInportDialog(REDApplication application) {
        super(application, "Inport Data Into Database...");
        this.application = application;
        setSize(600, 300);
        setLocationRelativeTo(REDApplication.getInstance());
        setModal(true);
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
        JLabel rScriptLable = new JLabel(LocationPreferences.RSCRIPT_PATH);
        rScriptPath = new JTextField();
        if (rScript != null) {
            rScriptPath.setText(rScript);
        } else {
            rScriptPath.setText("");
        }
        rScriptPath.setEditable(false);
        JButton rScriptButton = new JButton("Browse");
        rScriptButton.setActionCommand(LocationPreferences.RSCRIPT_PATH);
        rScriptButton.addActionListener(this);
        addItem(c, nonDenovoPanel, rScriptLable, rScriptPath, rScriptButton);

        c.gridy++;
        JLabel rnaVcfLable = new JLabel(LocationPreferences.RNA_VCF_FILE);
        rnaVcfFileField = new JTextField();
        if (rnaVcfFile != null) {
            rnaVcfFileField.setText(rnaVcfFile);
        } else {
            rnaVcfFileField.setText("");
        }
        rnaVcfFileField.setEditable(false);
        JButton rnaVcfButton = new JButton("Browse");
        rnaVcfButton.setActionCommand(LocationPreferences.RNA_VCF_FILE);
        rnaVcfButton.addActionListener(this);
        addItem(c, nonDenovoPanel, rnaVcfLable, rnaVcfFileField, rnaVcfButton);

        c.gridy++;
        JLabel dnaVcfLable = new JLabel(LocationPreferences.DNA_VCF_FILE);
        dnaVcfFileField = new JTextField();
        if (dnaVcfFile != null) {
            dnaVcfFileField.setText(dnaVcfFile);
        } else {
            dnaVcfFileField.setText("");
        }
        dnaVcfFileField.setEditable(false);
        JButton dnaVcfButton = new JButton("Browse");
        dnaVcfButton.setActionCommand(LocationPreferences.DNA_VCF_FILE);
        dnaVcfButton.addActionListener(this);
        addItem(c, nonDenovoPanel, dnaVcfLable, dnaVcfFileField, dnaVcfButton);

        c.gridy++;
        JLabel repeatLable = new JLabel(LocationPreferences.REPEAT_FILE);
        repeatFileField = new JTextField();
        if (repeatFile != null) {
            repeatFileField.setText(repeatFile);
        } else {
            repeatFileField.setText("");
        }
        repeatFileField.setEditable(false);
        JButton repeatButton = new JButton("Browse");
        repeatButton.setActionCommand(LocationPreferences.REPEAT_FILE);
        repeatButton.addActionListener(this);
        addItem(c, nonDenovoPanel, repeatLable, repeatFileField, repeatButton);

        c.gridy++;
        JLabel refSeqLabel = new JLabel(LocationPreferences.REF_SEQ_FILE);
        refSeqFileField = new JTextField();
        if (refSeqFile != null) {
            refSeqFileField.setText(refSeqFile);
        } else {
            refSeqFileField.setText("");
        }
        refSeqFileField.setEditable(false);
        JButton refSeqButton = new JButton("Browse");
        refSeqButton.setActionCommand(LocationPreferences.REF_SEQ_FILE);
        refSeqButton.addActionListener(this);
        addItem(c, nonDenovoPanel, refSeqLabel, refSeqFileField, refSeqButton);

        c.gridy++;
        JLabel dbsnpLabel = new JLabel(LocationPreferences.DBSNP_FILE);
        dbSNPFileField = new JTextField();
        if (dbSNPFile != null) {
            dbSNPFileField.setText(dbSNPFile);
        } else {
            dbSNPFileField.setText("");
        }
        dbSNPFileField.setEditable(false);
        JButton dbSNPButton = new JButton("Browse");
        dbSNPButton.setActionCommand(LocationPreferences.DBSNP_FILE);
        dbSNPButton.addActionListener(this);
        addItem(c, nonDenovoPanel, dbsnpLabel, dbSNPFileField, dbSNPButton);

        c.gridy++;
        JLabel darnedLabel = new JLabel(LocationPreferences.DARNED_FILE);
        darnedFileField = new JTextField();
        if (darnedFile != null) {
            darnedFileField.setText(darnedFile);
        } else {
            darnedFileField.setText("");
        }
        darnedFileField.setEditable(false);
        JButton darnedButton = new JButton("Browse");
        darnedButton.setActionCommand(LocationPreferences.DARNED_FILE);
        darnedButton.addActionListener(this);
        addItem(c, nonDenovoPanel, darnedLabel, darnedFileField, darnedButton);
        tabs.addTab("Non-Denovo", nonDenovoPanel);


        JPanel denovoPanel = new JPanel();
        denovoPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        denovoPanel.setLayout(new GridBagLayout());

        c = new GridBagConstraints();
        c.gridy = 0;
        JLabel denovoRSciptLable = new JLabel(LocationPreferences.RSCRIPT_PATH);
        denovoRScriptPath = new JTextField();
        if (rScript != null) {
            denovoRScriptPath.setText(rScript);
        } else {
            denovoRScriptPath.setText("");
        }
        denovoRScriptPath.setEditable(false);
        JButton denovoRScriptButton = new JButton("Browse");
        denovoRScriptButton.setActionCommand(LocationPreferences.RSCRIPT_PATH);
        denovoRScriptButton.addActionListener(this);
        addItem(c, denovoPanel, denovoRSciptLable, denovoRScriptPath, denovoRScriptButton);

        c.gridy++;
        JLabel denovoRnaVcfLable = new JLabel(LocationPreferences.RNA_VCF_FILE);
        denovoRnaVcfFileField = new JTextField();
        denovoRnaVcfFileField.setText(preferences.getRnaVcfFile());
        if (rnaVcfFile != null) {
            denovoRnaVcfFileField.setText(rnaVcfFile);
        } else {
            denovoRnaVcfFileField.setText("");
        }
        denovoRnaVcfFileField.setEditable(false);
        JButton devonoRnaVcfButton = new JButton("Browse");
        devonoRnaVcfButton.setActionCommand(LocationPreferences.RNA_VCF_FILE);
        devonoRnaVcfButton.addActionListener(this);
        addItem(c, denovoPanel, denovoRnaVcfLable, denovoRnaVcfFileField, devonoRnaVcfButton);

        c.gridy++;
        JLabel denovoRepeatLable = new JLabel(LocationPreferences.REPEAT_FILE);
        denovoRepeatFileField = new JTextField();
        if (repeatFile != null) {
            denovoRepeatFileField.setText(repeatFile);
        } else {
            denovoRepeatFileField.setText("");
        }
        denovoRepeatFileField.setEditable(false);
        JButton denovoRepeatButton = new JButton("Browse");
        denovoRepeatButton.setActionCommand(LocationPreferences.REPEAT_FILE);
        denovoRepeatButton.addActionListener(this);
        addItem(c, denovoPanel, denovoRepeatLable, denovoRepeatFileField, denovoRepeatButton);

        c.gridy++;
        JLabel denovoRefSeqLabel = new JLabel(LocationPreferences.REF_SEQ_FILE);
        denovoRefSeqFileField = new JTextField();
        if (refSeqFile != null) {
            denovoRefSeqFileField.setText(refSeqFile);
        } else {
            denovoRefSeqFileField.setText("");
        }
        denovoRefSeqFileField.setEditable(false);
        JButton denovoRefSeqButton = new JButton("Browse");
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
        JButton denovoDbSNPButton = new JButton("Browse");
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
        JButton denovoDarnedButton = new JButton("Browse");
        denovoDarnedButton.setActionCommand(LocationPreferences.DARNED_FILE);
        denovoDarnedButton.addActionListener(this);
        addItem(c, denovoPanel, denovoDarnedLabel, denovoDarnedFileField, denovoDarnedButton);

        tabs.addTab("Denovo", denovoPanel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Import");
        okButton.setActionCommand("import");
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
     * @param dataType The action.
     * @param f        the TextFild from which to take the starting directory
     */
    private void getFile(String dataType, JTextField f) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(f.getText()));
        chooser.setAccessory(new DataIntroductionPanel(dataType));
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            f.setText(chooser.getSelectedFile().getAbsolutePath().replaceAll("\\\\", "/"));
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        String action = ae.getActionCommand();
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

        } else if (action.equals(LocationPreferences.RSCRIPT_PATH)) {
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
                case NON_DENOVO_INDEX:
                    ThreadNonDenovoInput nonDenovoInput = new ThreadNonDenovoInput(application.dataCollection());
                    nonDenovoInput.addProgressListener(REDApplication.getInstance());
                    ProgressDialog progressDialog = new ProgressDialog("Importing Data Into Database");
                    nonDenovoInput.addProgressListener(progressDialog);
                    new Thread(nonDenovoInput).start();
                    break;
                case DENOVO_INDEX:
                    ThreadDenovoInput denovoInput = new ThreadDenovoInput(application.dataCollection());
                    denovoInput.addProgressListener(REDApplication.getInstance());
                    ProgressDialog progressDialog2 = new ProgressDialog("Importing Data Into Database");
                    denovoInput.addProgressListener(progressDialog2);
                    new Thread(denovoInput).start();
                    break;
            }
            setVisible(false);
            dispose();
        }
    }

}
