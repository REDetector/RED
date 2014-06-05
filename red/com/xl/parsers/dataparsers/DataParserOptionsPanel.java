package com.xl.parsers.dataparsers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xl.utils.NumberKeyListener;

public class DataParserOptionsPanel extends JPanel implements ItemListener, KeyListener, ActionListener {

	private JCheckBox removeDuplicates;
	private JCheckBox isHiC;
	private JTextField hiCDistance;
	private JCheckBox hiCIgnoreTransBox;
	private JCheckBox splitSplicedReads;
	private JCheckBox importIntrons;
	private JLabel importIntronsLabel;
	private JLabel hiCDistanceLabel;
	private JLabel hiCIgnoreTransLabel;
	private JCheckBox reverseReads;
	private JTextField extendReads;
	private JTextField minMappingQuality;
	private JComboBox<String> readType;
	private JTextField distanceLimit;
	private JPanel singleEndOptions;
	private JPanel pairedEndOptions;
	
	
	public DataParserOptionsPanel (boolean allowsPairedEnd, boolean allowsSplicing, boolean allowsReversal, boolean supportsQuality) {
		setLayout(new BorderLayout());
		
		JPanel commonOptions = new JPanel();
		
		commonOptions.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx=1;
		gbc.gridy=1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		
		commonOptions.add(new JLabel("Remove duplicate reads"),gbc);
		removeDuplicates = new JCheckBox();
		gbc.gridx=2;
		commonOptions.add(removeDuplicates,gbc);

		gbc.gridx=1;
		gbc.gridy++;
		commonOptions.add(new JLabel("Treat as HiC data"),gbc);
		isHiC = new JCheckBox();
		isHiC.addActionListener(this);
		gbc.gridx=2;
		commonOptions.add(isHiC,gbc);

		gbc.gridx=1;
		gbc.gridy++;
		hiCDistanceLabel = new JLabel("Min HiC interaction distance (bp)");
		hiCDistanceLabel.setEnabled(false);
		commonOptions.add(hiCDistanceLabel,gbc);
		hiCDistance = new JTextField("0");
		hiCDistance.addKeyListener(this);
		hiCDistance.setEnabled(false);
		gbc.gridx=2;
		commonOptions.add(hiCDistance,gbc);
		
		gbc.gridx=1;
		gbc.gridy++;
		hiCIgnoreTransLabel = new JLabel("Ignore HiC Trans hits");
		hiCIgnoreTransLabel.setEnabled(false);
		commonOptions.add(hiCIgnoreTransLabel,gbc);
		hiCIgnoreTransBox = new JCheckBox();
		hiCIgnoreTransBox.setEnabled(false);
		gbc.gridx=2;
		commonOptions.add(hiCIgnoreTransBox,gbc);
		
		minMappingQuality = new JTextField("0");
		if (supportsQuality) {
			gbc.gridx=1;
			gbc.gridy++;
			commonOptions.add(new JLabel("Min mapping quality"),gbc);
			gbc.gridx=2;
			minMappingQuality.addKeyListener(new NumberKeyListener(false, false, 255));
			commonOptions.add(minMappingQuality,gbc);
			
		}
		
		if (allowsReversal) {
			gbc.gridx=1;
			gbc.gridy++;
			commonOptions.add(new JLabel("Reverse all reads"),gbc);
			gbc.gridx=2;
			reverseReads = new JCheckBox();
			commonOptions.add(reverseReads,gbc);
		}
		
		if (allowsSplicing) {
			gbc.gridx=1;
			gbc.gridy++;
			commonOptions.add(new JLabel("Split spliced reads"),gbc);
			gbc.gridx=2;
			splitSplicedReads = new JCheckBox();
			splitSplicedReads.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (importIntrons != null) {
						importIntrons.setEnabled(splitSplicedReads.isSelected());
						importIntronsLabel.setEnabled(splitSplicedReads.isSelected());
					}
				}
			});
			commonOptions.add(splitSplicedReads,gbc);
			
			gbc.gridx=1;
			gbc.gridy++;
			// We need a separate label since we need to be able to disable it
			importIntronsLabel = new JLabel("Import Introns Rather than Exons");
			commonOptions.add(importIntronsLabel,gbc);
			gbc.gridx=2;
			importIntrons = new JCheckBox();
			importIntrons.setEnabled(false);
			importIntronsLabel.setEnabled(false);
			commonOptions.add(importIntrons,gbc);
			
		}
		
		if (allowsPairedEnd) {
			gbc.gridx=1;
			gbc.gridy++;
			
			commonOptions.add(new JLabel("Data Type"),gbc);
			readType = new JComboBox<String>(new String [] {"Single End","Paired End"});
			readType.addItemListener(this);
			gbc.gridx=2;
			commonOptions.add(readType,gbc);
		}		
		
		add(commonOptions,BorderLayout.NORTH);

		singleEndOptions = new JPanel();
		singleEndOptions.setLayout(new GridBagLayout());
		
		gbc.gridx=1;
		gbc.gridy=1;
		gbc.gridwidth = 2;
		
		singleEndOptions.add(new JLabel("Single End Options",JLabel.CENTER),gbc);
		gbc.gridwidth=1;
		gbc.gridy++;
		
		singleEndOptions.add(new JLabel("Extend reads by (bp)"),gbc);
		gbc.gridx=2;
		extendReads = new JTextField(6);
		extendReads.addKeyListener(this);
		singleEndOptions.add(extendReads,gbc);
		
		add(singleEndOptions,BorderLayout.SOUTH);
		
		pairedEndOptions = new JPanel();
		pairedEndOptions.setLayout(new GridBagLayout());
		gbc.gridx=1;
		gbc.gridy=1;
		gbc.gridwidth = 2;
		
		pairedEndOptions.add(new JLabel("Paired End Options",JLabel.CENTER),gbc);
		gbc.gridwidth=1;
		gbc.gridy++;
		
		pairedEndOptions.add(new JLabel("Pair Distance Cutoff (bp)"),gbc);
		gbc.gridx=2;
		distanceLimit = new JTextField("1000", 6);
		distanceLimit.addKeyListener(this);
		pairedEndOptions.add(distanceLimit,gbc);
		
	
	}
	
	public boolean pairedEnd () {
		return readType.getSelectedItem().equals("Paired End");
	}
	
	public boolean isHiC () {
		return isHiC.isSelected();
	}
	
	public int hiCDistance () {
		if (hiCDistance.getText().length()==0) return 0;
		else {
			return Integer.parseInt(hiCDistance.getText());
		}
	}
	
	public int minMappingQuality () {
		if (minMappingQuality.getText().length() == 0) return 0;
		return Integer.parseInt(minMappingQuality.getText());
	}
	
	public boolean hiCIgnoreTrans () {
		return hiCIgnoreTransBox.isSelected();
	}
	
	public boolean removeDuplicates () {
		return removeDuplicates.isSelected();
	}
	
	public boolean splitSplicedReads () {
		return splitSplicedReads.isSelected();
	}
	
	public boolean importIntrons () {
		return importIntrons.isSelected();
	}
	
	public boolean reverseReads () {
		return reverseReads.isSelected();
	}
	
	public int extendReads () {
		if (extendReads == null || extendReads.getText().length()==0) {
			return 0;
		}
		return Integer.parseInt(extendReads.getText());
	}
	
	public int pairDistanceCutoff () {
		if (distanceLimit == null || distanceLimit.getText().length()==0) {
			// Give them a default
			return 1000;
		}
		return Integer.parseInt(distanceLimit.getText());
	}


	public void itemStateChanged(ItemEvent e) {
		// The combo box selection changed
		if (readType.getSelectedItem().equals("Single End")) {
			remove(pairedEndOptions);
			add(singleEndOptions,BorderLayout.SOUTH);
			validate();
			repaint();
		}
		else {
			remove(singleEndOptions);
			add(pairedEndOptions,BorderLayout.SOUTH);
			validate();
			repaint();
		}
	}

	public Dimension getPreferredSize () {
		return new Dimension(300,250);
	}

	public void keyPressed(KeyEvent e) {}
	
	public void keyReleased(KeyEvent e) {
		
		JTextField source = (JTextField)e.getSource();
		try {
			int i = Integer.parseInt(source.getText());
			if (i < 0) {
				throw new Exception("Negative value");
			}
		}
		catch (Exception nfe) {
			if (source.getText().length()>0) {
				source.setText(source.getText().substring(0,source.getText().length()-1));
				// Do it again in case this doesn't fix the problem
				keyReleased(e);
			}
		}	
	}


	public void keyTyped(KeyEvent e) {}

	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource() == isHiC) {
			hiCDistance.setEnabled(isHiC());
			hiCDistanceLabel.setEnabled(isHiC());
			hiCIgnoreTransBox.setEnabled(isHiC());
			hiCIgnoreTransLabel.setEnabled(isHiC());
			distanceLimit.setEnabled(!isHiC());
		}
	}
}
