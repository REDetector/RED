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
import com.xl.utils.ColourScheme;
import com.xl.utils.SequenceReadUtils;
import com.xl.utils.Strand;

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
     * The x.
     */
    private int x = 0;
    /**
     * The width.
     */
    private int displayWidth;
    /**
     * The last cached height.
     */
    private int displayHeight;
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

    /** Stores the packed slot index for each read in this display */
    /**
     * Stores the Y axis base position for each lane of sequence reads
     */
    private int[] slotYValues = null;
    /**
     * The height of each read
     */
    private int readHeight = 5;
    private int maxCoverage = -1;
    private List<Integer> seqIndex = null;

    /** The amount of space between reads */
    // private int readWitdhSpace = 2;

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
//		File file = new File("D:/test.txt");
//		FileWriter pw = null;
//		try {
//			pw = new FileWriter(file);
//			for (int i=0;i<Math.min(1000, reads.length);i++) {
//
//				pw.write(reads[i].toWrite()+"\t"+reads[i].length()+"\n");
//			}
//
//			pw.close();
//		}catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

        // Reads should come ready sorted
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

        // Otherwise we alternate colours so we can see the difference
        // between tracks.
        g.setColor(ColourScheme.DATA_BACKGROUND_EVEN);
        g.fillRect(x, 0, displayWidth, displayHeight);

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

        int startBp = viewer.currentStart();
        int endBp = viewer.currentEnd();
