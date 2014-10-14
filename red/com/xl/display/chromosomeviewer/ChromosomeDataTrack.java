package com.xl.display.chromosomeviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.interfaces.DataChangeListener;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.AsciiUtils;
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

    /**
     * The data.
     */
    private DataStore data = null;
    /**
     * The reads.
     */
    private SequenceRead[] reads = null;
    /**
     * The probes
     */
    private Probe[] probes;
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
    private SequenceRead activeRead = null;
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

    private boolean drawProbes = true;
    private boolean drawReads = true;

    /**
     * Instantiates a new chromosome data track.
     *
     * @param viewer the viewer
     * @param data   the data
     */
    public ChromosomeDataTrack(ChromosomeViewer viewer, DataCollection collection, DataStore data) {
        super(viewer, collection, data.name());
        this.data = data;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        switch (DisplayPreferences.getInstance().getDisplayMode()) {
            case DisplayPreferences.DISPLAY_MODE_PROBES_ONLY:
                drawReads = false;
                break;
            case DisplayPreferences.DISPLAY_MODE_READS_ONLY:
                drawProbes = false;
                break;
            default:
                drawReads = true;
                drawProbes = true;
        }
        drawnReads.removeAllElements();

        if (drawReads) {
            for (int i = 0, readsLength = reads.length; i < readsLength; i++) {
                if (reads[i].getEnd() >= currentViewerStart && reads[i].getStart() <= currentViewerEnd) {
                    drawRead(g, reads[i], readsYIndex[i] * readHeight);
                }
            }
            //Always draw the active read last
            if (activeRead != null) {
                g.setColor(ColourScheme.ACTIVE_READ);
                drawRect(g, activeRead.getStart(), activeRead.getEnd(), activeReadIndex, readHeight);
                g.setColor(ColourScheme.DATA_TRACK);
            }
        }
        if (drawProbes) {
            for (Probe probe : probes) {
                if (probe.getStart() > currentViewerStart && probe.getStart() < currentViewerEnd) {
                    drawProbe(g, probe);
                }
            }
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

    private void drawRead(Graphics g, SequenceRead r, int pixelYStart) {

        g.setColor(ColourScheme.DATA_TRACK);
        int start = r.getStart();
        int end = r.getEnd();
        drawnReads.add(new DrawnRead(start, end, pixelYStart, pixelYStart + readHeight, r));
        if (currentViewerEnd - currentViewerStart > displayWidth) {
            fillRect(g, start, end, pixelYStart, readHeight);
        } else {
            drawRoundRect(g, start, end, pixelYStart, readHeight);
            char[] cChar = AsciiUtils.getChars(r.getReadBases());
            g.setFont(FontManager.defaultFont);
            for (int i = 0; i < cChar.length; i++) {
                char c = cChar[i];
                drawBase(g, c, start + i, pixelYStart + readHeight);
            }
        }
    }

    private void drawProbe(Graphics g, Probe b) {
        g.setColor(ColourScheme.getBaseColor(b.getAltBase()));
        int position = b.getStart();
        int baseWidth = g.getFontMetrics().stringWidth(String.valueOf(b.getAltBase()));
        int left = bpToPixel(position) - basePixel / 2 + baseWidth / 2;
        int right = bpToPixel(position) + basePixel / 2 + baseWidth / 2;
        g.drawLine(left, 0, left, displayHeight);
        g.drawLine(right, 0, right, displayHeight);
    }

    /**
     * This call can be made whenever the reads need to be updated -
     * particularly when the viewer is being switched to show a different
     * chromosome.
     */
    @Override
    protected void updateTrack(Chromosome chromosome) {
        String currentChromosome = chromosome.getName();
        if (dataCollection.probeSet() != null) {
            probes = dataCollection.probeSet().getActiveList().getProbesForChromosome(currentChromosome);
        } else {
            probes = new Probe[0];
        }
        Arrays.sort(probes);
        reads = data.getReadsForChromosome(currentChromosome);
        readsYIndex = new int[reads.length];
        Arrays.fill(readsYIndex, -1);
        maxCoverage = processSequence();
        // Force the slots to be reassigned
        displayHeight = 0;
        setSize(displayWidth, maxCoverage * readHeight);
        repaint();
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
                    chromosomeViewer.application().setStatusText(
                            " " + data.name() + " " + r.read.toString());
                    activeRead = r.read;
                    activeReadIndex = r.bottom;
                    repaint();
                }
                return;
            }
        }
    }

    private int processSequence() {
        if (reads == null || reads.length == 0) {
            return 0;
        }
        List<Integer> lastYIndexes = new ArrayList<Integer>();
        lastYIndexes.add(reads[0].getEnd());
        int readsLength = reads.length;
        readsYIndex[0] = 0;
        for (int i = 1; i < readsLength; i++) {
            int j = 0;
            int yIndexLength = lastYIndexes.size();
            int currentReadStart = reads[i].getStart();
            for (; j < yIndexLength; j++) {
                if (currentReadStart - lastYIndexes.get(j) > 0) {
                    readsYIndex[i] = j;
                    lastYIndexes.set(j, reads[i].getEnd());
                    j = yIndexLength;
                }
            }
            if (readsYIndex[i] == -1) {
                readsYIndex[i] = j + 1;
                lastYIndexes.add(reads[i].getEnd());
            }
        }
        return lastYIndexes.size();
    }

//    @Override
//    public Dimension getMinimumSize() {
//        return new Dimension(displayWidth, maxCoverage * readHeight);
//    }

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
    public void probeSetReplaced(ProbeSet p) {
        if (p == null) {
            probes = null;
        } else {
            probes = p.getProbesForChromosome(DisplayPreferences.getInstance().getCurrentChromosome().getName());
            Arrays.sort(probes);
        }
    }

    @Override
    public void activeDataStoreChanged(DataStore s) {

    }

    @Override
    public void activeProbeListChanged(ProbeList l) {
        if (l == null) {
            probes = null;
        } else {
            probes = l.getProbesForChromosome(DisplayPreferences.getInstance().getCurrentChromosome().getName());
            Arrays.sort(probes);
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
         * In many cases we don't need to search through reads and probes, so we
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
        public SequenceRead read;

        /**
         * Instantiates a new drawn read.
         *
         * @param left   the left
         * @param right  the right
         * @param bottom the bottom
         * @param top    the top
         * @param read   the read
         */
        public DrawnRead(int left, int right, int bottom, int top, SequenceRead read) {
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
