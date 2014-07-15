package com.xl.display.genomeviewer;

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.dialog.CrashReporter;
import com.xl.exception.REDException;
import com.xl.gradients.ColourIndexSet;
import com.xl.interfaces.DataChangeListener;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.ColourScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

/**
 * The Class ChromosomeDisplay shows a single chromosome within the genome view.
 */
public class ChromosomeDisplay extends JPanel implements DataChangeListener {

    /**
     * The max len.
     */
    private int maxLen;

    /**
     * The chromosome.
     */
    private Chromosome chromosome;

    /**
     * The viewer.
     */
    private GenomeViewer viewer;

    /**
     * The show view.
     */
    private boolean showView = false;

    /**
     * The view start.
     */
    private long viewStart = 0;

    /**
     * The view end.
     */
    private long viewEnd = 0;

    private Probe[] probes = null;

    private DataStore activeStore = null;

    private boolean showNegative = false;

    private double minValue;
    private double maxValue;

    // Values cached from the last update and used when
    // relating pixels to positions
    private int xOffset = 0;
    private int chrWidth = 0;

    // Stored values when dragging a selection
    private boolean isSelecting = false;
    private int selectionStart = 0;
    private int selectionEnd = 0;

    // Cached values used for drawing
    private int lastProbeXEnd = 0;
    private double lastProbeValue = 0;
    private int lastProbeXMid = 0;
    private int lastProbeY = 0;

    /**
     * Instantiates a new chromosome display.
     *
     * @param genome     the genome
     * @param chromosome the chromosome
     * @param viewer     the viewer
     */
    public ChromosomeDisplay(Genome genome, Chromosome chromosome,
                             GenomeViewer viewer) {
        maxLen = genome.getLongestChromosomeLength();
        this.chromosome = chromosome;
        this.viewer = viewer;
        PanelListener pl = new PanelListener();
        addMouseListener(pl);
        addMouseMotionListener(pl);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        xOffset = getWidth() / 80;
        if (xOffset > 10)
            xOffset = 10;
        if (xOffset < 1)
            xOffset = 1;

        int yOffset = getHeight() / 10;
        if (yOffset > 10)
            yOffset = 10;
        if (yOffset < 2)
            yOffset = 2;

        int width = getWidth() - (2 * xOffset);
        int height = getHeight() - (2 * yOffset);

        chrWidth = scaleX(width, chromosome.getLength(), maxLen);
        lastProbeXEnd = 0;
        lastProbeXMid = 0;
        lastProbeY = 0;
        lastProbeValue = -10000000;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // If we have a quantitated active store and some probes then we'll do a
        // full quantitative view

        if (activeStore != null && probes != null) {

            g.setColor(ColourScheme.DATA_BACKGROUND_ODD);
            g.fillRoundRect(xOffset, yOffset,
                    scaleX(width, chromosome.getLength(), maxLen), height, 2, 2);

            // Draw a box over the selected region if there is one
            if (showView) {
                g.setColor(Color.BLACK);

                // Limit how small the box can get so we can always see it
                int boxWidth = scaleX(width, viewEnd - viewStart, maxLen);
                if (boxWidth < 4) {
                    boxWidth = 4;
                }
                g.fillRoundRect(xOffset + scaleX(width, viewStart, maxLen), 0,
                        boxWidth, getHeight(), 2, 2);
            }

            // Draw as many probes as we have space for

            showNegative = DisplayPreferences.getInstance().getScaleType() == DisplayPreferences.SCALE_TYPE_POSITIVE_AND_NEGATIVE;

            Color fixedColour = null;

            if (DisplayPreferences.getInstance().getColourType() == DisplayPreferences.COLOUR_TYPE_INDEXED) {
                DataStore[] drawnStores = REDApplication.getInstance()
                        .drawnDataStores();
                for (int d = 0; d < drawnStores.length; d++) {
                    if (drawnStores[d] == activeStore) {
                        fixedColour = ColourIndexSet.getColour(d);
                        break;
                    }
                }

                if (fixedColour == null)
                    fixedColour = Color.DARK_GRAY;
            }

            maxValue = DisplayPreferences.getInstance().getMaxDataValue();
            if (showNegative) {
                minValue = 0 - maxValue;
            } else {
                minValue = 0;
            }

            // If we're showing negative values put a line in the middle to show
            // the origin
            if (showNegative) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(
                        xOffset,
                        getHeight() / 2,
                        xOffset + scaleX(width, chromosome.getLength(), maxLen),
                        getHeight() / 2);
            }

            // Now go through all the probes figuring out whether they
            // need to be displayed

            // Reset the values used to optimise drawing
            lastProbeXEnd = 0;
            lastProbeValue = 0;

            for (int i = 0; i < probes.length; i++) {
                drawProbe(probes[i], g, width, maxLen, yOffset, xOffset,
                        height, fixedColour);
            }

            if (showView) {
                g.setColor(ColourScheme.GENOME_SELECTED_CHROMOSOME);
            } else {
                g.setColor(ColourScheme.GENOME_CHROMOSOME);
            }
            g.drawRoundRect(xOffset, yOffset,
                    scaleX(width, chromosome.getLength(), maxLen), height, 2, 2);

            // Draw a box over the selected region if there is one
            if (showView) {
                g.setColor(Color.BLACK);

                // Limit how small the box can get so we can always see it
                int boxWidth = scaleX(width, viewEnd - viewStart, maxLen);
                if (boxWidth < 4) {
                    boxWidth = 4;
                }
                g.drawRoundRect(xOffset + scaleX(width, viewStart, maxLen), 0,
                        boxWidth, getHeight(), 2, 2);
            }
        } else {

            // There's no quantitation to draw so fall back to the old methods

            g.setColor(ColourScheme.GENOME_CHROMOSOME);
            g.fillRoundRect(xOffset, yOffset,
                    scaleX(width, chromosome.getLength(), maxLen), height, 2, 2);

            // Draw a box over the selected region if there is one
            if (showView) {
                g.setColor(ColourScheme.GENOME_SELECTED);

                // Limit how small the box can get so we can always see it
                int boxWidth = scaleX(width, viewEnd - viewStart, maxLen);
                if (boxWidth < 4) {
                    boxWidth = 4;
                }
                g.fillRoundRect(xOffset + scaleX(width, viewStart, maxLen), 1,
                        boxWidth, getHeight() - 2, 2, 2);
            }
        }

