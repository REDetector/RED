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

package com.xl.display.chromosomeviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.feature.Feature;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.sequence.Location;
import com.xl.display.featureviewer.FeatureViewer;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.ColourScheme;
import com.xl.utils.PositionFormat;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The ChromosomeFeatureTrack is a display which shows the feature from gene annotation file (e.g., UCSC) in the chromosome view. It is usually only created and
 * managed by a surrounding instance of ChromosomeViewer.
 */
public class ChromosomeFeatureTrack extends AbstractTrack {

    /**
     * The active feature.
     */
    private Feature activeFeature = null;

    /**
     * The features shown in this track.
     */
    private java.util.List<Feature> features;
    /**
     * The height of the label.
     */
    private int yLabelHeight = 0;
    /**
     * Exon height.
     */
    private int exonHeight = 20;
    /**
     * Coding regions height
     */
    private int cdsHeight = exonHeight / 2;
    /**
     * The transcriptome position.
     */
    private int txYPosition;
    /**
     * The coding regions position.
     */
    private int cdsYPosition;
    /**
     * The exon position.
     */
    private int exonYPosition;
    /**
     * The cursor's X position.
     */
    private int cursorXPosition = 0;
    /**
     * The collection.
     */
    private DataCollection collection;

    /**
     * A list of drawn features, used for look-ups when finding an active feature
     */
    private Vector<DrawnFeature> drawnFeatures = new Vector<DrawnFeature>();

