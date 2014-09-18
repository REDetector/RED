package com.xl.display.chromosomeviewer;

import com.xl.datatypes.sequence.Location;
import com.xl.display.featureviewer.Feature;
import com.xl.display.featureviewer.FeatureViewer;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.ColourScheme;
import com.xl.utils.PositionFormat;
import com.xl.utils.Strand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The ChromosomeFeatureTrack is a display which shows one feature type in the
 * chromosome view. It is usually only created and managed by a surrounding
 * instance of ChromsomeViewer.
 */
public class ChromosomeFeatureTrack extends JPanel {

    /**
     * The chromosome viewer which contains this track *
     */
    private ChromosomeViewer viewer;

    /**
     * The active feature.
     */
    private Feature activeFeature = null;

    /**
     * The features shown in this track
     */
    private Feature[] features;

    /**
     * The name of the feature type shown in this track
     */
    private String featureName;

    /**
     * The current width of this window
     */
    private int displayWidth;

    /**
     * The height of this track
     */
    private int displayHeight;

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
     * @param features    A list of features we're going to show
     */
    public ChromosomeFeatureTrack(ChromosomeViewer viewer, String featureName, Feature[] features) {
        this.viewer = viewer;
        this.featureName = featureName;
        this.features = features;
        addMouseMotionListener(new FeatureListener());
        addMouseListener(new FeatureListener());
        drawnFeatures = new Vector<DrawnFeature>();
    }

    public void updateFeature(Feature[] features) {
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
        txYPosition = displayHeight / 2 - 1;
        cdsYPosition = displayHeight / 2 - exonHeight / 4;
        exonYPosition = displayHeight / 2 - exonHeight / 2;
        g.setColor(ColourScheme.FEATURE_BACKGROUND_EVEN);
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

        int startBp = viewer.currentStart();
        int endBp = viewer.currentEnd();

        for (Feature feature : features) {
            if (isFeatureVisible(feature, startBp, endBp)) {
                // We always draw the active feature last so skip it here.
                if (feature != activeFeature) {
                    drawBasicFeature(feature, g);
                }
            }
        }

        if (activeFeature != null)
            drawBasicFeature(activeFeature, g);

        // Draw a box into which we'll put the track name so it's not obscured
        // by the data
        int nameWidth = g.getFontMetrics().stringWidth(featureName);
        int nameHeight = g.getFontMetrics().getAscent();

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

    private Color getFeatureColor(Feature feature) {
        if (feature.getStrand() == Strand.POSITIVE) {
            return ColourScheme.FORWARD_FEATURE;
        } else if (feature.getStrand() == Strand.NEGATIVE) {
            return ColourScheme.REVERSE_FEATURE;
        } else if (feature == activeFeature) {
            return ColourScheme.ACTIVE_FEATURE;
        } else {
            return ColourScheme.UNKNOWN_FEATURE;
        }

    }

    /**
     * Draws a single feature in the track
     *
     * @param feature the feature to draw
     * @param g       the graphics object to use for drawing
     */
    private void drawBasicFeature(Feature feature, Graphics g) {
        g.setColor(getFeatureColor(feature));

        // If there's space we'll put a label on the track as well as the feature.
        boolean drawLabel = false;
        if (displayHeight > 25) {
            drawLabel = true;
            yLableHeight = 5;
        }
        Location tx = feature.getTxLocation();
        Location cds = feature.getCdsLocation();
        Location[] exons = feature.getExonLocations();
        int wholeXStart = bpToPixel(tx.getStart());
        int wholeXEnd = bpToPixel(tx.getEnd());
        drawnFeatures.add(new DrawnFeature(wholeXStart, wholeXEnd,
                feature));
        g.fillRect(wholeXStart, txYPosition - yLableHeight, wholeXEnd - wholeXStart, 2);

        int cdsStart = bpToPixel(cds.getStart());
        int cdsEnd = bpToPixel(cds.getEnd()) - cdsStart;
        g.fillRect(cdsStart, cdsYPosition - yLableHeight, cdsEnd, cdsHeight);
        for (Location exon : exons) {
            int exonStart = bpToPixel(exon.getStart());
            int exonEnd = bpToPixel(exon.getEnd()) - exonStart;
            if (exonStart > cdsStart && exonEnd < cdsEnd) {
                g.fillRect(exonStart, exonYPosition - yLableHeight, exonEnd, exonHeight);
            }

            if (drawLabel && (feature == activeFeature)) {
//                g.setColor(Color.DARK_GRAY);
                g.drawString(feature.getChr() + ":" + feature.getAliasName(), cursorXPosition, ((displayHeight + exonHeight) / 2 + yLableHeight));
            }
        }
    }

    /**
     * Pixel to bp.
     *
     * @param x the x
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
     * @param bp the bp
     * @return the int
     */
    private int bpToPixel(int bp) {
        return (int) (((double) (bp - viewer.currentStart()) / ((viewer
                .currentEnd() - viewer.currentStart()))) * displayWidth);
    }

    private boolean isFeatureVisible(Feature feature, int currentStart,
                                     int currentEnd) {
        return (feature.getTxLocation().getStart() < currentEnd
                && feature.getTxLocation().getEnd() > currentStart);
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

    /**
     * The listener interface for receiving feature events. The class that is
     * interested in processing a feature event implements this interface, and
     * the object created with that class is registered with a component using
     * the component's <code>addFeatureListener<code> method. When
     * the feature event occurs, that object's appropriate
     * method is invoked.
     */
    private class FeatureListener implements MouseMotionListener,
            MouseListener {
        private long currentTime = 0;

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
            cursorXPosition = x;
            Enumeration<DrawnFeature> e = drawnFeatures.elements();
            while (e.hasMoreElements()) {
                DrawnFeature drawnFeature = e.nextElement();
                if (drawnFeature.isInFeature(x)) {
                    if (activeFeature != drawnFeature.feature) {
                        int length = drawnFeature.feature.getTotalLength();
                        viewer.application().setStatusText(
                                drawnFeature.feature.getChr() + ": " + drawnFeature.feature.getAliasName() + " " +
                                        drawnFeature.feature.getTxLocation().getStart() + "-" + drawnFeature.feature
                                        .getTxLocation().getEnd() + " (" + PositionFormat.formatLength(length,
                                        PositionFormat.UNIT_BASEPAIR) + ")");
                        activeFeature = drawnFeature.feature;
                        repaint();
                        return;
                    } else {
                        int length = activeFeature.getTotalLength();
                        viewer.application().setStatusText(
                                activeFeature.getChr() + ": " + activeFeature.getAliasName() + " " + activeFeature
                                        .getTxLocation().getStart() + "-" + activeFeature.getTxLocation().getEnd() +
                                        " (" + PositionFormat.formatLength(length, PositionFormat.UNIT_BASEPAIR) + ")");
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
            if (checkClickTime() && me.getClickCount() >= 2) {
                if (activeFeature != null) {
                    new FeatureViewer(activeFeature);
                }
            } else {
                viewer.zoomIn();
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
