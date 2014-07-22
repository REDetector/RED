package com.xl.dialog;

/**
 * Created by Administrator on 2014/6/25.
 */

import com.dw.denovo.*;
import com.dw.dnarna.DnaRnaFilter;
import com.dw.dnarna.DnaRnaVcf;
import com.dw.dnarna.LlrFilter;
import com.dw.publicaffairs.Clear;
import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;
import com.xl.main.REDApplication;
import com.xl.panel.DataIntroductionPanel;
import com.xl.preferences.LocationPreferences;
import com.xl.preferences.REDPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * A Dialog to allow the viewing and editing of all SeqMonk preferences.
 */
public class DataInportDialog extends JDialog implements ActionListener {

    private JTextField rnaVcfFile;

    private JTextField dnaVcfFile;

    private JTextField repeatFile;

    private JTextField refSeqFile;

    private JTextField dbsnpFile;

    private JTextField darnedFile;

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
        setSize(600, 300);
        setLocationRelativeTo(REDApplication.getInstance());
        setModal(true);

        JPanel filePanel = new JPanel();
        filePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        filePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        JLabel rnaVcfLable = new JLabel(LocationPreferences.RNA_VCF_FILE);
        rnaVcfFile = new JTextField();
        rnaVcfFile.setText("");
        rnaVcfFile.setEditable(false);
        JButton rnaVcfButton = new JButton("Browse");
        rnaVcfButton.setActionCommand(LocationPreferences.RNA_VCF_FILE);
        rnaVcfButton.addActionListener(this);
        addItem(c, filePanel, rnaVcfLable, rnaVcfFile, rnaVcfButton);

        c.gridy++;
        JLabel dnaVcfLable = new JLabel(LocationPreferences.DNA_VCF_FILE);
        dnaVcfFile = new JTextField();
        dnaVcfFile.setText("");
        dnaVcfFile.setEditable(false);
        JButton dnaVcfButton = new JButton("Browse");
        dnaVcfButton.setActionCommand(LocationPreferences.DNA_VCF_FILE);
        dnaVcfButton.addActionListener(this);
        addItem(c, filePanel, dnaVcfLable, dnaVcfFile, dnaVcfButton);

        c.gridy++;
        JLabel repeatLable = new JLabel(LocationPreferences.REPEAT_FILE);
        repeatFile = new JTextField();
        repeatFile.setText("");
        repeatFile.setEditable(false);
        JButton repeatButton = new JButton("Browse");
        repeatButton.setActionCommand(LocationPreferences.REPEAT_FILE);
        repeatButton.addActionListener(this);
        addItem(c, filePanel, repeatLable, repeatFile, repeatButton);

        c.gridy++;
        JLabel refSeqLabel = new JLabel(LocationPreferences.REF_SEQ_FILE);
        refSeqFile = new JTextField();
        refSeqFile.setText("");
        refSeqFile.setEditable(false);
        JButton refSeqButton = new JButton("Browse");
        refSeqButton.setActionCommand(LocationPreferences.REF_SEQ_FILE);
        refSeqButton.addActionListener(this);
        addItem(c, filePanel, refSeqLabel, refSeqFile, refSeqButton);

        c.gridy++;
        JLabel dbsnpLabel = new JLabel(LocationPreferences.DBSNP_FILE);
        dbsnpFile = new JTextField();
        dbsnpFile.setText("");
        dbsnpFile.setEditable(false);
        JButton dnaButton = new JButton("Browse");
        dnaButton.setActionCommand(LocationPreferences.DBSNP_FILE);
        dnaButton.addActionListener(this);
        addItem(c, filePanel, dbsnpLabel, dbsnpFile, dnaButton);

