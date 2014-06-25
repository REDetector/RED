/**
 * Copyright 2009-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.xl.dialog;

import com.xl.exception.REDException;
import com.xl.main.REDApplication;
import com.xl.parsers.dataparsers.DataParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DataParserOptionsDialog extends JDialog implements ActionListener {

    private static DataParser parser;

    // We need to set the default not to parse in case they use the X to close the window.
    private boolean goAheadAndParse = false;

    public DataParserOptionsDialog(DataParser parser) {
        super(REDApplication.getInstance());
        setModal(true);
        setTitle("Import Options");

        DataParserOptionsDialog.parser = parser;

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(new JLabel("Options for " + parser.parserName(), JLabel.CENTER), BorderLayout.NORTH);

        JPanel optionsPanel = parser.getOptionsPanel();
        getContentPane().add(optionsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton importButton = new JButton("Import");
        importButton.setActionCommand("import");
        importButton.addActionListener(this);
        buttonPanel.add(importButton);

        JButton closeButton = new JButton("Close");
        closeButton.setActionCommand("close");
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        Dimension d = getPreferredSize();

        setSize(Math.max(d.width, 400), Math.max(d.height, 400));
        setLocationRelativeTo(REDApplication.getInstance());

    }

    public boolean view() {
        setVisible(true);
        // This will block here since the dialog is modal
        return goAheadAndParse;
    }

    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals("close")) {
            goAheadAndParse = false;
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals("import")) {
            if (parser.readyToParse()) {
                goAheadAndParse = true;
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Some options have not been set", "Can't Import yet..", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            new CrashReporter(new REDException("Don't know how to handle action '" + ae.getActionCommand() + "'"));
        }
    }

}
