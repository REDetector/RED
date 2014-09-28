package com.xl.dialog;

import com.dw.publicaffairs.DatabaseManager;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Jim Robinson
 */
public class UserPasswordDialog extends JDialog implements ActionListener {
    protected final ArrayList<ProgressListener> listeners;
    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JPasswordField passwordField;
    private REDApplication application;
    private REDPreferences preferences = REDPreferences.getInstance();

    public UserPasswordDialog(REDApplication application) {
        super(application, "MySQL Server Login...");
        listeners = new ArrayList<ProgressListener>();
        listeners.add(application);
        this.application = application;
        setSize(500, 400);
        setModal(true);
        String host = preferences.getDatabaseHost();
        String port = preferences.getDatabasePort();
        String user = preferences.getDatabaseUser();
        String password = preferences.getDatabasePassword();
        setLocationRelativeTo(application);
        getContentPane().setLayout(new GridLayout(4, 1));

        JPanel introductionPanel = new JPanel();
        introductionPanel.setBorder(BorderFactory.createTitledBorder("Introduction"));

        introductionPanel.add(new JLabel("<html>MySQL Server Login Panel.<br>We use jdbc to connect between RED and " +
                "MySQL.<br>Before using this function, you must configure your MySQL database in advance."));
        add(introductionPanel);

        JPanel hostPanel = new JPanel();
        hostPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
        hostPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        hostPanel.add(new JLabel("    Host:    "), c);
        c.gridx = 1;
        c.weightx = 0.8;
        if (host != null) {
            hostField = new JTextField(host);
        } else {
            hostField = new JTextField("127.0.0.1");
        }
        hostPanel.add(hostField, c);
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        hostPanel.add(new JLabel("    Port:    "), c);
        c.gridx = 1;
        c.weightx = 0.8;
        if (port != null) {
            portField = new JTextField(port);
        } else {
            portField = new JTextField("3306");
        }
        hostPanel.add(portField, c);
        add(hostPanel);

        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Infomation"));
        infoPanel.setLayout(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(new JLabel("    User:    "), c);
        c.gridx = 1;
        c.weightx = 0.8;
        if (user != null) {
            userField = new JTextField(user);
        } else {
            userField = new JTextField("root");
        }
        infoPanel.add(userField, c);
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(new JLabel("    Password:"), c);
        c.gridx = 1;
        c.weightx = 0.8;
        if (password != null) {
            passwordField = new JPasswordField(password);
        } else {
            passwordField = new JPasswordField();
        }
        infoPanel.add(passwordField, c);
        add(infoPanel);

        JPanel confirmPanel = new JPanel();

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        confirmPanel.add(cancelButton);

        JButton okButton = new JButton("Connect");
        okButton.setActionCommand("connect");
        okButton.addActionListener(this);
        confirmPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);

        add(confirmPanel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("connect")) {
            String host = hostField.getText();
            String port = portField.getText();
            String user = userField.getText();
            String pwd = String.valueOf(passwordField.getPassword());
            preferences.setDatabaseHost(host);
            preferences.setDatabasePort(port);
            preferences.setDatabaseUser(user);
            preferences.setDatabasePassword(pwd);
            try {
                if (DatabaseManager.getInstance().connectDatabase(host, port, user, pwd)) {
                    for (int i = 0; i < 1000; i++) {
                        progressUpdated("Connecting to database...", i, 1000);
                    }

                    if (application.dataCollection() == null) {
                        JOptionPane.showMessageDialog(application, "<html>Connect Successfully. " +
                                        "<br>You may start a new project before you input your data into database. " +
                                        "<br>Click 'ok' to the next step.",
                                "Connect Successfully",
                                JOptionPane.INFORMATION_MESSAGE);
                        application.startNewProject();
                    }
                    if (!REDPreferences.getInstance().isDataLoadedToDatabase()) {
                        JOptionPane.showMessageDialog(application, "<html>Connect Successfully. " +
                                        "<br>You may import your data into database before detecting editing " +
                                        "sites. <br>Click 'ok' to the next step.",
                                "Connect Successfully",
                                JOptionPane.INFORMATION_MESSAGE);
                        new DataInportDialog(application);
                        setVisible(false);
                    } else {
                        JOptionPane.showMessageDialog(application, "<html>Connect Successfully. " +
                                        "<br>You can detect editing sites using the filter in 'Filter' menu",
                                "Connect Successfully",
                                JOptionPane.INFORMATION_MESSAGE);
                        if (REDPreferences.getInstance().isDenovo()) {
                            DatabaseManager.getInstance().useDatabase(DatabaseManager.DENOVO_DATABASE_NAME);
                        } else {
                            DatabaseManager.getInstance().useDatabase(DatabaseManager.NON_DENOVO_DATABASE_NAME);
                        }
                        processingComplete("database_loaded");
                    }
                    processingComplete("database_connected");
                    dispose();
                }
            } catch (ClassNotFoundException e1) {
                new CrashReporter(e1);
                progressCancelled();
                e1.printStackTrace();
            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(this, "Sorry, fail to connect to database. You may input one of wrong " +
                                "database site, user name or password.", "Connected Failed",
                        JOptionPane.ERROR_MESSAGE);
                progressCancelled();
                e1.printStackTrace();
            }
        } else if (action.equals("cancel")) {
            setVisible(false);
            dispose();
        }
    }

    /**
     * Alerts all listeners to a progress update
     *
     * @param message The message to send
     * @param current The current level of progress
     * @param max     The level of progress at completion
     */
    protected void progressUpdated(String message, int current, int max) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressUpdated(message, current, max);
        }
    }

    /**
     * Alerts all listeners that an exception was received. The
     * parser is not expected to continue after issuing this call.
     *
     * @param e The exception
     */
    protected void progressExceptionReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressExceptionReceived(e);
        }
    }

    /**
     * Alerts all listeners that a warning was received.  The parser
     * is expected to continue after issuing this call.
     *
     * @param e The warning exception received
     */
    protected void progressWarningReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressWarningReceived(e);
        }
    }

    /**
     * Alerts all listeners that the user cancelled this import.
     */
    protected void progressCancelled() {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressCancelled();
        }
    }

    /**
     * Tells all listeners that the parser has finished parsing the data
     * The list of dataSets should be the same length as the original file list.
     */
    protected void processingComplete(String command) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressComplete(command, null);
        }
    }
}
