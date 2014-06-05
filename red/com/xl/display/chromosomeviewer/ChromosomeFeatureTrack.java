package com.xl.display.chromosomeviewer;

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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPanel;

import com.xl.datatypes.sequence.Location;
import com.xl.display.featureviewer.Feature;
import com.xl.display.featureviewer.FeatureViewer;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.ColourScheme;
import com.xl.utils.PositionFormat;
import com.xl.utils.Strand;

/**
 * The ChromosomeFeatureTrack is a display which shows one feature type in the
 * chromosome view. It is usually only created and managed by a surrounding
 * instance of ChromsomeViewer.
 */
public class ChromosomeFeatureTrack extends JPanel {

	/** The chromosome viewer which contains this track **/
	private ChromosomeViewer viewer;

	/** The active feature. */
	private Feature activeFeature = null;

	/** The features shown in this track */
	private Feature[] features;

	/** The name of the feature type shown in this track */
	private String featureName;

	/** The current width of this window */
	private int displayWidth;

	/** The height of this track */
	private int displayHeight;

	private final int exonHeight = 20;

	private final int cdsHeight = exonHeight / 2;

	/**
	 * An optimisation to allow us to miss out features which would be drawn
	 * right on top of each other
	 */
	private long lastXStart = 0;

	/**
	 * A list of drawn features, used for lookups when finding an active feature
	 */
	private Vector<DrawnBasicFeature> drawnFeatures = new Vector<DrawnBasicFeature>();

	/**
	 * Instantiates a new chromosome feature track. We have to send the name of
	 * the feature type explicitly in case there aren't any features of a given
	 * type on a chromosome and we couldn't then work out the name of the track
	 * from the features themselves.
	 * 
	 * @param viewer
	 *            The chromosome viewer which holds this track
	 * @param featureName
	 *            The name of the type of features we're going to show
	 * @param features
	 *            A list of features we're going to show
	 */
	public ChromosomeFeatureTrack(ChromosomeViewer viewer, String featureName,
			Feature[] features) {
		this.viewer = viewer;
		this.featureName = featureName;
		this.features = features;
		addMouseMotionListener(new BasicFeatureListener());
		addMouseListener(new BasicFeatureListener());
		drawnFeatures = new Vector<DrawnBasicFeature>();
	}

	public void updateBasicFeatures(Feature[] features) {
		this.features = features;
		repaint();
	}

	public String type() {
		return featureName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		if (g instanceof Graphics2D) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawnFeatures.clear();
		displayWidth = getWidth();
		displayHeight = getHeight();
		// if (viewer.getIndex(this) % 2 == 0) {
		g.setColor(ColourScheme.FEATURE_BACKGROUND_EVEN);
		// } else {
		// g.setColor(ColourScheme.FEATURE_BACKGROUND_ODD);
		// }

		g.fillRect(0, 0, displayWidth, displayHeight);

		if (viewer.makingSelection()) {
			int selStart = viewer.selectionStart();
			int selEnd = viewer.selectionEnd();
			int useStart = (selEnd > selStart) ? selStart : selEnd;
			int selWidth = selEnd - selStart;
			if (selWidth < 0)
				selWidth = 0 - selWidth;
			g.setColor(ColourScheme.DRAGGED_SELECTION);
			g.fillRect(useStart, 0, selWidth, displayHeight);
		}

		// Now go through all the features figuring out whether they
		// need to be displayed

		// Reset the optimisation tracker
		lastXStart = 0;

		int startBp = viewer.currentStart();
		int endBp = viewer.currentEnd();

		for (int i = 0; i < features.length; i++) {
			// System.out.println("Looking at feature " + featureName +
			// " Start: "
			// + features[i].getTxLocation().getStart() + " End: "
			// + features[i].getTxLocation().getEnd() + " Start:"
			// + startBp + " End:" + endBp);
			if (isFeatureVisible(features[i], startBp, endBp)) {
				// We always draw the active feature last so skip it here.

				if (features[i] != activeFeature) {
					drawBasicFeature(features[i], g);
				}

			}
		}
		// Finally redraw the active feature so it always goes on top
		lastXStart = 0;
		if (activeFeature != null)
			drawBasicFeature(activeFeature, g);

		// Draw a box into which we'll put the track name so it's not obscured
		// by the data
		int nameWidth = g.getFontMetrics().stringWidth(featureName);
		int nameHeight = g.getFontMetrics().getAscent();

		// if (viewer.getIndex(this) % 2 == 0) {
		// g.setColor(ColourScheme.FEATURE_BACKGROUND_EVEN);
		// } else {
		// g.setColor(ColourScheme.FEATURE_BACKGROUND_ODD);
		// }
		g.setColor(Color.ORANGE);
		g.fillRect(0, 1, nameWidth + 3, nameHeight + 3);

		// Lastly draw the name of the track
		g.setColor(Color.GRAY);
		g.drawString(featureName, 2, nameHeight + 2);

	}

