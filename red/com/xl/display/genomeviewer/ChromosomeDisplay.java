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
import com.xl.interfaces.DataChangeListener;
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
    private int viewStart = 0;

    /**
     * The view end.
     */
    private int viewEnd = 0;

    private Probe[] probes = null;

    private DataStore activeStore = null;

    // Values cached from the last update and used when
    // relating pixels to positions
    private int xOffset = 0;
    private int chrWidth = 0;

    // Stored values when dragging a selection
    private boolean isSelecting = false;
    private int selectionStart = 0;
    private int selectionEnd = 0;


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

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (activeStore != null && probes != null) {

            g.setColor(ColourScheme.DATA_BACKGROUND_ODD);
            g.fillRoundRect(xOffset, yOffset, scaleX(width, chromosome.getLength(), maxLen), height, 2, 2);

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

            // Now go through all the probes figuring out whether they need to be displayed
            for (Probe probe : probes) {
                drawProbe(probe, g, width, maxLen, yOffset, xOffset, height);
            }

            if (showView) {
                g.setColor(ColourScheme.GENOME_SELECTED_CHROMOSOME);
            } else {
                g.setColor(ColourScheme.GENOME_CHROMOSOME);
            }
            g.drawRoundRect(xOffset, yOffset,
                    scaleX(width, chromosome.getLength(), maxLen), height, 2, 2);

            // Draw a box over the selected region if there is one
//            if (showView) {
//                g.setColor(Color.BLACK);
//
//                // Limit how small the box can get so we can always see it
//                int boxWidth = scaleX(width, viewEnd - viewStart, maxLen);
//                if (boxWidth < 4) {
//                    boxWidth = 4;
//                }
//                g.drawRoundRect(xOffset + scaleX(width, viewStart, maxLen), 0,
//                        boxWidth, getHeight(), 2, 2);
//            }
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
                           int yOffset, int xOffset, int effectiveHeight) {

        int wholeXStart = xOffset + scaleX(chrWidth, p.getStart(), maxLength);
        int wholeXEnd = wholeXStart + 1;
        g.setColor(ColourScheme.getBaseColor(p.getAltBase()));

        int yBoxStart;
        int yBoxEnd;

		/*
         * If we're drawing reads as well we can only take up half of the track.
		 * If it's just probes we can take the whole track.
		 * 
		 * We also need to consider if we're showing negative values. If we are
		 * then we draw from the middle of the track up or down
		 */


        yBoxStart = (getHeight() - yOffset)
                - ((int) (((double) effectiveHeight) * (p.getStart() / chrWidth)));
        yBoxEnd = effectiveHeight + yOffset;

        g.fillRect(wholeXStart, yBoxStart, (wholeXEnd - wholeXStart), yBoxEnd - yBoxStart);

//        System.out.println("Drawing probe from x=" + wholeXStart + " y=" + yBoxStart + " width=" + (wholeXEnd - wholeXStart) + " height=" + (yBoxEnd - yBoxStart));

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
    protected void setView(Chromosome c, int start, int end) {
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
