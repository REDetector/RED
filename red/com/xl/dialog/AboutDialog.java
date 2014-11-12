package com.xl.dialog;

import com.xl.main.REDApplication;
import com.xl.panel.REDTitlePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Shows the generic about dialog giving details of the current version
 * and copyright assignments.  This is just a thin shell around the
 * SeqMonkTitlePanel which actually holds the relevant information and
 * which is also used on the welcome screen.
 */
public class AboutDialog extends JDialog {

    /**
     * Instantiates a new about dialog.
     */
    public AboutDialog() {
        super(REDApplication.getInstance());
        setTitle("About RED...");
        Container cont = getContentPane();
        cont.setLayout(new BorderLayout());

        add(new REDTitlePanel(), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton closeButton = new JButton("Close");
        getRootPane().setDefaultButton(closeButton);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(closeButton);

        cont.add(buttonPanel, BorderLayout.SOUTH);

        setSize(1000, 300);
        setLocationRelativeTo(REDApplication.getInstance());
        setResizable(false);
        setVisible(true);
    }

}