	// There's no sense in letting the annotation tracks get too tall. We're
	// better off using that space for data tracks.
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	public Dimension getMinimumSize() {
		return new Dimension(30, 30);
	}

	/**
	 * Draws a single feature in the track
	 * 
	 * @param feature
	 *            the feature to draw
	 * @param g
	 *            the graphics object to use for drawing
	 */
	private void drawBasicFeature(Feature feature, Graphics g) {

		if (feature.getStrand() == Strand.POSITIVE) {
			g.setColor(ColourScheme.FORWARD_FEATURE);
		} else if (feature.getStrand() == Strand.NEGATIVE) {
			g.setColor(ColourScheme.REVERSE_FEATURE);
		} else {
			g.setColor(ColourScheme.UNKNOWN_FEATURE);
		}

		if (feature == activeFeature) {
			g.setColor(ColourScheme.ACTIVE_FEATURE);
		}

		// If there's space we'll put a label on the track as
		// well as the feature.
		boolean drawLabel = false;
		// int yBoxStart = 2;
		// int yBoxEnd = displayHeight - 2;
		// int yText = 0;
		// if (displayHeight > 25) {
		// drawLabel = true;
		// yBoxStart = 2;
		// yBoxEnd = displayHeight - 14;
		// yText = displayHeight - 2;
		// }

		Location tx = feature.getTxLocation();
		Location cds = feature.getCdsLocation();
		Location[] exons = feature.getExonLocations();
		int wholeXStart = bpToPixel(tx.getStart());
		int wholeXEnd = bpToPixel(tx.getEnd());
		g.fillRect(wholeXStart, displayHeight / 2 - 1, wholeXEnd - wholeXStart,
				2);

		if (wholeXEnd - wholeXStart < 3) {
			if (wholeXStart - lastXStart < 4) {
				return; // Skip this feature.
			}
			wholeXStart = wholeXEnd - 2;
		}
		int thickStart = cds.getStart();
		int thickEnd = cds.getEnd();
		int cdsStart = bpToPixel(thickStart);
		int cdsEnd = bpToPixel(thickEnd) - cdsStart;
		g.fillRect(cdsStart, displayHeight / 2 - cdsHeight / 2, cdsEnd,
				cdsHeight);
		for (Location exon : exons) {
			int exonStart = bpToPixel(exon.getStart());
			int exonEnd = bpToPixel(exon.getEnd()) - exonStart;
			if (exonStart > cdsStart && exonEnd < cdsEnd) {
				g.fillRect(exonStart, displayHeight / 2 - exonHeight / 2,
						exonEnd, exonHeight);
			}

			if (drawLabel
					&& (feature == activeFeature || viewer.showAllLables())) {
				g.setColor(Color.DARK_GRAY);
				// g.drawString(feature.getChr() + ":" + feature.getAliasName(),
				// (wholeXStart + wholeXEnd) / 2, yText);
			}

			drawnFeatures.add(new DrawnBasicFeature(wholeXStart, wholeXEnd,
					feature));
		}

	}

	/**
	 * Pixel to bp.
	 * 
	 * @param x
	 *            the x
	 * @return the int
	 */
	private int pixelToBp(int x) {
		int pos = viewer.currentStart()
				+ (int) (((double) x / displayWidth) * (viewer.currentEnd() - viewer
						.currentStart()));
		if (pos < 1)
			pos = 1;
		if (pos > viewer.chromosome().getLength())
			pos = viewer.chromosome().getLength();
		return pos;
	}

	/**
	 * Bp to pixel.
	 * 
	 * @param bp
	 *            the bp
	 * @return the int
	 */
	private int bpToPixel(int bp) {
		return (int) (((double) (bp - viewer.currentStart()) / ((viewer
				.currentEnd() - viewer.currentStart()))) * displayWidth);
	}

