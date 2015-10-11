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

import com.xl.datatypes.sites.SiteList;
import com.xl.main.RedApplication;
import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The Class SiteListCommentEditDialog provide a dialog for user to write comments for a site list.
 */
public class SiteListCommentEditDialog extends JDialog implements ActionListener {
    /**
     * The site list to be commented.
     */
    private SiteList list;
    /**
     * The text area.
     */
    private JTextArea editor;

    public SiteListCommentEditDialog(SiteList list, Component c) {

        super(RedApplication.getInstance(), "Edit comments for " + list.getListName());

        this.list = list;

        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());

        panel.add(new JLabel("You can use this comments section to record the rationale for a particular filter or site generation.", JLabel.CENTER), BorderLayout.NORTH);

        editor = new JTextArea();
        editor.setWrapStyleWord(true);
        editor.setLineWrap(true);
        editor.setText(list.comments());

        panel.add(new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton closeButton = new JButton(MenuUtils.CLOSE_BUTTON);
        closeButton.addActionListener(this);
        closeButton.setActionCommand(MenuUtils.CLOSE_BUTTON);
        buttonPanel.add(closeButton);

        JButton saveButton = new JButton(MenuUtils.SAVE_BUTTON);
        saveButton.setActionCommand(MenuUtils.SAVE_BUTTON);
        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(panel);

        setSize(600, 300);
        setLocationRelativeTo(c);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals(MenuUtils.SAVE_BUTTON)) {
            list.setComments(editor.getText());
        }

        setVisible(false);
        dispose();

    }

}
