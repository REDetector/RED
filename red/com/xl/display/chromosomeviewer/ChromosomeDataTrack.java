package com.xl.display.chromosomeviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteSet;
import com.xl.interfaces.DataChangeListener;
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
 * The Class ChromosomeDataTrack represents a single track in the chromosome
 * view containing the data from a single data store. Depending on the display
 * preferences it can show either just raw data, or quantitated data, or both.
 */
public class ChromosomeDataTrack extends AbstractTrack implements DataChangeListener {
    private static int TEN_MB = 10000000;
    private Chromosome chromosome = null;
    /**
     * The data.
     */
    private DataStore data = null;
    /**
     * The reads.
     */
    private List<? extends Location> reads = null;

    private List<Site> depths = null;

    /**
     * The sites
     */
    private Site[] sites;
    /**
     * Whether the tip is displayed.
     */
    private boolean timing = true;
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

    private int maxCoverage = 0;

    private boolean drawSites = true;
    private boolean drawReads = true;

    private boolean isStandardChromosomeName;

    private int enlargeStart = 0;
    private int enlargeEnd = 0;

    /**
     * Instantiates a new chromosome data track.
     *
     * @param viewer the viewer
     * @param data   the data
     */
    public ChromosomeDataTrack(ChromosomeViewer viewer, DataCollection collection, DataStore data) {
        super(viewer, collection, data.name());
        this.data = data;
        isStandardChromosomeName = data.isStandardChromosomeName();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int viewerLength = getViewerLength();
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

    private void drawRead(Graphics g, Location r, int pixelYStart) {
        g.setFont(FontManager.defaultFont);
        int start = r.getStart();
        int end = r.getEnd();
        drawnReads.add(new DrawnRead(start, end, pixelYStart, pixelYStart + readHeight, r));
        if (r instanceof SequenceRead) {
            SequenceRead sequenceRead = (SequenceRead) r;
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

    private void drawSite(Graphics g, Site b) {
        g.setColor(ColourScheme.getBaseColor(b.getAltBase()));
        int position = b.getStart();
        int baseWidth = g.getFontMetrics().stringWidth(String.valueOf(b.getAltBase()));
        int left = bpToPixel(position) - basePixel / 2 + baseWidth / 2;
        int right = bpToPixel(position) + basePixel / 2 + baseWidth / 2;
        g.drawLine(left, 0, left, displayHeight);
        g.drawLine(right, 0, right, displayHeight);
    }

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

    private void updateReads() {
        if (chromosome == null) {
            return;
        }
        String currentChromosome = chromosome.getName();
        if (!isStandardChromosomeName) {
            currentChromosome = ChromosomeUtils.getAliasChromosomeName(currentChromosome);
        }
        reads = data.getDataParser().query(currentChromosome, enlargeStart, enlargeEnd);
        readsYIndex = new int[reads.size()];
        Arrays.fill(readsYIndex, -1);
        maxCoverage = processSequence(reads, readsYIndex);
        setPreferredSize(new Dimension(displayWidth, maxCoverage * readHeight));
        repaint();
    }

    private void updateBlocks() {
        if (chromosome == null) {
            return;
        }
        String currentChromosome = chromosome.getName();
        if (!isStandardChromosomeName) {
            currentChromosome = ChromosomeUtils.getAliasChromosomeName(currentChromosome);
        }
        if (depths != null) {
            depths.clear();
            depths = null;
            System.gc();
        }
        depths = ((BAMFileParser) data.getDataParser()).getDepth(currentChromosome, currentViewerStart, currentViewerEnd);
        for (Site site : depths) {
            if (maxCoverage < site.getDepth()) {
                maxCoverage = site.getDepth();
            }
        }
        setPreferredSize(new Dimension(displayWidth, maxCoverage * readHeight));
        repaint();
    }

    /**
     * This call can be made whenever the reads need to be updated - particularly when the viewer is being switched to show a different chromosome.
     */
    @Override
    protected void updateTrack(Chromosome chromosome) {
        this.chromosome = chromosome;
        String currentChromosome = chromosome.getName();
        if (dataCollection.siteSet() != null) {
            sites = dataCollection.siteSet().getActiveList().getSitesForChromosome(currentChromosome);
        } else {
            sites = new Site[0];
        }
        Arrays.sort(sites);
        if (getViewerLength() > TEN_MB) {
            repaint();
            return;
        }
        if (getViewerLength() > displayWidth * 2) {
            updateBlocks();
        } else {
            updateReads();
        }
    }

    /**
     * Find read.
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

    private int processSequence(List<? extends Location> reads, int[] readsYIndex) {
        if (reads == null || reads.size() == 0) {
            return 0;
        }
        List<Integer> lastYIndexes = new ArrayList<Integer>();
        lastYIndexes.add(reads.get(0).getEnd());
        int readsLength = reads.size();
        readsYIndex[0] = 0;
        for (int i = 1; i < readsLength; i++) {
            int j = 0;
            int yIndexLength = lastYIndexes.size();
            int currentReadStart = reads.get(i).getStart();
            for (; j < yIndexLength; j++) {
                if (currentReadStart - lastYIndexes.get(j) > 0) {
                    readsYIndex[i] = j;
                    lastYIndexes.set(j, reads.get(i).getEnd());
                    j = yIndexLength;
                }
            }
            if (readsYIndex[i] == -1) {
                readsYIndex[i] = j + 1;
                lastYIndexes.add(reads.get(i).getEnd());
            }
        }
        return lastYIndexes.size();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(0, maxCoverage * readHeight);
    }

    @Override
    public void dataSetAdded(DataSet d) {

    }

    @Override
    public void dataSetsRemoved(DataSet[] d) {

    }

    @Override
    public void dataGroupAdded(DataGroup g) {

    }

    @Override
    public void dataGroupsRemoved(DataGroup[] g) {

    }

    @Override
    public void dataSetRenamed(DataSet d) {

    }

    @Override
    public void dataGroupRenamed(DataGroup g) {

    }

    @Override
    public void dataGroupSamplesChanged(DataGroup g) {

    }

    @Override
    public void siteSetReplaced(SiteSet p) {
        if (p == null) {
            sites = null;
        } else {
            sites = p.getSitesForChromosome(DisplayPreferences.getInstance().getCurrentChromosome().getName());
            Arrays.sort(sites);
        }
    }

    @Override
    public void activeDataStoreChanged(DataStore s) {

    }

    @Override
    public void activeSiteListChanged(SiteList l) {
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
         * In many cases we don't need to search through reads and sites, so we
		 * can quickly work out what we should be looking for from what we're
		 * drawing and where the mouse is.
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
