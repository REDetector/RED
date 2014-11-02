/*
 * RED---RNA Editing Detector
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

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.genome.Chromosome;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.ColourScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by Administrator on 2014/10/12.
 */
public abstract class AbstractTrack extends JPanel implements MouseListener, MouseMotionListener {
    protected ChromosomeViewer chromosomeViewer;
    protected DataCollection dataCollection;
    protected String trackName;
    protected int displayWidth;
    protected int displayHeight;
    protected int currentViewerStart;
    protected int currentViewerEnd;
    protected int basePixel;

    public AbstractTrack(ChromosomeViewer chromosomeViewer, DataCollection dataCollection, String trackName) {
        this.chromosomeViewer = chromosomeViewer;
        this.dataCollection = dataCollection;
        this.trackName = trackName;
        displayWidth = getWidth();
        displayHeight = getHeight();
        currentViewerStart = chromosomeViewer.currentStart();
        currentViewerEnd = chromosomeViewer.currentEnd();
        basePixel = getWidth() / getViewerLength();
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    protected int getViewerLength() {
        return currentViewerEnd - currentViewerStart + 1;
    }

    protected abstract void updateTrack(Chromosome chromosome);

    @Override
    public void paint(Graphics g) {
        displayWidth = getWidth();
        displayHeight = getHeight();
        currentViewerStart = chromosomeViewer.currentStart();
        currentViewerEnd = chromosomeViewer.currentEnd();
        basePixel = getWidth() / getViewerLength();
        // Otherwise we alternate colours so we can see the difference between tracks.
        if (chromosomeViewer.getIndex(this) % 2 == 0) {
            g.setColor(ColourScheme.DATA_BACKGROUND_EVEN);
        } else {
            g.setColor(ColourScheme.DATA_BACKGROUND_ODD);
        }
        g.fillRect(0, 0, displayWidth, displayHeight);

        // If we're in the middle of making a selection then highlight the selected part of the display in green.
        if (chromosomeViewer.makingSelection()) {
            int selStart = chromosomeViewer.selectionStart();
            int selEnd = chromosomeViewer.selectionEnd();
            int useStart = (selEnd > selStart) ? selStart : selEnd;
            int selWidth = Math.abs(selEnd - selStart);
            g.setColor(ColourScheme.DRAGGED_SELECTION);
            g.fillRect(useStart, 0, selWidth, displayHeight);
        }

        // Draw a box into which we'll put the track name so it's not obscured by the data
        int nameWidth = g.getFontMetrics().stringWidth(trackName);
        int nameHeight = g.getFontMetrics().getAscent();
        g.setColor(Color.ORANGE);
        g.fillRect(0, 1, nameWidth + 3, nameHeight + 3);

        // Finally draw the name of the data track
        g.setColor(ColourScheme.TRACK_NAME);
        g.drawString(trackName, 2, nameHeight + 2);
    }

    protected void drawBase(Graphics g, char base, int x, int y) {
        g.setColor(ColourScheme.getBaseColor(base));
        g.drawString(String.valueOf(base).toUpperCase(), bpToPixel(x), y);
    }

    protected void fillRect(Graphics g, int positionLeft, int positionRight, int yStart, int height) {
        int width = (int) ((double) (positionRight - positionLeft) / getViewerLength() * displayWidth);
        if (width < 1) {
            width = 1;
        }
        int xStart = bpToPixel(positionLeft);
        g.fillRect(xStart, yStart, width, height);
    }

    protected void drawRoundRect(Graphics g, int positionLeft, int positionRight, int yStart, int height) {
        int width = (int) ((double) (positionRight - positionLeft) / getViewerLength() * displayWidth);
        if (width < 1) {
            width = 1;
        }
        int xStart = bpToPixel(positionLeft);
        g.drawRoundRect(xStart, yStart, width, height, 3, 3);
    }

    protected void drawRect(Graphics g, int positionLeft, int positionRight, int yStart, int height) {
        int width = (int) ((double) (positionRight - positionLeft) / getViewerLength() * displayWidth);
        if (width < 1) {
            width = 1;
        }
        int xStart = bpToPixel(positionLeft);
        g.drawRect(xStart, yStart, width, height);
    }

    protected void drawLine(Graphics g, int x1, int x2, int y1, int y2) {
        int xStart = bpToPixel(x1);
        int xEnd = bpToPixel(x2);
        g.drawLine(xStart, y1, xEnd, y2);
    }

    /**
     * Pixel to bp.
     *
     * @param x the x
     * @return the int
     */
    protected int pixelToBp(int x) {
        int pos = chromosomeViewer.currentStart() + (int) (((double) x / displayWidth) * getViewerLength());
        if (pos < 1)
            pos = 1;
        if (pos > chromosomeViewer.chromosome().getLength())
            pos = chromosomeViewer.chromosome().getLength();
        return pos;
    }

    /**
     * Bp to pixel.
     *
     * @param bp the bp
     * @return the int
     */
    protected int bpToPixel(int bp) {
        return (int) (((double) (bp - currentViewerStart) / getViewerLength()) * displayWidth);
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        chromosomeViewer.setSelectionEnd(me.getX());
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK && me.getClickCount() == 1) {
            chromosomeViewer.zoomOut();
        } else if ((me.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK && me.getClickCount() == 1) {
            chromosomeViewer.zoomIn();
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        // Don't start making a selection if they click the right mouse button
        if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            return;
        }
        chromosomeViewer.setMakingSelection(true);
        chromosomeViewer.setSelectionStart(me.getX());
        chromosomeViewer.setSelectionEnd(me.getX());
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        // Don't process anything if they released the right mouse button
        if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            return;
        }
        chromosomeViewer.setMakingSelection(false);

        int width = chromosomeViewer.selectionEnd() - chromosomeViewer.selectionStart();
        if (width < 0) {
            width = 0 - width;
        }
        if (width < 5)
            return;

        DisplayPreferences.getInstance().setLocation(pixelToBp(chromosomeViewer.selectionStart()), pixelToBp(chromosomeViewer.selectionEnd()));
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        chromosomeViewer.application().setStatusText(" " + trackName);
    }
}
