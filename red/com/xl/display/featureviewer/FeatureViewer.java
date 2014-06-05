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
package com.xl.display.featureviewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;

/**
 * The Class FeatureViewer shows the key/value annotations for a selected
 * feature.
 */
public class FeatureViewer extends JDialog implements MouseListener,KeyListener {

	/**
	 * Instantiates a new feature viewer.
	 * 
	 * @param feature
	 *            the feature
	 * @param application
	 *            the application
	 */

	private JTable table = null;
	private FeatureAttributeTable model = null;
	private Feature feature = null;

	public FeatureViewer(Feature feature) {
		super(REDApplication.getInstance(), "Feature: "+feature.getAliasName());

		this.feature = feature;
		model = new FeatureAttributeTable(feature);
		table = new JTable(model);
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(true);
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(350);
		table.addMouseListener(this);
		table.addKeyListener(this);

		setContentPane(new JScrollPane(table));

		setSize(550, 300);
		setLocationRelativeTo(REDApplication.getInstance());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);

	}

	public void mouseClicked(MouseEvent me) {
		if (me.getClickCount() == 2) {

			System.err.println("Selected row is " + table.getSelectedRow()
					+ " value is "
					+ model.getValueAt(table.getSelectedRow(), 0));

			if (model.getValueAt(table.getSelectedRow(), 0).equals(
					"Transcription")
					|| model.getValueAt(table.getSelectedRow(), 0).equals(
							"Coding Region")
					|| model.getValueAt(table.getSelectedRow(), 0).equals(
							"Exons")) {
				DisplayPreferences.getInstance().setLocation(feature.getChr(),
						feature.getTxLocation().getStart(),
						feature.getTxLocation().getEnd());
				dispose();
			}
		}

	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}


	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
			dispose();
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
