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

package com.xl.database;

import com.xl.main.REDApplication;
import com.xl.preferences.DatabasePreferences;
import com.xl.utils.FontManager;
import com.xl.utils.namemanager.MenuUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 * @author Xing Li
 *         <p/>
 *         A database login dialog.
 */
public class UserPasswordDialog extends JDialog implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(UserPasswordDialog.class);
    /**
     * The host.
     */
    private JTextField hostField;
    /**
     * The port.
     */
    private JTextField portField;
    /**
     * The user.
     */
    private JTextField userField;
    /**
     * The password.
     */
    private JPasswordField passwordField;
    /**
     * The application.
     */
    private REDApplication application;
    /**
     * A reference of DatabasePreferences.
     */
    private DatabasePreferences preferences = DatabasePreferences.getInstance();

    public UserPasswordDialog(REDApplication application) {
        super(application, "MySQL Database Login Dialog...");
        this.application = application;
        setSize(500, 400);
        setModal(true);
        String host = preferences.getDatabaseHost();
        String port = preferences.getDatabasePort();
        String user = preferences.getDatabaseUser();
        String passwd = preferences.getDatabasePasswd();
        setLocationRelativeTo(application);
        getContentPane().setLayout(new GridLayout(4, 1));

        JPanel introductionPanel = new JPanel(new BorderLayout());
        introductionPanel.setBorder(BorderFactory.createTitledBorder("Introduction"));

        JTextArea textArea = new JTextArea();
        textArea.setText("We use JDBC to connect RED and MySQL database. Before connection, MySQL database should be configured correctly in advance.");
        textArea.setFont(FontManager.DIALOG_FONT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEnabled(false);
        textArea.setDragEnabled(false);
        textArea.setBackground(getBackground());
        introductionPanel.add(textArea);
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
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
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
        if (user != null) {
            passwordField = new JPasswordField(passwd);
        } else {
            passwordField = new JPasswordField();
            passwordField.requestFocus();
        }
        infoPanel.add(passwordField, c);
        add(infoPanel);

        JPanel confirmPanel = new JPanel();

        JButton cancelButton = new JButton(MenuUtils.CANCEL_BUTTON);
        cancelButton.setActionCommand(MenuUtils.CANCEL_BUTTON);
        cancelButton.addActionListener(this);
        confirmPanel.add(cancelButton);

        JButton okButton = new JButton(MenuUtils.CONNECT_BUTTON);
        okButton.setActionCommand(MenuUtils.CONNECT_BUTTON);
        okButton.addActionListener(this);
        confirmPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);

        add(confirmPanel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(MenuUtils.CONNECT_BUTTON)) {
            String host = hostField.getText();
            String port = portField.getText();
            String user = userField.getText();
            String pwd = String.valueOf(passwordField.getPassword());
            preferences.setDatabaseHost(host);
            preferences.setDatabasePort(port);
            preferences.setDatabaseUser(user);
            preferences.setDatabasePasswd(pwd);
            try {
                if (DatabaseManager.getInstance().connectDatabase(host, port, user, pwd)) {
                    dispose();
                    if (application.dataCollection() == null) {
                        JOptionPane.showMessageDialog(application, "<html>Connect Successfully. <br>You may start a new project before you input your data " +
                                "into database. <br>Click 'OK' to the next step.", "Connect Successfully", JOptionPane.INFORMATION_MESSAGE);
                        application.startNewProject();
                    }
                    new DatabaseSelector(application);
                    DatabaseManager.getInstance().databaseConnected();
                    logger.info("Database has been connected.");
                }
            } catch (ClassNotFoundException e1) {
                logger.warn("The driver has been integrated into the software and should not be found. If the ClassNotFoundException happens again, " +
                        "please try to download the latest version of RED to solve the problem.", e1);
            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(this, "Sorry, fail to connect to database. You may input one of wrong " +
                        "database host, port, user name or password.", "Connected Failed", JOptionPane.ERROR_MESSAGE);
                logger.error("Fail to connect to database.", e1);
            }
        } else if (action.equals(MenuUtils.CANCEL_BUTTON)) {
            setVisible(false);
            dispose();
        }
    }
}
