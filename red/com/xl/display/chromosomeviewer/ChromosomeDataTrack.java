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

import com.xl.datatypes.DataStore;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.interfaces.ActiveDataChangedListener;
import com.xl.parsers.dataparsers.BAMFileParser;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.AsciiUtils;
import com.xl.utils.ChromosomeUtils;
import com.xl.utils.ColourScheme;
import com.xl.utils.FontManager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * The Class ChromosomeDataTrack represents a single track in the chromosome view containing the data from a single data store. Depending on the display
 * preferences it can show either just mapped reads data, or RNA editing sites data, or both.
 */
public class ChromosomeDataTrack extends AbstractTrack implements ActiveDataChangedListener {
    /**
     * If the viewer length is longer than ten million, then we show nothing but a notification to tell user zoom in.
     */
    private static int TEN_MB = 10000000;
    /**
     * The chromosome.
     */
    private Chromosome chromosome = null;
    /**
     * The data.
     */
    private DataStore data = null;
    /**
     * The reads.
     */
    private List<? extends Location> reads = null;
    /**
     * The sites
     */
    private Site[] sites;
    /**
     * The drawn reads.
     */
    private Vector<DrawnRead> drawnReads = new Vector<DrawnRead>();
    /**
     * The active read.
     */
    private Location activeRead = null;
    /**
     * The active read index.
     */
    private int activeReadIndex;
    /**
     * The height of each read
     */
    private int readHeight = 10;
    /**
     * Each read's height index.
     */
    private int[] readsYIndex = null;
    /**
     * Coverages of depth for the RNA editing sites which had been shown on the display screen.
     */
    private List<Site> depths = null;
    /**
     * The max of the coverage of depth.
     */
    private int maxDepth = 0;
    /**
     * Whether the tip is displayed.
     */
    private boolean timing = true;
    /**
     * Show sites or not, depending on the display preference.
     */
    private boolean drawSites = true;
    /**
     * Show reads or not, depending on the display preference.
     */
    private boolean drawReads = true;
    /**
     * A mechanism to reduce loading data frequently from the BAM file.
     * <p/>
     * Each time loading data, enlargeStart = currentViewerStart - viewerLength / 2
     * <p/>
     * If the currentViewerStart < enlargeStart, then load new data from BAM file again.
     */
    private int enlargeStart = 0;
    /**
     * A mechanism to reduce loading data frequently from the BAM file.
     * <p/>
     * Each time loading data, enlargeEnd = currentViewerEnd + viewerLength / 2
     * <p/>
     * If the enlargeEnd < currentViewerEnd, then load new data from BAM file again.
     */
    private int enlargeEnd = 0;

    /**
     * Instantiates a new chromosome data track.
     *
     * @param viewer The chromosome viewer which holds this track.
     * @param data   The data which represents this data track.
     */
    public ChromosomeDataTrack(ChromosomeViewer viewer, DataStore data) {
        super(viewer, data.name());
        this.data = data;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int viewerLength = getViewerLength();

        // If viewer length is longer than ten million, then show a prompt to zoom in.
        if (viewerLength > TEN_MB) {
            g.setColor(ColourScheme.DATA_TRACK);
            String prompt = "Zoom in to make the mapped reads visible.";
            int baseWidth = g.getFontMetrics().stringWidth(prompt);
            g.drawString(prompt, (getWidth() - baseWidth) / 2, readHeight);
            reads = null;
            depths = null;
            enlargeStart = enlargeEnd = 0;
            return;
        }
        // Display mode, show reads, show sites, show reads and sites.
        switch (DisplayPreferences.getInstance().getDisplayMode()) {
            case DisplayPreferences.DISPLAY_MODE_PROBES_ONLY:
                drawReads = false;
                break;
            case DisplayPreferences.DISPLAY_MODE_READS_ONLY:
                drawSites = false;
                break;
            default:
                drawReads = true;
                drawSites = true;
        }
        drawnReads.removeAllElements();

        // Update enlarge start and end if needed.
        if (currentViewerStart < enlargeStart || currentViewerEnd > enlargeEnd) {
            if (viewerLength > displayWidth * 2) {
                updateBlocks();
            }
            enlargeStart = currentViewerStart - viewerLength / 2;
            enlargeEnd = currentViewerEnd + viewerLength / 2;
            if (enlargeStart < 0) {
                enlargeStart = 0;
            }
            if (enlargeEnd > chromosome.getLength()) {
                enlargeEnd = chromosome.getLength();
            }
        }

        if (drawReads) {
            // If viewer length is longer than twice display width, we display the reads in block status
            if (viewerLength > displayWidth * 2) {
                drawBlocks(g);
                reads = null;
            } else {
                drawReads(g);
            }
        }
        if (drawSites) {
            drawSites(g);
        }

        // Draw a line across the bottom of the display
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, displayHeight - 1, displayWidth, displayHeight - 1);

