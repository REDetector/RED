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
package com.xl.dialog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.utils.ColourScheme;

public class TypeColourRenderer extends DefaultListCellRenderer {
		
	public Component getListCellRendererComponent (JList<?> list,Object value, int index, boolean selected, boolean hasFocus) {
		JLabel l = new JLabel(value.toString());
		
		if (value instanceof DataSet) {
			l.setForeground(ColourScheme.DATASET_LIST);
			l.setBackground(ColourScheme.DATASET_LIST);
		}
		else if (value instanceof DataGroup){
			l.setForeground(ColourScheme.DATAGROUP_LIST);
			l.setBackground(ColourScheme.DATAGROUP_LIST);
		}
		else {
			// Should only be replicate sets
			l.setForeground(ColourScheme.REPLICATE_SET_LIST);
			l.setBackground(ColourScheme.REPLICATE_SET_LIST);
			
		}

		if (selected) {
			l.setForeground(Color.WHITE);
			l.setOpaque(true);
		}
		return l;
	}


}