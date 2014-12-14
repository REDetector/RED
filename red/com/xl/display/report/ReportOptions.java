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

/**
 * Created by Xing Li on 2014/9/18.
 */

import com.xl.datatypes.sites.SiteList;
import com.xl.display.dialog.ProgressDialog;
import com.xl.interfaces.OptionsListener;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.net.crashreport.CrashReporter;
import com.xl.utils.FontManager;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The Class ReportOptions displays a dialog containing the options panel for a report.
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

        JLabel siteListLabel;

        SiteList l = report.collection.getActiveSiteList();
        if (l != null) {
            siteListLabel = new JLabel("Reporting on sites in '" + l.getFilterName() + "' (" + l.getAllSites().length + " sites)", JLabel.CENTER);
        } else {
            siteListLabel = new JLabel("Reporting on all data", JLabel.CENTER);
        }
        siteListLabel.setFont(FontManager.DEFAULT_FONT);
        topPanel.add(siteListLabel);

        getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        okButton = new JButton("OK");
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

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
