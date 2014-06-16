/**
 * Copyright 2010-13 Simon Andrews
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
package com.xl.panel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xl.main.REDApplication;
import com.xl.utils.ColourScheme;
import com.xl.utils.IconUtils;

/**
 * The Class SeqMonkTitlePanel.
 */
public class REDTitlePanel extends JPanel {

	/**
	 * Provides a small panel which gives details of the RED version and
	 * copyright. Used in both the welcome panel and the about dialog.
	 */
	public REDTitlePanel() {
		setLayout(new BorderLayout(5, 1));

		add(new JLabel("", IconUtils.LOGO_1, JLabel.CENTER), BorderLayout.WEST);
		add(new JLabel("", IconUtils.LOGO_2, JLabel.CENTER), BorderLayout.EAST);
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.weighty = 1;
		gridBagConstraints.insets = new Insets(6, 6, 0, 0);
		gridBagConstraints.fill = GridBagConstraints.NONE;

		JLabel program = new SmoothJLabel("RED---RNA Editing Detection",
				JLabel.CENTER);
		program.setFont(new Font("Dialog", Font.BOLD, 18));
		program.setForeground(ColourScheme.FORWARD_FEATURE);
		jPanel.add(program, gridBagConstraints);

		gridBagConstraints.gridy++;
		JLabel version = new SmoothJLabel("Version: " + REDApplication.VERSION,
				JLabel.CENTER);
		version.setFont(new Font("Dialog", Font.BOLD, 15));
		version.setForeground(ColourScheme.REVERSE_FEATURE);
		jPanel.add(version, gridBagConstraints);

		gridBagConstraints.gridy++;

		// Use a text field so they can copy this
		JTextField buptWebsite = new JTextField(" http://www.bupt.edu.cn/ ");
		buptWebsite.setFont(new Font("Dialog", Font.PLAIN, 14));
		buptWebsite.setEditable(false);
		buptWebsite.setBorder(null);
		buptWebsite.setOpaque(false);
		buptWebsite.setHorizontalAlignment(JTextField.CENTER);
		jPanel.add(buptWebsite, gridBagConstraints);
		gridBagConstraints.gridy++;
		JLabel buptCopyright = new JLabel(
				"\u00a9 Xing Li, Di Wu, Yongmei Sun, Internet of Things, BUPT, 2012-14",
				JLabel.CENTER);
		buptCopyright.setFont(new Font("Dialog", Font.PLAIN, 14));
		jPanel.add(buptCopyright, gridBagConstraints);
		gridBagConstraints.gridy++;
	
		// Use a text field so they can copy this
		JTextField website = new JTextField(" http://www.cqmu.edu.cn/ ");
		website.setFont(new Font("Dialog", Font.PLAIN, 14));
		website.setEditable(false);
		website.setBorder(null);
		website.setOpaque(false);
		website.setHorizontalAlignment(JTextField.CENTER);
		jPanel.add(website, gridBagConstraints);
		gridBagConstraints.gridy++;

		JLabel cqmuCopyright = new JLabel(
				"\u00a9 Keyue Ding, Qi Pan, Liver Cancer Research Center, CQMU, 2013-14",
				JLabel.CENTER);
		cqmuCopyright.setFont(new Font("Dialog", Font.PLAIN, 14));
		jPanel.add(cqmuCopyright, gridBagConstraints);
		gridBagConstraints.gridy++;
		add(jPanel, BorderLayout.CENTER);
	}

	/**
	 * A JLabel with anti-aliasing enabled. Takes the same constructor
	 * arguements as JLabel
	 */
	private class SmoothJLabel extends JLabel {

		/**
		 * Creates a new label
		 * 
		 * @param text
		 *            The text
		 * @param position
		 *            The JLabel constant position for alignment
		 */
		public SmoothJLabel(String text, int position) {
			super(text, position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		public void paintComponent(Graphics g) {
			if (g instanceof Graphics2D) {
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
			}
			super.paintComponent(g);
		}

	}

}