	private boolean isFeatureVisible(Feature feature, int currentStart,
			int currentEnd) {
		if (feature.getTxLocation().getStart() < currentEnd
				&& feature.getTxLocation().getEnd() > currentStart)
			return true;
		else {
			return false;
		}
	}

	/**
	 * A container class which stores a feature and its last drawn position in
	 * the display. Split location features will use a separate DrawnFeature for
	 * each exon.
	 */
	private class DrawnBasicFeature {

		/** The start. */
		private int start;

		/** The end. */
		private int end;

		/** The feature. */
		private Feature feature = null;

		/**
		 * Instantiates a new drawn feature.
		 * 
		 * @param start
		 *            the start position in pixels
		 * @param end
		 *            the end position in pixels
		 * @param feature
		 *            the feature
		 */
		public DrawnBasicFeature(int start, int end, Feature feature) {
			this.start = start;
			this.end = end;
			this.feature = feature;
		}

		/**
		 * Checks if a given pixel position is inside this feature.
		 * 
		 * @param x
		 *            the x pixel position
		 * @return true, if this falls within the last drawn position of this
		 *         feature
		 */
		public boolean isInFeature(int x) {
			if (x >= start && x <= end) {
				return true;
			} else {
				return false;
			}
		}

	}

	/**
	 * The listener interface for receiving feature events. The class that is
	 * interested in processing a feature event implements this interface, and
	 * the object created with that class is registered with a component using
	 * the component's <code>addFeatureListener<code> method. When
	 * the feature event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see FeatureEvent
	 */
	private class BasicFeatureListener implements MouseMotionListener,
			MouseListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
		 * )
		 */
		public void mouseDragged(MouseEvent me) {
			viewer.setSelectionEnd(me.getX());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent
		 * )
		 */
		public void mouseMoved(MouseEvent me) {
			int x = me.getX();
			Enumeration<DrawnBasicFeature> e = drawnFeatures.elements();
			while (e.hasMoreElements()) {
				DrawnBasicFeature drawnFeature = e.nextElement();
				if (drawnFeature.isInFeature(x)) {
					if (activeFeature != drawnFeature.feature) {
						int length = drawnFeature.feature.getTotalLength();
						viewer.application().setStatusText(
								drawnFeature.feature.getChr()
										+ ": "
										+ drawnFeature.feature.getAliasName()
										+ " "
										+ drawnFeature.feature.getTxLocation()
												.getStart()
										+ "-"
										+ drawnFeature.feature.getTxLocation()
												.getEnd() + " ("
										+ PositionFormat.formatLength(length)
										+ ")");
						activeFeature = drawnFeature.feature;
						repaint();
						return;
					} else {
						int length = activeFeature.getTotalLength();
						viewer.application().setStatusText(
								activeFeature.getChr()
										+ ": "
										+ activeFeature.getAliasName()
										+ " "
										+ activeFeature.getTxLocation()
												.getStart()
										+ "-"
										+ activeFeature.getTxLocation()
												.getEnd() + " ("
										+ PositionFormat.formatLength(length)
										+ ")");
						repaint();
						return;
					}
				} else {
					viewer.application().setStatusText(
							"Chromsome "
									+ DisplayPreferences.getInstance()
											.getCurrentChromosome().getName()
									+ " " + pixelToBp(me.getX()) + "bp");
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent me) {
			if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
				viewer.zoomOut();
				return;
			}
			if (me.getClickCount() >= 2) {
				if (activeFeature != null) {
					new FeatureViewer(activeFeature);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent me) {
			viewer.setMakingSelection(true);
			viewer.setSelectionStart(me.getX());
			viewer.setSelectionEnd(me.getX());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent me) {
			viewer.setMakingSelection(false);

			int width = viewer.selectionEnd() - viewer.selectionStart();
			if (width < 0) {
				width = 0 - width;
			}

			// Stop people from accidentally making really short selections
			if (width < 5)
				return;

			DisplayPreferences.getInstance().setLocation(
					pixelToBp(viewer.selectionStart()),
					pixelToBp(viewer.selectionEnd()));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent arg0) {
			if (activeFeature != null)
				viewer.application().setStatusText(
						" " + activeFeature.getAliasName());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent arg0) {
			activeFeature = null;
			repaint();
		}

	}

}
