package com.xl.dialog;

import com.xl.main.REDApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Jim Robinson
 */
public class UserPasswordDialog extends JDialog implements ActionListener {

    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JPasswordField passwordField;

    public UserPasswordDialog(REDApplication application) {
        super(application, "MySQL Server Login...");
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
            System.out.println(host + "\t" + port + "\t" + user + "\t" + String.valueOf(pwd));
        } else if (action.equals("cancel")) {
            setVisible(false);
            dispose();
        }
    }
}
