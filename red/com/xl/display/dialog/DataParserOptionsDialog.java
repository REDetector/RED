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
package com.xl.display.dialog;

import com.xl.exception.REDException;
import com.xl.main.REDApplication;
import com.xl.parsers.dataparsers.DataParser;
import com.xl.utils.ui.OptionDialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The Class DataParserOptionsDialog provides a option dialog before importing new data if it is necessary.
 */
public class DataParserOptionsDialog extends JDialog implements ActionListener {
    /**
     * The data parser.
     */
    private DataParser parser;

    // We need to set the default not to parse in case they use the X to close the window.
    private boolean goAheadAndParse = false;

    public DataParserOptionsDialog(DataParser parser) {
        super(REDApplication.getInstance());
        setModal(true);
        setTitle("Import Options");

        this.parser = parser;

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(new JLabel("Options for " + parser.parserName(), JLabel.CENTER), BorderLayout.NORTH);

        JPanel optionsPanel = parser.getOptionsPanel();
        getContentPane().add(optionsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton closeButton = new JButton("Close");
        closeButton.setActionCommand("close");
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        JButton importButton = new JButton("Import");
        importButton.setActionCommand("import");
        importButton.addActionListener(this);
        buttonPanel.add(importButton);
        getRootPane().setDefaultButton(importButton);

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
                OptionDialogUtils.showErrorDialog(this, "Some options have not been set");
            }
        } else {
            new CrashReporter(new REDException("Don't know how to handle action '" + ae.getActionCommand() + "'"));
        }
    }

}
