package com.xl.dialog;

import com.dw.publicaffairs.DatabaseManager;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 * @author Jim Robinson
 */
public class UserPasswordDialog extends JDialog implements ActionListener {

    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JPasswordField passwordField;
    private REDApplication application;

    public UserPasswordDialog(REDApplication application) {
        super(application, "MySQL Server Login...");
        this.application = application;
        setSize(500, 400);
        setModal(true);

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
        hostField = new JTextField();
        hostPanel.add(hostField, c);
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        hostPanel.add(new JLabel("    Port:    "), c);
        c.gridx = 1;
        c.weightx = 0.8;
        portField = new JTextField();
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
        userField = new JTextField();
        infoPanel.add(userField, c);
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(new JLabel("    Password:"), c);
        c.gridx = 1;
        c.weightx = 0.8;
        passwordField = new JPasswordField();
        infoPanel.add(passwordField, c);
        add(infoPanel);

        JPanel confirmPanel = new JPanel();
        JButton okButton = new JButton("Connect");
        okButton.setActionCommand("connect");
        okButton.addActionListener(this);
        confirmPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        confirmPanel.add(cancelButton);
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
            ProgressDialog progressDialog = new ProgressDialog(this, "Connecting to database...");
            try {
                progressDialog.requestFocus();
                if (DatabaseManager.getInstance().connectDatabase(host, port, user, pwd)) {
                    if (!REDPreferences.getInstance().isDataLoadedToDatabase()) {
                        if (application.dataCollection().genome().getAllChromosomes() == null) {
                            JOptionPane.showMessageDialog(application, "<html>Connect Successfully. " +
                                            "<br>You may start a new project before you input your data into database. " +
                                            "<br>Click 'ok' to the next step.",
                                    "Connect Successfully",
                                    JOptionPane.INFORMATION_MESSAGE);
                            setVisible(false);
                            application.startNewProject();
                        } else {
                            JOptionPane.showMessageDialog(application, "<html>Connect Successfully. " +
                                            "<br>You may import your data into database before detecting editing " +
                                            "sites. <br>Click 'ok' to the next step.",
                                    "Connect Successfully",
                                    JOptionPane.INFORMATION_MESSAGE);
                            setVisible(false);
                            new DataInportDialog(application);
                        }
                    } else {
                        JOptionPane.showMessageDialog(application, "<html>Connect Successfully. " +
                                        "<br>You can detect editing sites using the filter in 'Filter' menu",
                                "Connect Successfully",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    progressDialog.progressComplete("database_connected", DatabaseManager.getInstance());
                    dispose();
                }
            } catch (ClassNotFoundException e1) {
                new CrashReporter(e1);
                progressDialog.progressCancelled();
                e1.printStackTrace();
            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(this, "Sorry, fail to connect to database. You may input one of wrong " +
                                "database site, user name or password.", "Connected Failed",
                        JOptionPane.ERROR_MESSAGE);
                progressDialog.progressCancelled();
                e1.printStackTrace();
            }
        } else if (action.equals("cancel")) {
            setVisible(false);
            dispose();
        }
    }

}
