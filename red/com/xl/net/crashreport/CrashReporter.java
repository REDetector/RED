package com.xl.net.crashreport;

import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;
import com.xl.utils.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * The Class CrashReporter is the dialog which appears when an unexpected
 * exception is encountered. It can generate a stack trace and submit it back to
 * the authors to fix.
 */
public class CrashReporter extends JDialog implements ActionListener {

    /**
     * The Constant reportURLString.
     */
    public static final String reportURLString = "sam.lxing@gmail.com";

    /**
     * The e.
     */
    private final Throwable e;

    /**
     * Instantiates a new crash reporter.
     *
     * @param e the e
     */
    public CrashReporter(Throwable e) {
        super(REDApplication.getInstance(), "Oops - Crash Reporter");

        this.e = e;
        e.printStackTrace();

        if (e instanceof OutOfMemoryError) {
            // Don't issue a normal crash report but tell them that they
            // ran out of memory

            JOptionPane
                    .showMessageDialog(
                            REDApplication.getInstance(),
                            "<html>You ran out of memory!<br><br>Please look at Help &gt; Contents &gt; Configuration to see how to fix this",
                            "Out of memory", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 200);
        setLocationRelativeTo(REDApplication.getInstance());

        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        gbc.gridheight = 4;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        ImageIcon sadMonk = new ImageIcon(
                ClassLoader
                        .getSystemResource("resources/sad_monk100.png"));

        getContentPane().add(new JLabel(sadMonk), gbc);
        gbc.weightx = 0.2;
        gbc.weighty = 0.5;

        gbc.weightx = 0.8;
        gbc.gridheight = 1;
        gbc.gridx = 1;

        JLabel genericMessage = new JLabel("RED encountered a problem.",
                JLabel.CENTER);

        getContentPane().add(genericMessage, gbc);
        gbc.gridy++;

        JLabel sorryMessage = new JLabel("Sorry about that.  The error was: ",
                JLabel.CENTER);

        getContentPane().add(sorryMessage, gbc);
        gbc.gridy++;

        JLabel errorClass = new JLabel(e.getClass().getName(), JLabel.CENTER);
        errorClass.setFont(FontManager.reportFont);
        errorClass.setForeground(Color.RED);
        getContentPane().add(errorClass, gbc);

        gbc.gridy++;
        JLabel errorMessage = new JLabel(e.getLocalizedMessage(), JLabel.CENTER);
        errorMessage.setFont(FontManager.reportFont);
        errorMessage.setForeground(Color.RED);

        getContentPane().add(errorMessage, gbc);

        JPanel buttonPanel = new JPanel();

        JButton closeButton = new JButton("Ignore");
        closeButton.setActionCommand("close");
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        JButton sendButton = new JButton("Report Error");
        sendButton.setActionCommand("send_report");
        sendButton.addActionListener(this);
        buttonPanel.add(sendButton);

        gbc.gridy++;
        gbc.gridwidth = 2;
        getContentPane().add(buttonPanel, gbc);

        setVisible(true);

    }

    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals("close")) {
            setVisible(false);
            super.dispose();
        } else if (ae.getActionCommand().equals("send_report")) {
            new ReportSender(this);
        }
    }

    /**
     * The Class ReportSender.
     */
    private class ReportSender extends JDialog implements ActionListener, Runnable {

        /**
         * The report text.
         */
        private String reportText;

        /**
         * The email.
         */
        private JTextField email;

        /**
         * Do we remember the email
         */
        private JCheckBox rememberEmail;

        /**
         * The send button.
         */
        private JButton sendButton;

        /**
         * The cancel button.
         */
        private JButton cancelButton;

        /**
         * A flag to say if we found any SeqMonk specific classes *
         */
        private boolean foundREDClass = false;

        /**
         * The cr.
         */
        private CrashReporter cr;

        /**
         * Instantiates a new report sender.
         *
         * @param cr the cr
         */
        public ReportSender(CrashReporter cr) {
            super(cr, "Send Error Report");
            this.cr = cr;
            setSize(500, 500);
            setLocationRelativeTo(cr);

            reportText = makeReportText();

            getContentPane().setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.weightx = 0.5;
            gbc.weighty = 0.1;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel introLabel = new JLabel("The contents of the report are shown below:", JLabel.CENTER);
            getContentPane().add(introLabel, gbc);

            JTextArea reportTextArea = new JTextArea(reportText);
            reportTextArea.setEditable(false);

            gbc.gridy++;
            gbc.weighty = 0.9;
            gbc.fill = GridBagConstraints.BOTH;
            getContentPane().add(new JScrollPane(reportTextArea), gbc);

            gbc.gridy++;
            gbc.weighty = 0.1;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel notifyLabel = new JLabel("Enter your email below to allow us to help you with this problem", JLabel.CENTER);

            getContentPane().add(notifyLabel, gbc);

            JPanel emailPanel = new JPanel();
            emailPanel.add(new JLabel("Email:"));
            email = new JTextField(30);
            email.setText(REDPreferences.getInstance().getCrashEmail());
            emailPanel.add(email);

            gbc.gridy++;

            getContentPane().add(emailPanel, gbc);

            JPanel rememberPanel = new JPanel();
            rememberPanel.add(new JLabel("Remember this address for future reports "));
            rememberEmail = new JCheckBox();

            // We don't initially remember their email - but we will once they have supplied one.
            if (REDPreferences.getInstance().getCrashEmail().length() > 0) {
                rememberEmail.setSelected(true);
            }
            rememberPanel.add(rememberEmail);

            gbc.gridy++;

            getContentPane().add(rememberPanel, gbc);

            JPanel buttonPanel = new JPanel();

            cancelButton = new JButton("Cancel");
            cancelButton.setActionCommand("cancel");
            cancelButton.addActionListener(this);
            buttonPanel.add(cancelButton);

            sendButton = new JButton("Send");
            sendButton.setActionCommand("send_report");
            sendButton.addActionListener(this);
            buttonPanel.add(sendButton);

            gbc.gridy++;
            getContentPane().add(buttonPanel, gbc);

            setVisible(true);
        }

        /**
         * Make report text.
         *
         * @return the string
         */
        private String makeReportText() {
            StringBuilder sb = new StringBuilder();

            sb.append("RED Version:");
            sb.append(REDApplication.VERSION);
            sb.append("\n\n");

            sb.append("Operating System:");
            sb.append(System.getProperty("os.name"));
            sb.append(" - ");
            sb.append(System.getProperty("os.version"));
            sb.append("\n\n");

            sb.append("Java Version:");
            sb.append(System.getProperty("java.version"));
            sb.append(" - ");
            sb.append(System.getProperty("java.vm.version"));
            sb.append("\n\n");

            sb.append(e.getClass().getName());
            if (e.getClass().getName().contains("RED")) {
                foundREDClass = true;
            }
            sb.append("\n");
            sb.append(e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("RED")) {
                foundREDClass = true;
            }
            sb.append("\n\n");

            StackTraceElement[] elements = e.getStackTrace();
            for (int i = 0; i < elements.length; i++) {
                sb.append(elements[i].toString());
                sb.append("\n");
                if (elements[i].toString().contains("RED")) {
                    foundREDClass = true;
                }
            }
            return sb.toString();
        }

        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("cancel")) {
                setVisible(false);
                dispose();
            } else if (ae.getActionCommand().equals("send_report")) {

                if (!foundREDClass) {
                    int reply = JOptionPane.showConfirmDialog(
                            this,
                            "<html>This error doesn't appear to come from within RED but is a bug in the core" +
                                    " Java classes.<br>You can still submit this report, but the RED authors may " +
                                    "not be able to fix it!<br><br>Do you still want to send the report?</html>",
                            "Not our fault!",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (reply == JOptionPane.NO_OPTION)
                        return;
                }

                if (email.getText().length() == 0) {
                    int reply = JOptionPane
                            .showConfirmDialog(
                                    this,
                                    "<html>You have not provided an email address.<br>Your report is still useful to us, but we can't send you any feedback about this bug.<br><br>Do you want to send anyway?</html>",
                                    "Send anonymous report?",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE);
                    if (reply == JOptionPane.NO_OPTION)
                        return;
                }

                // Check if we need to store their email
                if (rememberEmail.isSelected()) {
                    REDPreferences.getInstance().setCrashEmail(email.getText());
                } else {
                    REDPreferences.getInstance().setCrashEmail(null);
                }

                try {
                    REDPreferences.getInstance().savePreferences();
                } catch (IOException e) {
                    // We're going to ignore this in the UI since we're already
                    // inside a crash dialog.
                    e.printStackTrace();
                }

                Thread t = new Thread(this);
                t.start();
            }
        }

        public void run() {

            sendButton.setEnabled(false);
            sendButton.setText("Sending...");
            cancelButton.setEnabled(false);

            // Send the actual report.

            // We don't need to worry about proxy settings as these will have been put into the System properties by the REDPreferences class and should be
            // picked up automatically.

            try {
                URL url = new URL(reportURLString);
                String data = URLEncoder.encode("email", "ISO-8859-1") + "=" + URLEncoder.encode(email.getText(), "ISO-8859-1")
                        + "&" + URLEncoder.encode("stacktrace", "ISO-8859-1") + "=" + URLEncoder.encode(reportText, "ISO-8859-1");

                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(data);
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuffer htmlResponse = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    htmlResponse.append(line);
                }
                writer.close();
                reader.close();

                System.out.println(htmlResponse);

                if (!htmlResponse.toString().startsWith("Report Sent")) {

                    JOptionPane.showMessageDialog(cr, "We found some information which might help solve the problem you hit", "Help found",
                            JOptionPane.INFORMATION_MESSAGE);

                    // We've been returned a possible solution
                    new HTMLDisplayDialog(htmlResponse.toString());
                }
                setVisible(false);
                cr.setVisible(false);
                cr.dispose();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error Sending Report: " + e.getLocalizedMessage(), "Error sending crash Report", JOptionPane.ERROR_MESSAGE);

                sendButton.setText("Sending Failed...");
                cancelButton.setEnabled(true);

            }

        }
    }
}