    /**
     * Instantiates a new chromosome feature track. We have to send the name of the feature type explicitly in case there aren't any features of a given type on
     * a chromosome and we couldn't then work out the name of the track from the features themselves.
     *
     * @param viewer      The chromosome viewer which holds this track
     * @param collection  The data collection
     * @param featureName The name of the type of features we're going to show
     */
    public ChromosomeFeatureTrack(ChromosomeViewer viewer, DataCollection collection, String featureName) {
        super(viewer, featureName);
        this.collection = collection;
        drawnFeatures = new Vector<DrawnFeature>();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        super.paint(g);
        drawnFeatures.clear();

        txYPosition = displayHeight / 2 - 1;
        cdsYPosition = displayHeight / 2 - exonHeight / 4;
        exonYPosition = displayHeight / 2 - exonHeight / 2;

        g.setColor(ColourScheme.FEATURE_TRACK);
        for (Feature feature : features) {
            if (isFeatureVisible(feature)) {
                // We always draw the active feature last so skip it here.
                if (feature != activeFeature) {
                    drawFeature(feature, g);
                }
            }
        }

        if (activeFeature != null) {
            g.setColor(ColourScheme.ACTIVE_FEATURE);
            drawFeature(activeFeature, g);
            g.setColor(ColourScheme.FEATURE_TRACK);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(30, 30);
    }

    /**
     * Draws a single feature in the track
     *
     * @param feature the feature to draw
     * @param g       the graphics object to use for drawing
     */
    private void drawFeature(Feature feature, Graphics g) {
        // If there's space we'll put a label on the track as well as the feature.
        boolean drawLabel = false;
        if (displayHeight > 25) {
            drawLabel = true;
            yLabelHeight = 5;
        }
        ArrayList<Location> allLocation = (ArrayList<Location>) feature.getAllLocations();

        int wholeXStart = allLocation.get(0).getStart();
        int wholeXEnd = allLocation.get(0).getEnd();
        drawnFeatures.add(new DrawnFeature(wholeXStart, wholeXEnd, feature));
        fillRect(g, wholeXStart, wholeXEnd, txYPosition - yLabelHeight, 2);

        int cdsStart = allLocation.get(1).getStart();
        int cdsEnd = allLocation.get(1).getEnd();
        fillRect(g, cdsStart, cdsEnd, cdsYPosition - yLabelHeight, cdsHeight);

        for (int i = 2, len = allLocation.size(); i < len; i++) {
            int exonStart = allLocation.get(i).getStart();
            int exonEnd = allLocation.get(i).getEnd();
            if (exonStart > cdsStart && exonEnd < cdsEnd) {
                fillRect(g, exonStart, exonEnd, exonYPosition - yLabelHeight, exonHeight);
            }
        }
        if (drawLabel) {
            String featureName = feature.getAliasName();
            int baseWidth = g.getFontMetrics().stringWidth(String.valueOf(featureName));
            if (getViewerLength() < getWidth()) {
                g.drawString(featureName, cursorXPosition, ((displayHeight + exonHeight) / 2 + yLabelHeight));
            } else if (getViewerLength() < 500000) {
                g.drawString(featureName, (bpToPixel(wholeXStart) + bpToPixel(wholeXEnd) - baseWidth) / 2, ((displayHeight + exonHeight) / 2 + yLabelHeight));
            }
        }

    }

    /**
     * Update track from annotation collection.
     *
     * @param chromosome the chromosome
     */
    @Override
    protected void updateTrack(Chromosome chromosome) {
        features = collection.genome().getAnnotationCollection().getFeaturesForChr(chromosome);
        repaint();
    }

    /**
     * A simple way to tell RED whether a feature should be drawn.
     *
     * @param feature The feature
     * @return true if it is visible
     */
    private boolean isFeatureVisible(Feature feature) {
        return (feature.getTxLocation().getStart() < currentViewerEnd && feature.getTxLocation().getEnd() > currentViewerStart);
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        cursorXPosition = me.getX();
        Enumeration<DrawnFeature> e = drawnFeatures.elements();
        while (e.hasMoreElements()) {
            DrawnFeature drawnFeature = e.nextElement();
            if (drawnFeature.isInFeature(pixelToBp(me.getX()))) {
                if (activeFeature != drawnFeature.feature) {
                    int length = drawnFeature.feature.getTotalLength();
                    chromosomeViewer.application().setStatusText(
                            drawnFeature.feature.getChr() + ": " + drawnFeature.feature.getAliasName() + " " +
                                    drawnFeature.feature.getTxLocation().getStart() + "-" + drawnFeature.feature
                                    .getTxLocation().getEnd() + " (" + PositionFormat.formatLength(length,
                                    PositionFormat.UNIT_BASEPAIR) + ")");
                    activeFeature = drawnFeature.feature;
                    repaint();
                    return;
                } else {
                    int length = activeFeature.getTotalLength();
                    chromosomeViewer.application().setStatusText(
                            activeFeature.getChr() + ": " + activeFeature.getAliasName() + " " + activeFeature
                                    .getTxLocation().getStart() + "-" + activeFeature.getTxLocation().getEnd() +
                                    " (" + PositionFormat.formatLength(length, PositionFormat.UNIT_BASEPAIR) + ")");
                    repaint();
                    return;
                }
            } else {
                chromosomeViewer.application().setStatusText("Chromosome " + DisplayPreferences.getInstance().getCurrentChromosome().getName() + " "
                        + pixelToBp(me.getX()) + "bp");
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        super.mouseClicked(me);
        if (me.getClickCount() >= 2) {
            if (activeFeature != null) {
                new FeatureViewer(activeFeature);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        if (activeFeature != null)
            chromosomeViewer.application().setStatusText(" " + activeFeature.getAliasName());
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        activeFeature = null;
        repaint();
    }

    /**
     * A container class which stores a feature and its last drawn position in the display.
     */
    private class DrawnFeature {

        /**
         * The start.
         */
        private int start;

        /**
         * The end.
         */
        private int end;

        /**
         * The feature.
         */
        private Feature feature = null;

        /**
         * Instantiates a new drawn feature.
         *
         * @param start   the start position in pixels
         * @param end     the end position in pixels
         * @param feature the feature
         */
        public DrawnFeature(int start, int end, Feature feature) {
            this.start = start;
            this.end = end;
            this.feature = feature;
        }

        /**
         * Checks if a given pixel position is inside this feature.
         *
         * @param x the x pixel position
         * @return true, if this falls within the last drawn position of this feature
         */
        public boolean isInFeature(int x) {
            return (x >= start && x <= end);
        }
    }
}