        c.gridy++;
        JLabel darnedLabel = new JLabel(LocationPreferences.DARNED_FILE);
        darnedFile = new JTextField();
        darnedFile.setText("");
        darnedFile.setEditable(false);
        JButton darnedButton = new JButton("Browse");
        darnedButton.setActionCommand(LocationPreferences.DARNED_FILE);
        darnedButton.addActionListener(this);
        addItem(c, filePanel, darnedLabel, darnedFile, darnedButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(filePanel, BorderLayout.CENTER);

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
            System.out.println(f.getText());
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        String action = ae.getActionCommand();

        if (action.equals(LocationPreferences.RNA_VCF_FILE)) {
            getFile(action, rnaVcfFile);
        } else if (action.equals(LocationPreferences.DNA_VCF_FILE)) {
            getFile(action, dnaVcfFile);
        } else if (action.equals(LocationPreferences.REPEAT_FILE)) {
            getFile(action, repeatFile);
        } else if (action.equals(LocationPreferences.REF_SEQ_FILE)) {
            getFile(action, refSeqFile);
        } else if (action.equals(LocationPreferences.DBSNP_FILE)) {
            getFile(action, dbsnpFile);
        } else if (action.equals(LocationPreferences.DARNED_FILE)) {
            getFile(action, darnedFile);
        } else if (action.equals("cancel")) {
            setVisible(false);
            dispose();
        } else if (action.equals("import")) {
            DatabaseManager manager = DatabaseManager.getInstance();
            manager.createStatement();
            manager.setAutoCommit(true);

            boolean isDenovo = false;
            if (!isDenovo) {
                manager.createDatabase("dnarna");
                manager.useDatabase("dnarna");

                DnaRnaVcf df = new DnaRnaVcf(manager, rnaVcfFile.getText(), dnaVcfFile.getText(), "rnaVcf",
                        "dnaVcf");
                Utilities.getInstance().createCalTable(dnaVcfFile.getText());
                df.establishDnaTable();
                df.dnaVcf();

                Clear cl = new Clear();
                cl.clear(Utilities.getInstance().getS2(), Utilities.getInstance().getS3());

                Utilities.getInstance().createCalTable(rnaVcfFile.getText());
                df.establishRnaTable();
                df.rnaVcf();

                BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
                        "basictemp");
                bf.createSpecificTable();
                bf.specificf();
                // The first parameter means quality and the second means depth
                bf.basicf(20, 6);
                bf.distinctTable();

                RepeatFilter rf = new RepeatFilter(manager, repeatFile.getText(), "repeattemp", "referencerepeat",
                        "basictemp");
                rf.loadrepeat();
                rf.establishrepeat();
                rf.rfilter();
                rf.distinctTable();

                ComphrehensiveFilter cf = new
                        ComphrehensiveFilter(manager, refSeqFile.getText(), "comphrehensivetemp", "refcomphrehensive",
                        "repeattemp");
                cf.establishCom();
                cf.loadcom();
                cf.comphrehensiveF(2);
                cf.distinctTable();

                DbsnpFilter sf = new
                        DbsnpFilter(manager, dbsnpFile.getText(), "snptemp", "refsnp", "comphrehensivetemp");
                sf.establishsnp();
                sf.dbSnpinput();
                sf.snpFilter();
                sf.distinctTable();

                DnaRnaFilter dr = new DnaRnaFilter(manager, "dnaVcf", "DnaRnatemp",
                        "snptemp");
                dr.createDnaRnaTable();
                dr.dnarnaFilter();
                dr.distinctTable();

                LlrFilter lf = new LlrFilter(manager, "dnaVcf", "llrtemp",
                        "DnaRnatemp");
                lf.createLlrTable();
                lf.llrtemp();
                lf.distinctTable();

                PValueFilter pv = new
                        PValueFilter(manager, darnedFile.getText(), "pvtemp", "refHg19", "llrtemp");
                pv.loadHg19();
//                pv.fdr(args[7]);

            } else {
                manager.createDatabase("denovo");
                manager.useDatabase("denovo");
                Utilities.getInstance().createCalTable(rnaVcfFile.getText());

                DenovoVcf df = new DenovoVcf(manager, rnaVcfFile.getText(), "rnaVcf");
                df.rnaVcf();

                BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
                        "basictemp");
                bf.createSpecificTable();
                bf.specificf();
                // The first parameter means quality and the second means depth
                bf.basicf(20, 6);
                bf.distinctTable();

                RepeatFilter rf = new
                        RepeatFilter(manager, repeatFile.getText(), "repeattemp", "referencerepeat", "basictemp");
                rf.loadrepeat();
                rf.establishrepeat();
                rf.rfilter();
                rf.distinctTable();

                ComphrehensiveFilter cf = new
                        ComphrehensiveFilter(manager, refSeqFile.getText(), "comphrehensivetemp", "refcomphrehensive",
                        "repeattemp");
                cf.establishCom();
                cf.loadcom();
                cf.comphrehensiveF(2);
                cf.distinctTable();

                DbsnpFilter sf = new
                        DbsnpFilter(manager, dbsnpFile.getText(), "snptemp", "refsnp", "comphrehensivetemp");
                sf.establishsnp();
                sf.dbSnpinput();
                sf.snpFilter();
                sf.distinctTable();

                PValueFilter pv = new
                        PValueFilter(manager, darnedFile.getText(), "pvtemp", "refHg19", "snptemp");
                pv.loadHg19();
//                pv.fdr(args[6]);

            }
            manager.closeDatabase();
            REDPreferences.getInstance().setDataLoadedToDatabase(true);
            setVisible(false);
            dispose();
        }
    }

}
