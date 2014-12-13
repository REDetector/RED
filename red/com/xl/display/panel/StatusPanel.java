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

package com.xl.display.panel;

import com.xl.utils.MemoryMonitor;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * The Class StatusPanel shows the interactive bar at the bottom of the main application screen.
 */
public class StatusPanel extends JPanel {

    /**
     * The label.
     */
    private JLabel label = new JLabel("RED---RNA Editing Detector", JLabel.LEFT);

    /**
     * Instantiates a new status panel.
     */
    public StatusPanel() {
        setLayout(new BorderLayout());
        add(label, BorderLayout.WEST);
        add(new MemoryMonitor(), BorderLayout.EAST);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    /**
     * Sets the text.
     *
     * @param text the new text
     */
    public void setText(String text) {
        label.setText(text);
    }
}
