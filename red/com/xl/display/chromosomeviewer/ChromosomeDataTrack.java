package com.xl.display.chromosomeviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.interfaces.DataChangeListener;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.AsciiUtils;
import com.xl.utils.ColourScheme;
import com.xl.utils.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.util.List;

/**
 * The Class ChromosomeDataTrack represents a single track in the chromosome
 * view containing the data from a single data store. Depending on the display
 * preferences it can show either just raw data, or quantitated data, or both.
 */
public class ChromosomeDataTrack extends JPanel implements DataChangeListener {


    private DataCollection collection = null;
    /**
     * The viewer.
     */
    private ChromosomeViewer viewer = null;
    /**
     * The data.
     */
    private DataStore data = null;
    /**
     * The reads.
     */
    private SequenceRead[] reads = null;

    private Probe[] probes;
    /**
     * The width.
     */
    private int displayWidth;
    /**
     * The last cached height.
     */
    private int displayHeight;

    private int viewerCurrentStart = 0;

    private int viewerCurrentEnd = 0;

    private int readPixel = 0;

    private boolean timing = true;

    private float basePixel = 0;

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
        this.viewer = viewer;
        this.data = data;
        this.collection = collection;
        updateReads();
        // System.out.println("Chr"+viewer.chromosome().name()+" has "+probes.length+" probes on it");
        SequenceListner listner = new SequenceListner();
        addMouseMotionListener(listner);
        addMouseListener(listner);
    }

    /**
     * This call can be made whenever the reads need to be updated -
     * particularly when the viewer is being switched to show a different
     * chromosome.
     */
    public void updateReads() {
        String currentChromosome = DisplayPreferences.getInstance().getCurrentChromosome().getName();
        System.out.println(this.getClass().getName() + ":updateReads()\t");
        if (collection.probeSet() != null) {
            probes = collection.probeSet().getActiveList().getProbesForChromosome(currentChromosome);
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
        setSize(getWidth(), maxCoverage * readHeight);
        repaint();
    }

    /**
     * This call is made by the ChromosomeViewer when the active
     * probe list is changed.  It allows us to update the set of
     * probes without having to do a completely new layout (which
     * is slow).
     *
     * @param newProbes The new probes for the currently displayed chromosome
     */
    protected void setProbes(Probe[] newProbes) {
        probes = newProbes;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        super.paint(g);

        int displayMode = DisplayPreferences.getInstance().getDisplayMode();
        drawProbes = true;
        drawReads = true;
        if (displayMode == DisplayPreferences.DISPLAY_MODE_PROBES_ONLY) {
            drawReads = false;
        }
        if (displayMode == DisplayPreferences.DISPLAY_MODE_READS_ONLY) {
            drawProbes = false;
        }

        drawnReads.removeAllElements();
        displayHeight = getHeight();
        displayWidth = getWidth();
        viewerCurrentStart = viewer.currentStart();
        viewerCurrentEnd = viewer.currentEnd();
        if (reads != null && reads.length != 0) {
            readPixel = bpToPixel(reads[0].length() + viewerCurrentStart);
        } else {
            return;
        }
        if (readPixel == 0) {
            readPixel = 1;
        }
        // Otherwise we alternate colours so we can see the difference between tracks.
        if (viewer.getIndex(this) % 2 == 0) {
            g.setColor(ColourScheme.DATA_BACKGROUND_EVEN);
        } else {
            g.setColor(ColourScheme.DATA_BACKGROUND_ODD);
        }
        g.fillRect(0, 0, displayWidth, displayHeight);

        // If we're in the middle of making a selection then highlight the
        // selected part of the display in green.
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

        if (drawReads) {
            for (int i = 0, readsLength = reads.length; i < readsLength; i++) {
                if (reads[i].getEnd() >= viewerCurrentStart && reads[i].getStart() <= viewerCurrentEnd) {
                    drawRead(g, reads[i], bpToPixel(reads[i].getStart()), readsYIndex[i] * readHeight);
                }
            }     //        Always draw the active read last
            if (activeRead != null) {
                drawRead(g, activeRead, bpToPixel(activeRead.getStart()), activeReadIndex);
            }
        }
        if (drawProbes) {
            for (Probe probe : probes) {
                if (probe.getStart() > viewerCurrentStart && probe.getStart() < viewerCurrentEnd) {
                    drawProbe(g, probe);
                }
            }
        }

        // Draw a line across the bottom of the display
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, displayHeight - 1, displayWidth, displayHeight - 1);

        // If we're the active data store then surround us in red

        // This can fail if the viewer is being destroyed (viewer returns null)
        // so catch this
        try {
            if (viewer.application().dataCollection().getActiveDataStore() == data) {
                g.setColor(Color.RED);
                g.drawLine(0, displayHeight - 2, displayWidth,
                        displayHeight - 2);
                g.drawLine(0, displayHeight - 1, displayWidth,
                        displayHeight - 1);
                g.drawLine(0, 0, displayWidth, 0);
                g.drawLine(0, 1, displayWidth, 1);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        String name = data.name();

        // Draw a box into which we'll put the track name so it's not obscured
        // by the data
        int nameWidth = g.getFontMetrics().stringWidth(name);
        int nameHeight = g.getFontMetrics().getAscent();
        g.setColor(Color.ORANGE);
        g.fillRect(0, 1, nameWidth + 3, nameHeight + 3);

        // Finally draw the name of the data track
        g.setColor(ColourScheme.TRACK_NAME);
        g.drawString(name, 2, nameHeight + 2);
    }

    private void drawRead(Graphics g, SequenceRead r, int pixelXStart, int pixelYStart) {

        g.setColor(getSequenceColor(r, pixelYStart));
        drawnReads.add(new DrawnRead(pixelXStart, pixelXStart + readPixel, pixelYStart,
                pixelYStart + readHeight, r));
        if (viewerCurrentEnd - viewerCurrentStart >= getWidth()) {
            g.fillRect(pixelXStart, pixelYStart, readPixel, readHeight);
        } else {
            g.drawRoundRect(pixelXStart, pixelYStart, readPixel, readHeight, 3, 3);
            byte[] readBases = r.getReadBases();
            char[] cChar = AsciiUtils.getChars(readBases);
            basePixel = (float) (readPixel) / (readBases.length);
            g.setFont(FontManager.defaultFont);
            for (int i = 0; i < cChar.length; i++) {
                char c = cChar[i];
                g.setColor(ColourScheme.getBaseColor(c));
                g.drawString(String.valueOf(c), (int) (pixelXStart + basePixel * i + basePixel / 2), pixelYStart + readHeight);
            }
        }
    }

    private void drawProbe(Graphics g, Probe b) {
        g.setColor(ColourScheme.getBaseColor(b.getEditingBase()));
        int position = b.getStart();
        int left = Math.round(bpToPixel(position));
        int right = Math.round(bpToPixel(position) + basePixel);
        g.drawLine(left, 0, left, displayHeight);
        g.drawLine(right, 0, right, displayHeight);
    }

    private Color getSequenceColor(SequenceRead read, int pixelYStart) {
        if (read == activeRead) {
            return ColourScheme.ACTIVE_READ;
        } else {
            return ColourScheme.DATA_TRACK;
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
        return (int) (((double) (bp - viewerCurrentStart) / ((viewerCurrentEnd - viewerCurrentStart))) * displayWidth);
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
                    viewer.application().setStatusText(
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

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(displayWidth, maxCoverage * readHeight);
    }

    private class SequenceListner implements MouseListener, MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent me) {
            viewer.setSelectionEnd(me.getX());
        }

        @Override
        public void mouseMoved(MouseEvent me) {
        /*
         * In many cases we don't need to search through reads and probes, so we
		 * can quickly work out what we should be looking for from what we're
		 * drawing and where the mouse is.
		 */
            findRead(me.getX(), me.getY());
            long lastTime = System.currentTimeMillis();
            while (timing) {
                if (System.currentTimeMillis() - lastTime > 1000) {
                    setToolTipText("Single left click to zoom in and single right click to zoom out.");
                    timing = false;
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK && me.getClickCount() == 1) {
                viewer.zoomOut();
            } else if ((me.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK && me.getClickCount() ==
                    1) {
                viewer.zoomIn();
            }
        }

        @Override
        public void mousePressed(MouseEvent me) {
            // Don't start making a selection if they click the right mouse button
            if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                return;
            }
            viewer.setMakingSelection(true);
            viewer.setSelectionStart(me.getX());
            viewer.setSelectionEnd(me.getX());
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            // Don't process anything if they released the right mouse button
            if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                return;
            }
            viewer.setMakingSelection(false);

            int width = viewer.selectionEnd() - viewer.selectionStart();
            if (width < 0) {
                width = 0 - width;
            }
            if (width < 5)
                return;

            DisplayPreferences.getInstance().setLocation(
                    pixelToBp(viewer.selectionStart()),
                    pixelToBp(viewer.selectionEnd()));
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
            viewer.application().setStatusText(" " + data.name());
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
            activeRead = null;
            timing = true;
            repaint();
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
        public DrawnRead(int left, int right, int bottom, int top,
                         SequenceRead read) {
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
}