        // If we're the active data store then surround us in red. This can fail if the viewer is being destroyed (viewer returns null) so catch this
        try {
            if (chromosomeViewer.application().dataCollection().getActiveDataStore() == data) {
                g.setColor(Color.RED);
                g.drawLine(0, displayHeight - 2, displayWidth, displayHeight - 2);
                g.drawLine(0, displayHeight - 1, displayWidth, displayHeight - 1);
                g.drawLine(0, 0, displayWidth, 0);
                g.drawLine(0, 1, displayWidth, 1);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

    }

    /**
     * Draw the reads from a given location.
     *
     * @param g the g
     */
    private void drawReads(Graphics g) {
        if (reads == null) {
            updateReads();
        }
        for (int i = 0, readsLength = reads.size(); i < readsLength; i++) {
            Location location = reads.get(i);
            if (location.getEnd() >= currentViewerStart && location.getStart() <= currentViewerEnd) {
                drawRead(g, location, readsYIndex[i] * readHeight);
            }
        }
        //Always draw the active read last
        if (activeRead != null) {
            g.setColor(ColourScheme.ACTIVE_READ);
            drawRect(g, activeRead.getStart(), activeRead.getEnd() + 1, activeReadIndex, readHeight);
            g.setColor(ColourScheme.DATA_TRACK);
        }
    }

    /**
     * Draw a single read obtained from a BAM file.
     *
     * @param g           the g
     * @param read        the read
     * @param pixelYStart the y position refer to the screen pixel
     */
    private void drawRead(Graphics g, Location read, int pixelYStart) {
        g.setFont(FontManager.DEFAULT_FONT);
        int start = read.getStart();
        int end = read.getEnd();
        // Store this read to make it convenient find the active read.
        drawnReads.add(new DrawnRead(start, end, pixelYStart, pixelYStart + readHeight, read));
        if (read instanceof SequenceRead) {
            SequenceRead sequenceRead = (SequenceRead) read;
            // Get the small piece sequences which depends on the CIGAR string of this read.
            List<SequenceRead.SmallPieceSequence> smallPieceSequences = sequenceRead.getAlignmentBlocks();
            for (int i = 0, len = smallPieceSequences.size(); i < len; i++) {
                SequenceRead.SmallPieceSequence smallPieceSequence = smallPieceSequences.get(i);
                start = smallPieceSequence.getReferenceStart();
                end = smallPieceSequence.getEnd();
                g.setColor(ColourScheme.DATA_TRACK);
                drawRoundRect(g, start, end, pixelYStart, readHeight);
                char[] cChar = AsciiUtils.getChars(smallPieceSequence.getBases());
                int k = 0;
                for (; k < cChar.length; k++) {
                    char c = cChar[k];
                    if (getViewerLength() > displayWidth / 4) {
                        g.setColor(ColourScheme.getBaseColor(c));
                        fillRect(g, start + k, start + k + 1, pixelYStart, readHeight);
                    } else {
                        drawBase(g, c, start + k, pixelYStart + readHeight);
                    }
                }
                // A line for a splice junction between two small piece sequences.
                if (i < len - 1) {
                    g.setColor(ColourScheme.READ_INTEVAL);
                    drawLine(g, start + k, smallPieceSequences.get(i + 1).getReferenceStart(), pixelYStart + readHeight / 2, pixelYStart + readHeight / 2);
                }

            }
        } else {
            g.setColor(ColourScheme.DATA_TRACK);
            fillRect(g, start, end + 1, pixelYStart, readHeight);
        }
    }

    /**
     * Draw the RNA editing sites.
     *
     * @param g the g
     */
    private void drawSites(Graphics g) {
        if (sites == null) {
            return;
        }
        for (Site site : sites) {
            if (site.getStart() > currentViewerStart && site.getStart() < currentViewerEnd) {
                drawSite(g, site);
            }
        }
    }

    /**
     * Draw a site in a box using two line, whose color is the same as alternative base color.
     *
     * @param g    the g
     * @param site the site
     */
    private void drawSite(Graphics g, Site site) {
        g.setColor(ColourScheme.getBaseColor(site.getAltBase()));
        int position = site.getStart();
        int baseWidth = g.getFontMetrics().stringWidth(String.valueOf(site.getAltBase()));
        int left = bpToPixel(position) - basePixel / 2 + baseWidth / 2;
        int right = bpToPixel(position) + basePixel / 2 + baseWidth / 2;
        g.drawLine(left, 0, left, displayHeight);
        g.drawLine(right, 0, right, displayHeight);
    }

    /**
     * If the viewer is shorter than ten million and longer than twice screen pixel, we have a comfortable view with the block status to tell user about the
     * distribution of mapped reads.
     *
     * @param g the g
     */
    private void drawBlocks(Graphics g) {
        if (depths == null) {
            return;
        }
        g.setColor(ColourScheme.DATA_TRACK);
        for (Site depth : depths) {
            int position = depth.getStart();
            if (position >= currentViewerStart && position <= currentViewerEnd) {
                drawLine(g, position, position, 0, depth.getDepth() * readHeight);
            }
        }
    }

    /**
     * Update the read list by loading data from BAM file when conditions (currentViewerStart < enlargeStart || currentViewerEnd > enlargeEnd) can not be
     * matched. Before query for new data, we need to know whether the chromosome name is standard (i.e., chr1, ch1, 1).
     */
    private void updateReads() {
        if (chromosome == null) {
            return;
        }
        String currentChromosome = chromosome.getName();
        if (!data.isStandardChromosomeName()) {
            currentChromosome = ChromosomeUtils.getAliasChromosomeName(currentChromosome);
        }
        if (reads != null) {
            reads.clear();
            reads = null;
            System.gc();
        }
        reads = data.getDataParser().query(currentChromosome, enlargeStart, enlargeEnd);
        readsYIndex = new int[reads.size()];
        Arrays.fill(readsYIndex, -1);
        // Every time data has been loaded to read list, we need to calculate the display position for each read, especially the height index.
        maxDepth = processSequence(reads, readsYIndex);
        // When the max coverage of depth has been calculated, we have to set the preferred size in order to adjusting the scroll bar to its best position.
        setPreferredSize(new Dimension(displayWidth, maxDepth * readHeight));
        repaint();
    }

    /**
     * Update the block status
     */
    private void updateBlocks() {
        if (chromosome == null) {
            return;
        }
        String currentChromosome = chromosome.getName();
        if (!data.isStandardChromosomeName()) {
            currentChromosome = ChromosomeUtils.getAliasChromosomeName(currentChromosome);
        }
        if (depths != null) {
            depths.clear();
            depths = null;
            System.gc();
        }
        depths = ((BAMFileParser) data.getDataParser()).getDepth(currentChromosome, currentViewerStart, currentViewerEnd);
        for (Site site : depths) {
            if (maxDepth < site.getDepth()) {
                maxDepth = site.getDepth();
            }
        }
        setPreferredSize(new Dimension(displayWidth, maxDepth * readHeight));
        repaint();
    }

    /**
     * This call can be made whenever the reads need to be updated - particularly when the viewer is being switched to show a different chromosome.
     */
    @Override
    protected void updateTrack(Chromosome chromosome) {
        this.chromosome = chromosome;
        String currentChromosome = chromosome.getName();
        if (data.siteSet() != null) {
            sites = data.siteSet().getActiveList().getSitesForChromosome(currentChromosome);
        } else {
            sites = new Site[0];
        }
        Arrays.sort(sites);
        if (getViewerLength() > TEN_MB) {
            repaint();
        } else if (getViewerLength() > displayWidth * 2) {
            updateBlocks();
        } else {
            updateReads();
        }
    }

    /**
     * Find read from the drawn reads vector.
     *
     * @param x the x
     * @param y the y
     */
    private void findRead(int x, int y) {
        Enumeration<DrawnRead> e = drawnReads.elements();
        while (e.hasMoreElements()) {
            DrawnRead r = e.nextElement();
            if (r.isInFeature(x, y)) {
                if (activeRead != r.read || r.bottom != activeReadIndex) {
                    chromosomeViewer.application().setStatusText(" " + data.name() + ":" + r.read.getStart() + "-" + r.read.getEnd());
                    activeRead = r.read;
                    activeReadIndex = r.bottom;
                    repaint();
                }
                return;
            }
        }
    }

    /**
     * An efficient way to calculate the y index for each read so we can show the reads without any overlap in the chromosome view. We judge whether the new
     * read's start position is overlapped with all of the indexes of last read's end positions (stored in a list named lastYIndexes), if it is overlapped with
     * all, then add this read's end position to the index list.
     *
     * @param reads       The reads
     * @param readsYIndex The calculated y index for each read.
     * @return the max coverage of depth calculated by ourselves.
     */
    private int processSequence(List<? extends Location> reads, int[] readsYIndex) {
        if (reads == null || reads.size() == 0) {
            return 0;
        }
        // A list to hold all last y indexes.
        List<Integer> lastYIndexes = new ArrayList<Integer>();
        lastYIndexes.add(reads.get(0).getEnd());
        int readsLength = reads.size();
        readsYIndex[0] = 0;
        for (int i = 1; i < readsLength; i++) {
            int j = 0;
            Location currentRead = reads.get(i);
            int yIndexLength = lastYIndexes.size();
            int currentReadStart = currentRead.getStart();
            for (; j < yIndexLength; j++) {
                // Judge if it is overlapped, if not, then update the last y index of this height to the new read's end position.
                if (currentReadStart - lastYIndexes.get(j) > 0) {
                    // Set y index for this read.
                    readsYIndex[i] = j;
                    // Update the last y index for the list.
                    lastYIndexes.set(j, currentRead.getEnd());
                    // Stop the for circulation.
                    j = yIndexLength;
                }
            }
            // If the new read is overlapped with all of the last y indexes, then add the new last y index to the list.
            if (readsYIndex[i] == -1) {
                readsYIndex[i] = j + 1;
                lastYIndexes.add(currentRead.getEnd());
            }
        }
        return lastYIndexes.size();
    }

    /**
     * We have to override this method to make the scroll pane visible.
     *
     * @return the preferred size.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(0, maxDepth * readHeight);
    }

    @Override
    public void activeDataChanged(DataStore d, SiteList l) {
        if (data != d) {
            return;
        }
        if (l == null) {
            sites = null;
        } else {
            sites = l.getSitesForChromosome(DisplayPreferences.getInstance().getCurrentChromosome().getName());
            Arrays.sort(sites);
        }
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        activeRead = null;
        timing = true;
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        /*
         * In many cases we don't need to search through reads and sites, so we can quickly work out what we should be looking for from what we're drawing
         * and where the mouse is.
		 */
        findRead(pixelToBp(me.getX()), me.getY());
        long lastTime = System.currentTimeMillis();
        while (timing) {
            if (System.currentTimeMillis() - lastTime > 1000) {
                setToolTipText("Single left click to zoom in and single right click to zoom out.");
                timing = false;
            }
        }
    }

    /**
     * The Class DrawnRead.
     */
    private class DrawnRead {

        /**
         * The left.
         */
        public int left;
        /**
         * The right.
         */
        public int right;
        /**
         * The top.
         */
        public int top;
        /**
         * The bottom.
         */
        public int bottom;
        /**
         * The read.
         */
        public Location read;

        /**
         * Instantiates a new drawn read.
         *
         * @param left   the left
         * @param right  the right
         * @param bottom the bottom
         * @param top    the top
         * @param read   the read
         */
        public DrawnRead(int left, int right, int bottom, int top, Location read) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
            this.read = read;
        }

        /**
         * Checks if is in feature.
         *
         * @param x the x
         * @param y the y
         * @return true, if is in feature
         */
        public boolean isInFeature(int x, int y) {
            return x >= left && x <= right && y >= bottom && y <= top;
        }
    }

}
