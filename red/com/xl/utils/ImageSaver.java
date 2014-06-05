package com.xl.utils;

/**
 * Copyright 2009-13 Simon Andrews
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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.xl.dialog.CrashReporter;
import com.xl.preferences.REDPreferences;
import com.xl.utils.filefilters.PNGFileFilter;
import com.xl.utils.filefilters.SVGFileFilter;

/**
 * A utility class which acts as a wrapper for the SVG or PNG generating
 * code which can be used to save (almost) any component which uses the
 * standard Graphics interface to draw itself.
 */
public class ImageSaver {

	/**
	 * Launches a file selector to select which type of file to create and
	 * then create it.
	 * 
	 * @param c The component to save.
	 */
	public static void saveImage (Component c) {
		JFileChooser chooser = new JFileChooser(REDPreferences.getInstance().getSaveLocation());
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new SVGFileFilter());
		PNGFileFilter pff = new PNGFileFilter();
		chooser.addChoosableFileFilter(pff);
		chooser.setFileFilter(pff);
		
		int result = chooser.showSaveDialog(c);
		if (result == JFileChooser.CANCEL_OPTION) return;

		File file = chooser.getSelectedFile();
		REDPreferences.getInstance().setLastUsedSaveLocation(file);
		
		if (file.isDirectory()) return;

		FileFilter filter = chooser.getFileFilter();
		
		if (filter instanceof PNGFileFilter) {		
			if (! file.getPath().toLowerCase().endsWith(".png")) {
				file = new File(file.getPath()+".png");
			}
		}
		else if (filter instanceof SVGFileFilter) {
			if (! file.getPath().toLowerCase().endsWith(".svg")) {
				file = new File(file.getPath()+".svg");
			}			
		}
		else {
			System.err.println("Unknown file filter type "+filter+" when saving image");
			return;
		}
		
		// Check if we're stepping on anyone's toes...
		if (file.exists()) {
			int answer = JOptionPane.showOptionDialog(c,file.getName()+" exists.  Do you want to overwrite the existing file?","Overwrite file?",0,JOptionPane.QUESTION_MESSAGE,null,new String [] {"Overwrite and Save","Cancel"},"Overwrite and Save");

			if (answer > 0) {
				return;
			}
		}

		try {					
			if (filter instanceof PNGFileFilter) {
				BufferedImage b = new BufferedImage(c.getWidth(),c.getHeight(),BufferedImage.TYPE_INT_RGB);
				Graphics g = b.getGraphics();			
				c.paint(g);

				ImageIO.write((BufferedImage)(b),"PNG",file);
			}
			else if (filter instanceof SVGFileFilter) {
				PrintWriter pr = new PrintWriter(new FileWriter(file));
				if (c instanceof SVGProducer) {
					((SVGProducer)c).writeSVG(pr);
				}
				else {
					SVGGenerator.writeSVG(pr,c);
				}
				pr.close();
			}
			else {
				System.err.println("Unknown file filter type "+filter+" when saving image");
				return;
			}
		}

		catch (IOException e) {
			new CrashReporter(e);
		}
	}
	
}
