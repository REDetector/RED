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

import com.xl.main.RedApplication;
import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The Class WarningDisplayDialog presents a warning dialog for user when receiving any warnings.
 */
public class WarningDisplayDialog extends JDialog implements ActionListener {

    /**
     * Instantiates a new warning display dialog.
     *
     * @param exceptions the exceptions
     */
    public WarningDisplayDialog(Exception[] exceptions) {
        super(RedApplication.getInstance(), "Request Generated Warnings...");
        constructDialog(exceptions.length, exceptions);
        setLocationRelativeTo(RedApplication.getInstance());
        setVisible(true);
    }

    /**
     * Instantiates a new warning display dialog.
     *
     * @param exceptions the exceptions
     */
    public WarningDisplayDialog(int exceptionCount, Exception[] exceptions) {
        super(RedApplication.getInstance(), "Request Generated Warnings...");
        constructDialog(exceptionCount, exceptions);
        setLocationRelativeTo(RedApplication.getInstance());
        setVisible(true);
    }


    /**
     * Instantiates a new warning display dialog.
     *
     * @param parent     the parent
     * @param exceptions the exceptions
     */
    public WarningDisplayDialog(JFrame parent, Exception[] exceptions) {
        super(parent, "Request Generated Warnings...");
        constructDialog(exceptions.length, exceptions);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Instantiates a new warning display dialog.
     *
     * @param parent         the parent
     * @param exceptionCount the exception count
     * @param exceptions     the exceptions
     */
    public WarningDisplayDialog(JDialog parent, int exceptionCount, Exception[] exceptions) {
        super(parent, "Request Generated Warnings...");
        constructDialog(exceptionCount, exceptions);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Instantiates a new warning display dialog.
     *
     * @param parent     the parent
     * @param exceptions the exceptions
     */
    public WarningDisplayDialog(JDialog parent, Exception[] exceptions) {
        super(parent, "Request Generated Warnings...");
        constructDialog(exceptions.length, exceptions);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Instantiates a new warning display dialog.
     *
     * @param parent         the parent
     * @param exceptionCount the exception count
     * @param exceptions     the exceptions
     */
    public WarningDisplayDialog(JFrame parent, int exceptionCount, Exception[] exceptions) {
        super(parent, "Request Generated Warnings...");
        constructDialog(exceptionCount, exceptions);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void constructDialog(int exceptionCount, Exception[] exceptions) {

        getContentPane().setLayout(new BorderLayout());
        if (exceptionCount == exceptions.length) {
            getContentPane().add(new JLabel("There were " + exceptionCount + " warnings when processing your request", UIManager.getIcon("OptionPane.warningIcon"), JLabel.LEFT), BorderLayout.NORTH);
        } else {
            getContentPane().add(new JLabel("There were " + exceptionCount + " warnings when processing your request - showing the first " + exceptions.length, UIManager.getIcon("OptionPane.warningIcon"), JLabel.LEFT), BorderLayout.NORTH);
        }
        StringBuilder b = new StringBuilder();
        for (Exception exception : exceptions) {
            b.append(exception.getMessage());
            b.append("\n");
        }
        JTextArea text = new JTextArea(b.toString());
        text.setEditable(false);
        getContentPane().add(new JScrollPane(text), BorderLayout.CENTER);

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton(MenuUtils.CLOSE_BUTTON);
        closeButton.addActionListener(this);
        closePanel.add(closeButton);
        getContentPane().add(closePanel, BorderLayout.SOUTH);

        setSize(600, 300);
        setModal(true);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
    }

}
