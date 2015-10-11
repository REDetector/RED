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

import com.xl.display.panel.RedTitlePanel;
import com.xl.main.RedApplication;
import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog to tell the users about the version, authors and license.
 */
public class AboutDialog extends JDialog {

    /**
     * Instantiates a new about dialog.
     */
    public AboutDialog() {
        super(RedApplication.getInstance());
        setTitle("About RED...");
        Container cont = getContentPane();
        cont.setLayout(new BorderLayout());

        add(new RedTitlePanel(), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton closeButton = new JButton(MenuUtils.CLOSE_BUTTON);
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
        setLocationRelativeTo(RedApplication.getInstance());
        setResizable(false);
        setVisible(true);
    }

}