//		System.out.println(this.getClass().getName()+":CurrentStart:"+startBp+"\tCurrentEnd:"+endBp+"\tLength:"+(endBp-startBp));
        if (seqIndex == null || seqIndex.size() == 0) {
            return;
        }


        // Now go through all the reads figuring out whether they
        // need to be displayed
        int startSequenceIndex = 0;
        int endSequenceIndex = 0;
        int index = seqIndex.size();
        for (int i = 0; i < index; i++) {
            if (reads[seqIndex.get(i)].getStart() > startBp) {
                startSequenceIndex = seqIndex.get(i != 0 ? i - 1 : 0);
                break;
            }
        }
        for (int i = index - 1; i >= 0; i--) {
            if (reads[seqIndex.get(i)].getEnd() < endBp) {
                endSequenceIndex = seqIndex.get(i != index - 1 ? i + 1
                        : index - 1);
                break;
            }
        }
        // System.out.println("Start:\t"+startBp+"End:\t"+endBp);
        // System.out.println("Sequence Start:\t"+reads[startSequenceIndex]+"\t"+"Sequence End:\t"+reads[endSequenceIndex]);
        for (int i = startSequenceIndex; i < endSequenceIndex; i++) {
            if (endBp - startBp >= getWidth()) {
                drawRectRead(reads[i], i, g);
            } else {
                drawStringRead(reads[i], i, g);
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
        if (activeRead != null) {
            drawStringRead(activeRead, activeReadIndex, g);
        }

        // System.out.println("Drew "+drawnReads.size()+" reads");

        // Draw a line across the bottom of the display
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(x, displayHeight - 1, x + displayWidth, displayHeight - 1);

        // If we're the active data store then surround us in red

        // This can fail if the viewer is being destroyed (viewer returns null)
        // so catch this
        try {
            if (viewer.application().dataCollection().getActiveDataStore() == data) {
                g.setColor(Color.RED);
                g.drawLine(x, displayHeight - 2, x + displayWidth,
                        displayHeight - 2);
                g.drawLine(x, displayHeight - 1, x + displayWidth,
                        displayHeight - 1);
                g.drawLine(x, 0, x + displayWidth, 0);
                g.drawLine(x, 1, x + displayWidth, 1);
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
        g.fillRect(x, 1, nameWidth + 3, nameHeight + 3);

        // Finally draw the name of the data track
        g.setColor(Color.GRAY);
        g.drawString(name, x + 2, nameHeight + 2);
    }

    private void drawRectRead(SequenceRead r, int index, Graphics g) {
        int wholeXStart = bpToPixel(r.getStart());
        int wholeXEnd = bpToPixel(r.getEnd());

        // System.out.println("Drawing read from "+r.start()+"-"+r.end()+" "+wholeXEnd+"-"+wholeXEnd+" lastX slot"+slotValues[index]+" end "+lastReadXEnds[slotValues[index]]);

        // We make sure that this new read is at least 3px wide
        if ((wholeXEnd - wholeXStart) < 3) {
            wholeXEnd = wholeXStart + 3;
        }

        if (r == activeRead && index == activeReadIndex) {
            g.setColor(ColourScheme.ACTIVE_FEATURE);
        } else if (r == activeRead) {
            g.setColor(ColourScheme.ACTIVE_FEATURE_MATCH);
        } else {
            if (r.getStrand() == Strand.POSITIVE) {
                g.setColor(ColourScheme.FORWARD_FEATURE);
            } else if (r.getStrand() == Strand.NEGATIVE) {
                g.setColor(ColourScheme.REVERSE_FEATURE);
            } else {
                g.setColor(ColourScheme.UNKNOWN_FEATURE);
            }
        }
        int yBoxStart = 0;
        for (int i = seqIndex.size() - 1; i >= 0; i--) {
            if ((index - seqIndex.get(i) >= 0)
                    && (index - seqIndex.get(i) < maxCoverage)) {
                yBoxStart = slotYValues[index - seqIndex.get(i)];
//				System.out.println("Sequence Index: " + index + "\tSeqIndex:"
//						+ seqIndex.get(i) + "\tyBoxStart:" + yBoxStart
//						+ "\tmaxCoverage:" + maxCoverage);
                break;
            }
        }

        // System.out.println("Sequence Start: "+r.getLocation().getStart()+"\tSequence End:"+r.getLocation().getEnd());
        // System.out.println("Drawing read from "+wholeXStart+","+yBoxStart+","+wholeXEnd+","+yBoxEnd);
        drawnReads.add(new DrawnRead(wholeXStart, wholeXEnd, yBoxStart,
                readHeight, index, r));
        g.fillRect(wholeXStart, yBoxStart, wholeXEnd - wholeXStart, readHeight);
    }

    /**
     * Draw read.
     *
     * @param r     the r
     * @param index the index
     * @param g     the g
     */
    private void drawStringRead(SequenceRead r, int index, Graphics g) {

        int wholeXStart = bpToPixel(r.getStart());
        int wholeXEnd = bpToPixel(r.getEnd());

        // System.out.println("Drawing read from "+r.start()+"-"+r.end()+" "+wholeXEnd+"-"+wholeXEnd+" lastX slot"+slotValues[index]+" end "+lastReadXEnds[slotValues[index]]);

        // We make sure that this new read is at least 3px wide
        if ((wholeXEnd - wholeXStart) < 3) {
            wholeXEnd = wholeXStart + 3;
        }

        if (r == activeRead && index == activeReadIndex) {
            g.setColor(ColourScheme.ACTIVE_FEATURE);
        } else if (r == activeRead) {
            g.setColor(ColourScheme.ACTIVE_FEATURE_MATCH);
        } else {
            if (r.getStrand() == Strand.POSITIVE) {
                g.setColor(ColourScheme.FORWARD_FEATURE);
            } else if (r.getStrand() == Strand.NEGATIVE) {
                g.setColor(ColourScheme.REVERSE_FEATURE);
            } else {
                g.setColor(ColourScheme.UNKNOWN_FEATURE);
            }
        }
        int yBoxStart = 0;
        for (int i = seqIndex.size() - 1; i >= 0; i--) {
            if ((index - seqIndex.get(i) >= 0)
                    && (index - seqIndex.get(i) < maxCoverage)) {
                yBoxStart = slotYValues[index - seqIndex.get(i)];
//				System.out.println("Sequence Index: " + index + "\tSeqIndex:"
//						+ seqIndex.get(i) + "\tyBoxStart:" + yBoxStart
//						+ "\tmaxCoverage:" + maxCoverage);
                break;
            }
        }

        int yBoxEnd = yBoxStart + readHeight;
        // System.out.println("Sequence Start: "+r.getLocation().getStart()+"\tSequence End:"+r.getLocation().getEnd());
        // System.out.println("Drawing read from "+wholeXStart+","+yBoxStart+","+wholeXEnd+","+yBoxEnd);
        drawnReads.add(new DrawnRead(wholeXStart, wholeXEnd, yBoxStart,
                yBoxEnd, index, r));

        // System.out.println("Drawing probe from x="+wholeXStart+" y="+yBoxStart+" width="+(wholeXEnd-wholeXStart)+" height="+(yBoxEnd-yBoxStart));
        // g.fillRect((int)wholeXStart, yBoxStart, (int)(wholeXEnd -
        // wholeXStart), yBoxEnd
        // - yBoxStart);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 10));
        g.drawString(new String(r.getReadBases()), wholeXStart, yBoxStart);

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
                if (activeRead != r.read || r.index != activeReadIndex) {
                    viewer.application().setStatusText(
                            " " + data.name() + " " + r.read.toString());
                    activeRead = r.read;
                    activeReadIndex = r.index;
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
        if (width < 5)
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
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

        List<Integer> seqIndexCoverage = new ArrayList<Integer>();
        int j = 0;
        for (int index = 0; index < reads.length; index++) {
            if (!SequenceReadUtils.overlaps(reads[index], reads[j])) {
                seqIndexCoverage.add(index);
                if (maxCoverage < index - j) {
                    maxCoverage = index - j;
                }
                j = index;
            }
        }
//		File file = new File("D:/test.txt");
//		try {
//			FileWriter fw = new FileWriter(file);
//			for (int i = 0; i < seqIndexCoverage.size() - 1; i++) {
//				if (seqIndexCoverage.get(i + 1) - seqIndexCoverage.get(i) == maxCoverage) {
//					for (int k = seqIndexCoverage.get(i); k < seqIndexCoverage
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

        System.out.println(this.getClass().getName() + ":maxCoverage\t" + maxCoverage + "\tj:" + j);
        slotYValues = new int[maxCoverage];
        for (int i = 0; i < slotYValues.length; i++) {
            slotYValues[i] = readHeight * i;
        }

        return seqIndexCoverage;
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
         * The index
         */
        public int index;
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
        public DrawnRead(int left, int right, int bottom, int top, int index,
                         SequenceRead read) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
            this.index = index;
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
