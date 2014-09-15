package com.xl.dialog;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.probes.ProbeList;
import com.xl.exception.REDException;
import com.xl.filter.ProbeFilter;
import com.xl.interfaces.OptionsListener;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Administrator on 2014/7/25.
 */
public class FilterOptionsDialog extends JDialog implements OptionsListener, ProgressListener, ActionListener {

    private ProbeFilter filter;
    private JButton filterButton;

    public FilterOptionsDialog(DataCollection collection, ProbeFilter filter) {
        super(REDApplication.getInstance(), filter.name());

        this.filter = filter;

        filter.addProgressListener(this);
        filter.addOptionsListener(this);

        getContentPane().setLayout(new BorderLayout());

        JLabel probeListLabel = new JLabel("Testing probes in '" + collection.probeSet().getActiveList().name() + "' (" + collection.probeSet().getActiveList().getAllProbes().length + " probes)", JLabel.CENTER);
        probeListLabel.setFont(new Font("Default", Font.BOLD, 12));
        probeListLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        getContentPane().add(probeListLabel, BorderLayout.NORTH);

        if (filter.hasOptionsPanel()) {
            JPanel optionsPanel = filter.getOptionsPanel();
            getContentPane().add(optionsPanel, BorderLayout.CENTER);
            setSize(optionsPanel.getPreferredSize());
        } else {
            getContentPane().add(new JLabel("No Options", JLabel.CENTER), BorderLayout.CENTER);
            setSize(400, 100);
        }

        JPanel buttonPanel = new JPanel();

        JButton closeButton = new JButton("Close");
        closeButton.setActionCommand("close");
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        filterButton = new JButton("Run Filter");
        filterButton.setActionCommand("filter");
        getRootPane().setDefaultButton(filterButton);
        filterButton.addActionListener(this);
        if (!filter.isReady()) {
            filterButton.setEnabled(false);
        }
        buttonPanel.add(filterButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(REDApplication.getInstance());
        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals("filter")) {
            if (filter.isReady()) {
                filterButton.setEnabled(false);
                filter.addProgressListener(new ProgressDialog(this, "Running Filter...", filter));
                try {
                    filter.runFilter();
                } catch (REDException e) {
                    progressExceptionReceived(e);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Filter options have not all been set", "Can't run filter", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    public void optionsChanged() {
        if (filter.isReady()) {
            filterButton.setEnabled(true);
        } else {
            filterButton.setEnabled(false);
        }
    }


    public void progressCancelled() {
        filterButton.setEnabled(true);
    }


    public void progressComplete(String command, Object result) {

        ProbeList newList = (ProbeList) result;

        filterButton.setEnabled(true);

        // See if any probes actually passed
        if (newList.getAllProbes().length == 0) {
            // We need to remove this empty list.
            newList.delete();
            JOptionPane.showMessageDialog(this, "No probes matched the criteria set", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Ask for a name for the list
        String groupName;
        while (true) {
            groupName = (String) JOptionPane.showInputDialog(this, "Enter list name", "Found " + newList.getAllProbes().length + " probes", JOptionPane.QUESTION_MESSAGE, null, null, newList.name());
            if (groupName == null) {
                // Since the list will automatically have been added to
                // the ProbeList tree we actively need to delete it if
                // they choose to cancel at this point.
                newList.delete();
                return;  // They cancelled
            }

            if (groupName.length() == 0)
                continue; // Try again

            break;
        }
        newList.setName(groupName);

    }


    public void progressExceptionReceived(Exception e) {
        new CrashReporter(e);
        filterButton.setEnabled(true);
    }


    public void progressUpdated(String message, int current, int max) {
    }


    public void progressWarningReceived(Exception e) {
    }
}
