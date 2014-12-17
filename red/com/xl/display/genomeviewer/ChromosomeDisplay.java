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

package com.xl.display.genomeviewer;

import com.xl.datatypes.DataStore;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.REDException;
import com.xl.interfaces.ActiveDataChangedListener;
import com.xl.net.crashreport.CrashReporter;
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
public class ChromosomeDisplay extends JPanel implements ActiveDataChangedListener, MouseListener, MouseMotionListener {
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
    /**
     * The RNA editing sites.
     */
    private Site[] sites = null;
    /**
     * A flag to show RNA editing sites or not.
     */
    private boolean showSites;
    // Values cached from the last update and used when relating pixels to positions
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
    public ChromosomeDisplay(Genome genome, Chromosome chromosome, GenomeViewer viewer) {
        maxLen = genome.getLongestChromosomeLength();
        this.chromosome = chromosome;
        this.viewer = viewer;
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Set showing the sites or not.
     *
     * @param showSites show sites or not.
     */
    public void showSites(boolean showSites) {
        this.showSites = showSites;
    }

    @Override
    public void paintComponent(Graphics g) {
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

        if (showSites && sites != null) {

            g.setColor(ColourScheme.DATA_BACKGROUND_ODD);
            g.fillRoundRect(xOffset, yOffset, scaleX(width, chromosome.getLength(), maxLen), height, 2, 2);

            // Now go through all the sites figuring out whether they need to be displayed
            for (Site site : sites) {
                drawSite(site, g, width, maxLen, yOffset, xOffset, height);
            }

            // Draw a box over the selected region if there is one
            if (showView) {
                g.setColor(Color.BLACK);

                // Limit how small the box can get so we can always see it
                int boxWidth = scaleX(width, viewEnd - viewStart, maxLen);
                if (boxWidth < 2) {
                    boxWidth = 2;
                }
                g.fillRoundRect(xOffset + scaleX(width, viewStart, maxLen), 0, boxWidth, getHeight(), 2, 2);
            }

            if (showView) {
                g.setColor(ColourScheme.GENOME_SELECTED_CHROMOSOME);
            } else {
                g.setColor(ColourScheme.GENOME_CHROMOSOME);
            }
            g.drawRoundRect(xOffset, yOffset, scaleX(width, chromosome.getLength(), maxLen), height, 2, 2);

        } else {

            g.setColor(ColourScheme.GENOME_CHROMOSOME);
            g.fillRoundRect(xOffset, yOffset, scaleX(width, chromosome.getLength(), maxLen), height, 2, 2);

            // Draw a box over the selected region if there is one
            if (showView) {
                g.setColor(ColourScheme.GENOME_SELECTED);

                // Limit how small the box can get so we can always see it
                int boxWidth = scaleX(width, viewEnd - viewStart, maxLen);
                if (boxWidth < 1) {
                    boxWidth = 1;
                }
                g.fillRoundRect(xOffset + scaleX(width, viewStart, maxLen), 1, boxWidth, getHeight() - 2, 2, 2);
            }
        }

        // Finally draw a selection if there is one
        if (isSelecting) {
            g.setColor(ColourScheme.DRAGGED_SELECTION);
            g.fillRect(Math.min(selectionEnd, selectionStart), yOffset, Math.abs(selectionEnd - selectionStart), height);
        }

    }

    /**
     * Draw a site in the genome view. It is a very thin box like a colored line that the color is the same as the color for alternative base.
     *
     * @param site            the editing site
     * @param g               the g
     * @param chrWidth        the chromosome width
     * @param maxLength       the max length of chromosome
     * @param yOffset         the offset of y position
     * @param xOffset         the offset of x position
     * @param effectiveHeight the height
     */
    private void drawSite(Site site, Graphics g, int chrWidth, int maxLength, int yOffset, int xOffset, int effectiveHeight) {
        int wholeXStart = xOffset + scaleX(chrWidth, site.getStart(), maxLength);
        int wholeXEnd = wholeXStart + 1;
        g.setColor(ColourScheme.getBaseColor(site.getAltBase()));
        g.fillRect(wholeXStart, yOffset, (wholeXEnd - wholeXStart), effectiveHeight);
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

    private int getBasePosition(int pixelPosition) throws REDException {
        if (pixelPosition < xOffset) {
            throw new REDException("Before the start of the chromosome");
        }
        if (pixelPosition > (xOffset + chrWidth)) {
            throw new REDException("After the end of the chromosome");
        }
        return (int) (chromosome.getLength() * (((double) (pixelPosition - xOffset)) / chrWidth));
    }

    /**
     * Sets the view.
     *
     * @param c     the c
     * @param start the start
     * @param end   the end
     */
    protected void setView(Chromosome c, int start, int end) {
        if (c == null) {
            showView = false;
            repaint();
        } else if (c.equals(chromosome)) {
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

    @Override
    public void activeDataChanged(DataStore dataStore, SiteList siteList) {
        if (siteList == null) {
            sites = null;
        } else {
            sites = siteList.getSitesForChromosome(chromosome.getName());
            Arrays.sort(sites);
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
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

    @Override
    public void mouseReleased(MouseEvent me) {

        if (!isSelecting)
            return;

        isSelecting = false;

        try {
            // If it's a really small selection (ie a click with no drag) give them a small chunk around this point
            if (selectionEnd == selectionStart) {

                selectionStart = Math.max(selectionStart - 3, xOffset);
                selectionEnd = Math.min(selectionEnd + 3, xOffset + chrWidth);
            }

            int start = getBasePosition(Math.min(selectionEnd, selectionStart));
            int end = getBasePosition(Math.max(selectionEnd, selectionStart));

            DisplayPreferences.getInstance().setLocation(chromosome, start, end);
        } catch (REDException e) {
            // This should have been caught before now.
            new CrashReporter(e);
        }
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        viewer.setInfo(chromosome);
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent me) {
        try {
            getBasePosition(me.getX());
            selectionEnd = me.getX();
            repaint();
        } catch (REDException e) {
            // This was outside the chromosome so ignore it
        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

}
