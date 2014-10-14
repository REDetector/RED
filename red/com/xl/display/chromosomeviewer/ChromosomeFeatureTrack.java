package com.xl.display.chromosomeviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.sequence.Location;
import com.xl.display.featureviewer.Feature;
import com.xl.display.featureviewer.FeatureViewer;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.ColourScheme;
import com.xl.utils.PositionFormat;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The ChromosomeFeatureTrack is a display which shows one feature type in the
 * chromosome view. It is usually only created and managed by a surrounding
 * instance of ChromsomeViewer.
 */
public class ChromosomeFeatureTrack extends AbstractTrack {

    /**
     * The active feature.
     */
    private Feature activeFeature = null;

    /**
     * The features shown in this track
     */
    private Feature[] features;

    private long currentTime = 0;

    private int yLableHeight = 0;

    private int exonHeight = 20;
    private int cdsHeight = exonHeight / 2;
    private int txYPosition;
    private int cdsYPosition;
    private int exonYPosition;

    private int cursorXPosition = 0;

    /**
     * A list of drawn features, used for lookups when finding an active feature
     */
    private Vector<DrawnFeature> drawnFeatures = new Vector<DrawnFeature>();

    /**
     * Instantiates a new chromosome feature track. We have to send the name of
     * the feature type explicitly in case there aren't any features of a given
     * type on a chromosome and we couldn't then work out the name of the track
     * from the features themselves.
     *
     * @param viewer      The chromosome viewer which holds this track
     * @param featureName The name of the type of features we're going to show
     */
    public ChromosomeFeatureTrack(ChromosomeViewer viewer, DataCollection collection, String featureName) {
        super(viewer, collection, featureName);
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
            if (isFeatureVisible(feature, currentViewerStart, currentViewerEnd)) {
                // We always draw the active feature last so skip it here.
                if (feature != activeFeature) {
                    drawBasicFeature(feature, g);
                }
            }
        }

        if (activeFeature != null) {
            g.setColor(ColourScheme.ACTIVE_FEATURE);
            drawBasicFeature(activeFeature, g);
            g.setColor(ColourScheme.FEATURE_TRACK);
        }
    }

    public Dimension getMinimumSize() {
        return new Dimension(30, 30);
    }

    /**
     * Draws a single feature in the track
     *
     * @param feature the feature to draw
     * @param g       the graphics object to use for drawing
     */
    private void drawBasicFeature(Feature feature, Graphics g) {
        // If there's space we'll put a label on the track as well as the feature.
        boolean drawLabel = false;
        if (displayHeight > 25) {
            drawLabel = true;
            yLableHeight = 5;
        }
        Location tx = feature.getTxLocation();
        Location cds = feature.getCdsLocation();
        Location[] exons = feature.getExonLocations();
        int wholeXStart = tx.getStart();
        int wholeXEnd = tx.getEnd();
        drawnFeatures.add(new DrawnFeature(wholeXStart, wholeXEnd, feature));
        fillRect(g, wholeXStart, wholeXEnd, txYPosition - yLableHeight, 2);

        int cdsStart = cds.getStart();
        int cdsEnd = cds.getEnd();
        fillRect(g, cdsStart, cdsEnd, cdsYPosition - yLableHeight, cdsHeight);

        for (Location exon : exons) {
            int exonStart = exon.getStart();
            int exonEnd = exon.getEnd();
            if (exonStart > cdsStart && exonEnd < cdsEnd) {
                fillRect(g, exonStart, exonEnd, exonYPosition - yLableHeight, exonHeight);
            }

            if (drawLabel && (feature == activeFeature)) {
//                g.setColor(Color.DARK_GRAY);
                g.drawString(feature.getChr() + ":" + feature.getAliasName(), cursorXPosition, ((displayHeight + exonHeight) / 2 + yLableHeight));
            }
        }
    }

    @Override
    protected void updateTrack(Chromosome chromosome) {
        features = dataCollection.genome().getAnnotationCollection().getFeaturesForChr(chromosome);
        repaint();
    }

    private boolean isFeatureVisible(Feature feature, int currentStart, int currentEnd) {
        return (feature.getTxLocation().getStart() < currentEnd && feature.getTxLocation().getEnd() > currentStart);
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
                chromosomeViewer.application().setStatusText("Chromsome " + DisplayPreferences.getInstance().getCurrentChromosome().getName() + " "
                        + pixelToBp(me.getX()) + "bp");
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        super.mouseClicked(me);
        if (checkClickTime() && me.getClickCount() >= 2) {
            if (activeFeature != null) {
                new FeatureViewer(activeFeature);
            }
        }
    }

    public boolean checkClickTime() {
        long nowTime = (new Date()).getTime();
        if ((nowTime - currentTime) < 300) {
            currentTime = nowTime;
            return true;
        }
        currentTime = nowTime;
        return false;
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
     * A container class which stores a feature and its last drawn position in
     * the display. Split location features will use a separate DrawnFeature for
     * each exon.
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
         * @return true, if this falls within the last drawn position of this
         * feature
         */
        public boolean isInFeature(int x) {
            return (x >= start && x <= end);
        }
    }
}
