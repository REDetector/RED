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

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.utils.ColourScheme;

import javax.swing.*;
import java.awt.*;

/**
 * The Class TypeColourRenderer provides a new schema for list rendering.
 */
public class TypeColourRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        JLabel l = new JLabel(value.toString());

        if (value instanceof DataSet) {
            l.setForeground(ColourScheme.DATASET_LIST);
            l.setBackground(ColourScheme.DATASET_LIST);
        } else if (value instanceof DataGroup) {
            l.setForeground(ColourScheme.DATAGROUP_LIST);
            l.setBackground(ColourScheme.DATAGROUP_LIST);
        }

        if (selected) {
            l.setForeground(Color.WHITE);
            l.setOpaque(true);
        }
        return l;
    }


}
