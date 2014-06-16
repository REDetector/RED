package com.xl.display.chromosomeviewer;

/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import com.xl.datatypes.DataStore;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.interfaces.HiCDataStore;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * The Class ChromosomeDataTrack represents a single track in the chromosome
 * view containing the data from a single data store. Depending on the display
 * preferences it can show either just raw data, or quantitated data, or both.
 */
public class ChromosomeDataTrack extends JPanel implements MouseListener,
        MouseMotionListener {

    // private static final int MAX_HEIGHT = 500;

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
    private int maxCoverage = -1;
    private List<Integer> seqIndex = null;

    private List<Integer> coverage = null;
    private List<Integer> overlapReads = null;

    private int overlapIndex = 0;
    /** The amount of space between reads */

    /**
     * Instantiates a new chromosome data track.
     *
     * @param viewer the viewer
     * @param data   the data
     */
    public ChromosomeDataTrack(ChromosomeViewer viewer, DataStore data) {
        this.viewer = viewer;
        this.data = data;
        updateReads();
        // System.out.println("Chr"+viewer.chromosome().name()+" has "+probes.length+" probes on it");
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    /**
     * This call can be made whenever the reads need to be updated -
     * particularly when the viewer is being switched to show a different
     * chromosome.
     */
    public void updateReads() {
        System.out.println(this.getClass().getName() + ":updateReads()\t");
        reads = data.getReadsForChromosome(DisplayPreferences.getInstance()
                .getCurrentChromosome().getName());
//        File file = new File("D:/test.txt");
//        FileWriter pw = null;
//        try {
//            pw = new FileWriter(file);
//            for (int i = 0; i < Math.min(1000, reads.length); i++) {
//
//                pw.write(reads[i].toWrite() + "\t" + reads[i].length() + "\n");
//            }
//            pw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // Reads should come ready sorted
        overlapReads = new ArrayList<Integer>();
        coverage = new ArrayList<Integer>();
        seqIndex = processSequence();
        // Force the slots to be reassigned
        displayHeight = 0;

        repaint();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        super.paint(g);

        drawnReads.removeAllElements();
        displayHeight = getHeight();
        displayWidth = getWidth();
        viewerCurrentStart = viewer.currentStart();
        viewerCurrentEnd = viewer.currentEnd();

        if (reads != null && reads.length != 0) {
            readPixel = bpToPixel(reads[0].length() + viewerCurrentStart);
        } else {
            MessageUtils.showError(ChromosomeDataTrack.class, "Can't get the reads of this chromosome.");
            return;
        }
        if (readPixel == 0) {
            readPixel = 1;
        }
//        MessageUtils.showInfo(ChromosomeDataTrack.class,"readPixel:"+readPixel);
        // Otherwise we alternate colours so we can see the difference
        // between tracks.
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


//		System.out.println(this.getClass().getDisplayName()+":CurrentStart:"+startBp+"\tCurrentEnd:"+endBp+"\tLength:"+(endBp-startBp));
        if (seqIndex == null || seqIndex.size() == 0) {
            return;
        }

        // Now go through all the reads figuring out whether they
        // need to be displayed
        int pixelReadXStart;
        int startFirstRowIndex = 0;
        int endFirstRowIndex = 0;
        int length = seqIndex.size();
        for (int i = 0; i < length; i++) {
            if (reads[seqIndex.get(i)].getStart() > viewerCurrentStart) {
                startFirstRowIndex = i >= 2 ? i - 2 : i;
                break;
            }
        }
        for (int i = length - 1; i >= 0; i--) {
            if (reads[seqIndex.get(i)].getEnd() < viewerCurrentEnd) {
                endFirstRowIndex = (i + 2) <= length ? i + 2 : i;
                break;
            }
        }
        // System.out.println("Start:\t"+startBp+"End:\t"+endBp);
//        System.out.println("Sequence Start:\t" + reads[startFirstRowIndex] + "\t" + "Sequence End:\t" + reads[endFirstRowIndex]);
        for (int i = startFirstRowIndex; i < endFirstRowIndex; i++) {
            int startIndex = seqIndex.get(i);
            int secondIndex = seqIndex.get((i + 1) != length ? i + 1 : length);
            int pixelReadYStart;
            overlapIndex = 0;
            for (int j = startIndex; j < secondIndex; j++) {
                if (overlapReads.contains(j)) {
                    int preCoverage = coverage.get(i >= 2 ? i - 2 : 0);
                    int afterCoverage = coverage.get(i >= 1 ? i - 1 : 0);
                    overlapIndex++;
                    if (preCoverage > afterCoverage) {
                        pixelReadYStart = (preCoverage + overlapIndex) * readHeight;
                    } else {
                        pixelReadYStart = (afterCoverage + overlapIndex) * readHeight;
                    }
                } else {
                    pixelReadYStart = (j - startIndex) * readHeight;
                }
                pixelReadXStart = bpToPixel(reads[j].getStart());
                drawRead(g, reads[j], pixelReadXStart, pixelReadYStart);
            }
        }

        // for (int i = 0; i < reads.length; i++) {
        // // System.out.println("Looking at read " + i + " Start: "
        // // + reads[i].getLocation().getStart() + " End: "
        // // + reads[i].getLocation().getEnd() + " Global Start:"
        // // + startBp + " End:" + endBp);
        // if (reads[i].getLocation().getEnd() > startBp
        // && reads[i].getLocation().getStart() < endBp) {
        // }
        // }

        // Always draw the active read last
//        if (activeRead != null) {
//            pixelReadXStart = bpToPixel(activeRead.getStart());
//            drawRead(g, activeRead, pixelReadXStart, activeReadIndex);
//        }

//        System.out.println("Drew "+drawnReads.size()+" reads");

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

        if (data instanceof HiCDataStore && ((HiCDataStore) data).isValidHiC()) {
            name = "[HiC] " + name;
        }

        // Draw a box into which we'll put the track name so it's not obscured
        // by the data
        int nameWidth = g.getFontMetrics().stringWidth(name);
        int nameHeight = g.getFontMetrics().getAscent();
        g.setColor(Color.ORANGE);
        g.fillRect(0, 1, nameWidth + 3, nameHeight + 3);

        // Finally draw the name of the data track
        g.setColor(Color.GRAY);
        g.drawString(name, 2, nameHeight + 2);
    }

    private void drawRead(Graphics g, SequenceRead r, int pixelXStart, int pixelYStart) {

        // System.out.println("Drawing read from "+r.start()+"-"+r.end()+" "+wholeXEnd+"-"+wholeXEnd+" lastX slot"+slotValues[index]+" end "+lastReadXEnds[slotValues[index]]);
        g.setColor(getSequenceColor(r, pixelYStart));
        // System.out.println("Sequence Start: "+r.getLocation().getStart()+"\tSequence End:"+r.getLocation().getEnd());
        // System.out.println("Drawing read from "+wholeXStart+","+yBoxStart+","+wholeXEnd+","+yBoxEnd);
        drawnReads.add(new DrawnRead(pixelXStart, pixelXStart + readPixel, pixelYStart,
                pixelYStart + readHeight, r));
        if (viewerCurrentEnd - viewerCurrentStart >= getWidth()) {
            g.fillRect(pixelXStart, pixelYStart, readPixel, readHeight);
        } else {
            g.drawRoundRect(pixelXStart, pixelYStart, readPixel, readHeight, 3, 3);
            byte[] readBases = r.getReadBases();
            char[] cChar = AsciiUtils.getChars(readBases);
            double basePixel = (double) (readPixel) / (readBases.length + 1);
            g.setFont(new Font("Times New Roman", Font.PLAIN, readHeight));
            for (int i = 0; i < cChar.length; i++) {
                char c = cChar[i];
                if (c == 'a' || c == 'A') {
                    g.setColor(ColourScheme.BASE_A);
                } else if (c == 'g' || c == 'G') {
                    g.setColor(ColourScheme.BASE_G);
                } else if (c == 't' || c == 'T') {
                    g.setColor(ColourScheme.BASE_T);
                } else if (c == 'c' || c == 'C') {
                    g.setColor(ColourScheme.BASE_C);
                } else {
                    g.setColor(ColourScheme.BASE_UNKNOWN);
                }
                g.drawString(String.valueOf(c), (int) (pixelXStart + basePixel * i + basePixel / 2), pixelYStart + readHeight);
            }
        }
    }

    private Color getSequenceColor(SequenceRead read, int pixelYStart) {
        if (read == activeRead && pixelYStart == activeReadIndex) {
            return ColourScheme.ACTIVE_FEATURE;
        } else if (read == activeRead) {
            return ColourScheme.ACTIVE_FEATURE_MATCH;
        } else {
            if (read.getStrand() == Strand.POSITIVE) {
                return ColourScheme.FORWARD_FEATURE;
            } else if (read.getStrand() == Strand.NEGATIVE) {
                return ColourScheme.REVERSE_FEATURE;
            } else {
                return ColourScheme.UNKNOWN_FEATURE;
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
//        System.out.println("bp:" + bp + "\tCurrent start:" + viewerCurrentStart + "\tCurrent end:" + viewer.currentEnd() + "\tDisplay width:" + displayWidth);
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
                if (activeRead != r.read || r.top != activeReadIndex) {
                    viewer.application().setStatusText(
                            " " + data.name() + " " + r.read.toString());
                    activeRead = r.read;
                    activeReadIndex = r.top;
                    repaint();
                }
                return;
            }
        }
    }

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
     * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent me) {
        int x = me.getX();
        int y = me.getY();

		/*
         * In many cases we don't need to search through reads and probes, so we
		 * can quickly work out what we should be looking for from what we're
		 * drawing and where the mouse is.
		 */

        findRead(x, y);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {
        if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            viewer.zoomOut();
        }
        MessageUtils.showInfo(ChromosomeUtils.class, "mouseClicked:" + me.getX());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent me) {
        // Don't start making a selection if they click the right mouse button
        if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            return;
        }
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
        // Don't process anything if they released the right mouse button
        if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            return;
        }
        viewer.setMakingSelection(false);

        int width = viewer.selectionEnd() - viewer.selectionStart();
        if (width < 0) {
            width = 0 - width;
        }
        if (width < 2)
            return;

        DisplayPreferences.getInstance().setLocation(
                pixelToBp(viewer.selectionStart()),
                pixelToBp(viewer.selectionEnd()));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent arg0) {
        viewer.application().setStatusText(" " + data.name());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent arg0) {
        activeRead = null;
        repaint();
    }

    private List<Integer> processSequence() {
        if (reads == null || reads.length == 0) {
            return null;
        }
//		File file = new File("D:/reads_location.txt")
//		try {
//			FileWriter fw = new FileWriter(file);
//			for(int i =0;i<reads.length;i++){
//				fw.write(reads[i].getStart());
//			}
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

        List<Integer> seqIndex = new ArrayList<Integer>();

        int j = 0;
        //Add the first index of column.
        seqIndex.add(j);
        int index;
        int lastSecondIndex = 0;
        int seqIndexSize;
        for (index = 0; index < reads.length; index++) {

            if (j >= 1 && j != index && (index + lastSecondIndex) < 2 * j) {

//                MessageUtils.showInfo("(index - j + lastSecondIndex):" + (index - j + lastSecondIndex) + "\tindex:" + index + "\tj:" + j + "\tlastSecondIndex:" + lastSecondIndex);
                if (SequenceReadUtils.overlaps(reads[index], reads[index - j + lastSecondIndex])) {
                    overlapReads.add(index);
                }
            }
            if (!SequenceReadUtils.overlaps(reads[index], reads[j])) {
                seqIndex.add(index);
                coverage.add(index - j);

                if (maxCoverage < index - j) {
                    maxCoverage = index - j;
                }
                j = index;
                seqIndexSize = seqIndex.size();
                if (seqIndexSize >= 2) {
                    lastSecondIndex = seqIndex.get(seqIndexSize - 2);
                }
            }
        }
//		File file = new File("D:/test.txt");
//		try {
//			FileWriter fw = new FileWriter(file);
//			for (int i = 0; i < seqIndex.size() - 1; i++) {
//				if (seqIndex.get(i + 1) - seqIndex.get(i) == maxCoverage) {
//					for (int k = seqIndex.get(i); k < seqIndex
//							.get(i + 1); k++) {
//						fw.write(reads[k].toWrite() + "\t" + k + "\t" + i
//								+ "\n");
//					}
//				}
//			}
//			fw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

        System.out.println(this.getClass().getName() + ":maxCoverage\t" + maxCoverage + "\toverlapReads:" + overlapReads.size());
        return seqIndex;
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

}