        // Finally draw a selection if there is one
        if (isSelecting) {
            g.setColor(ColourScheme.DRAGGED_SELECTION);
            g.fillRect(Math.min(selectionEnd, selectionStart), yOffset,
                    Math.abs(selectionEnd - selectionStart), height);
        }

    }

    private void drawProbe(Probe p, Graphics g, int chrWidth, int maxLength,
                           int yOffset, int xOffset, int effectiveHeight, Color color) {

        int wholeXStart = xOffset + scaleX(chrWidth, p.getStart(), maxLength);
        int wholeXEnd = xOffset + scaleX(chrWidth, p.getEnd(), maxLength);
        if ((wholeXEnd - wholeXStart) < 2) {
            wholeXEnd = wholeXStart + 2;
        }

        double value = 0;

        // Restrict the range we cover...
        if (value > maxValue)
            value = maxValue;
        if (value < minValue)
            value = minValue;

        // Don't draw probes which overlap exactly with the last one
        // and are of lower height
        if (wholeXEnd <= lastProbeXEnd + 2) {

            switch (DisplayPreferences.getInstance().getGraphType()) {
                case DisplayPreferences.GRAPH_TYPE_LINE:
                    return; // Never overlap

                default: // Overlap if the new value is more extreme
                    if (lastProbeValue > 0 && value > 0 && value <= lastProbeValue) {
                        return;
                    }

                    if (lastProbeValue < 0 && value < 0 && value >= lastProbeValue) {
                        return;
                    }

            }

        }

        lastProbeXEnd = wholeXEnd;
        lastProbeValue = value;

        if (color != null) {
            g.setColor(color);
        } else {
            g.setColor(DisplayPreferences.getInstance().getGradient()
                    .getColor(value, minValue, maxValue));
        }

        int yBoxStart;
        int yBoxEnd;

		/*
         * If we're drawing reads as well we can only take up half of the track.
		 * If it's just probes we can take the whole track.
		 * 
		 * We also need to consider if we're showing negative values. If we are
		 * then we draw from the middle of the track up or down
		 */

        int yValue;
        if (showNegative) {
            if (value > 0) {
                yBoxEnd = getHeight() / 2;
                yBoxStart = yOffset
                        + ((effectiveHeight / 2) - ((int) (((double) effectiveHeight / 2) * (value / maxValue))));
                yValue = yBoxStart;
            } else {
                yBoxStart = getHeight() / 2;
                yBoxEnd = (getHeight() - yOffset)
                        - ((int) (((double) effectiveHeight / 2) * ((minValue - value) / minValue)));
                yValue = yBoxEnd;
            }
        } else {
            yBoxStart = (getHeight() - yOffset)
                    - ((int) (((double) effectiveHeight) * (value / maxValue)));
            yValue = yBoxStart;
            yBoxEnd = effectiveHeight + yOffset;
        }

        switch (DisplayPreferences.getInstance().getGraphType()) {

            case DisplayPreferences.GRAPH_TYPE_BAR:

                g.fillRect(wholeXStart, yBoxStart, (wholeXEnd - wholeXStart),
                        yBoxEnd - yBoxStart);
                break;

            case DisplayPreferences.GRAPH_TYPE_POINT:
                g.fillOval(wholeXStart + ((wholeXEnd - wholeXStart) / 2), yValue,
                        2, 2);
                break;

            case DisplayPreferences.GRAPH_TYPE_LINE:
                int xMid = wholeXStart + ((wholeXEnd - wholeXStart) / 2);
                if (xMid <= lastProbeXMid)
                    return; // Can happen with probes of different length
                if (lastProbeXMid > 0) {
                    g.drawLine(lastProbeXMid, lastProbeY, wholeXStart
                            + ((wholeXEnd - wholeXStart) / 2), yValue);
                }
                lastProbeXMid = wholeXStart + ((wholeXEnd - wholeXStart) / 2);
                lastProbeY = yValue;
                break;

        }
        // System.out.println("Drawing probe from x="+wholeXStart+" y="+yBoxStart+" width="+(wholeXEnd-wholeXStart)+" height="+(yBoxEnd-yBoxStart));

    }

    /**
     * Scale x.
     *
     * @param width   the width
     * @param measure the measure
     * @param max     the max
     * @return the int
     */
    private int scaleX(int width, double measure, double max) {
        return (int) (width * (measure / max));
    }

    /**
     * Sets the view.
     *
     * @param c     the c
     * @param start the start
     * @param end   the end
     */
    protected void setView(Chromosome c, long start, long end) {
        if (c.equals(chromosome)) {
            showView = true;
            viewStart = start;
            viewEnd = end;
            repaint();
        } else {
            if (showView) {
                showView = false;
                repaint();
            }
        }
    }

    public void dataGroupAdded(DataGroup g) {
    }

    public void dataGroupsRemoved(DataGroup[] g) {
    }

    public void dataGroupRenamed(DataGroup g) {
    }

    public void dataGroupSamplesChanged(DataGroup g) {
    }

    public void dataSetAdded(DataSet d) {
    }

    public void dataSetsRemoved(DataSet[] d) {
    }

    public void dataSetRenamed(DataSet d) {
    }

    public void probeSetReplaced(ProbeSet p) {
        if (p == null) {
            probes = null;
        } else {
            probes = p.getProbesForChromosome(chromosome.getName());
            Arrays.sort(probes);
        }
    }

    public void activeDataStoreChanged(DataStore s) {
        activeStore = s;
        repaint();
    }

    public void activeProbeListChanged(ProbeList l) {

        if (l == null) {
            probes = null;
        } else {
            probes = l.getProbesForChromosome(chromosome.getName());
            Arrays.sort(probes);
        }

        repaint();

    }

    /**
     * The listener interface for receiving panel events. The class that is
     * interested in processing a panel event implements this interface, and the
     * object created with that class is registered with a component using the
     * component's <code>addPanelListener<code> method. When
     * the panel event occurs, that object's appropriate
     * method is invoked.
     */
    private class PanelListener implements MouseListener, MouseMotionListener {

        /*
         * (non-Javadoc)
         *
         * @see
         * java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(MouseEvent me) {
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent me) {
            selectionStart = me.getX();
            selectionEnd = me.getX();

            try {
                getBasePosition(me.getX());
                isSelecting = true;
            } catch (REDException e) {
                // They pressed outside of the chromosome so ignore it.
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent me) {

            if (!isSelecting)
                return;

            isSelecting = false;

            try {
                // If it's a really small selection (ie a click with no drag)
                // give them a small chunk around this point
                if (selectionEnd == selectionStart) {

                    selectionStart = Math.max(selectionStart - 3, xOffset);
                    selectionEnd = Math.min(selectionEnd + 3, xOffset
                            + chrWidth);
                }

                int start = getBasePosition(Math.min(selectionEnd,
                        selectionStart));
                int end = getBasePosition(Math
                        .max(selectionEnd, selectionStart));

                DisplayPreferences.getInstance().setLocation(chromosome, start,
                        end);
            } catch (REDException e) {
                // This should have been caught before now.
                new CrashReporter(e);
            }

            repaint();
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(MouseEvent arg0) {
            viewer.setInfo(chromosome);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(MouseEvent arg0) {
        }

        public void mouseDragged(MouseEvent me) {
            try {
                getBasePosition(me.getX());
                selectionEnd = me.getX();
                repaint();
            } catch (REDException e) {
                // This was outside the chromosome so ignore it
            }

        }

        public void mouseMoved(MouseEvent arg0) {
        }

        public int getBasePosition(int pixelPosition) throws REDException {

            if (pixelPosition < xOffset) {
                throw new REDException("Before the start of the chromosome");
            }
            if (pixelPosition > (xOffset + chrWidth)) {
                throw new REDException("After the end of the chromosome");
            }
            return (int) (chromosome.getLength() * (((double) (pixelPosition - xOffset)) / chrWidth));

        }
    }

}
