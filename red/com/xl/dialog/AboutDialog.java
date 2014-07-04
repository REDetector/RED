package com.xl.dialog;

/**
 * Copyright Copyright 2007-13 Simon Andrews
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

        setSize(750, 200);
        setLocationRelativeTo(REDApplication.getInstance());
        setResizable(false);
        setVisible(true);
    }

}
