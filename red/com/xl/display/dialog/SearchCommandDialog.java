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

import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Administrator on 2015/3/11.
 */
public class SearchCommandDialog extends JDialog implements ActionListener {

    private JTextField textField;
    private JButton button;

    public SearchCommandDialog() {
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        textField = new JTextField(30);
        textField.setToolTipText("Please enter chromosome name(e.g., chr2), locus(e.g., chr2:1003-2025) or feature name(PRMT1)");
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 4;
        c.weighty = 1;
        getContentPane().add(textField, c);

        c.gridx = 4;
        c.weightx = 1;
        button = new JButton("Search...");
        getContentPane().add(button, c);
        button.setActionCommand(MenuUtils.SEARCH_BUTTON);
        button.addActionListener(this);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(MenuUtils.SEARCH_BUTTON)) {
            String searchField = textField.getText();
            if (searchField != null) {
                SearchCommand c = new SearchCommand(textField.getText());
                c.execute();
            }
        }
    }
}
