package com.xl.display.report;

/**
 * Created by Administrator on 2014/9/18.
 */

import com.xl.dialog.CrashReporter;
import com.xl.dialog.ProgressDialog;
import com.xl.dialog.ReportTableDialog;
import com.xl.interfaces.OptionsListener;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.utils.FontManager;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The Class ReportOptions displays a dialog containing the options panel
 * for a report.
 */
public class ReportOptions extends JDialog implements ActionListener, ProgressListener, OptionsListener {

    /**
     * The ok button.
     */
    private JButton okButton;

    /**
     * The application.
     */
    private REDApplication application;

    /**
     * The report.
     */
    private Report report;

    /**
     * Instantiates a new report options.
     *
     * @param parent the parent
     * @param report the report
     */
    public ReportOptions(REDApplication parent, Report report) {
        super(parent, report.name() + " Options");

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.report = report;
        this.application = parent;

        report.addOptionsListener(this);

        setSize(500, 250);
        setLocationRelativeTo(parent);

        getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();

        JLabel probeListLabel;

        if (report.dataCollection().probeSet() != null) {
            probeListLabel = new JLabel("Reporting on probes in '" + report.dataCollection().probeSet().getActiveList().name() + "' (" + report.dataCollection().probeSet().getActiveList().getAllProbes().length + " probes)", JLabel.CENTER);
        } else {
            probeListLabel = new JLabel("Reporting on all data", JLabel.CENTER);
        }
        probeListLabel.setFont(FontManager.defaultFont);
        topPanel.add(probeListLabel);

        getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();

        okButton = new JButton("OK");
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        JPanel optionsPanel = report.getOptionsPanel();

        if (optionsPanel != null) {
            getContentPane().add(report.getOptionsPanel());
            setVisible(true);
        } else {
            actionPerformed(new ActionEvent(this, 0, "ok"));
        }

        // Some reports need to have set up their options panel
        // before the isReady option works, so we make this call
        // only once the whole dialog is laid out.
        okButton.setEnabled(report.isReady());

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("cancel")) {
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals("ok")) {
            okButton.setEnabled(false);
            report.addProgressListener(this);
            report.addProgressListener(new ProgressDialog(this, "Creating report...", report));
            report.generateReport();
        }
    }

    public void progressCancelled() {
        // Reenable the OK button as long as they haven't been messing around.
        optionsChanged();
    }

    public void progressComplete(String command, Object result) {
        new ReportTableDialog(application, report, (TableModel) result);
        setVisible(false);
        dispose();
    }

    public void progressExceptionReceived(Exception e) {
        new CrashReporter(e);
    }

    public void progressUpdated(String message, int current, int max) {
    }

    public void progressWarningReceived(Exception e) {
    }

    public void optionsChanged() {
        if (report.isReady()) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }

}
