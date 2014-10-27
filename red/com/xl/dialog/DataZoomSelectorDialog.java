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

package com.xl.dialog;

import com.xl.main.REDApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Provides a small dialog which allows the user to select a suitable data zoom
 * level. Applies the selected level to currently visible data tracks
 */
public class DataZoomSelectorDialog extends DataZoomSelector implements ActionListener {

    /**
     * Instantiates a new data zoom selector.
     *
     * @param application
     */
    public DataZoomSelectorDialog(REDApplication application) {
        super(application);
        JButton closeButton = new JButton("Close");
        getRootPane().setDefaultButton(closeButton);
        closeButton.addActionListener(this);
        getContentPane().add(closeButton, BorderLayout.SOUTH);
        setSize(300, 100);
        setLocationRelativeTo(application);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        setVisible(false);
        dispose();
    }
}
