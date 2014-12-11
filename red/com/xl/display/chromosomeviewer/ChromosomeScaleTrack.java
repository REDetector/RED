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

import com.xl.preferences.DisplayPreferences;
import com.xl.utils.AxisScale;

import javax.swing.*;
import java.awt.*;

/**
 * The ChromosomeScaleTrack shows the current genome position on a sensible scale. It is usually only created and managed by a surrounding instance of
 * ChromosomeViewer.
 */
public class ChromosomeScaleTrack extends JPanel {

    /**
     * The chromosome viewer which contains this track.
     */
    private ChromosomeViewer viewer;

    /**
     * The full virtual width of this track
     */
    private int lastStartLocation = -1;
    private int lastEndLocation = -1;

    /**
     * A cached value of the width of the visible portion of this track inside the surrounding JScrollPane
     */
    private int width;

    private AxisScale scale = null;

    /**
     * Instantiates a new scale track.
     *
     * @param viewer The chromosome viewer which holds this track
     */
    public ChromosomeScaleTrack(ChromosomeViewer viewer) {
        this.viewer = viewer;
    }

    private String commify(int number) {
        char[] numbers = ("" + number).toCharArray();

        char[] commaNumbers = new char[numbers.length + ((numbers.length - 1) / 3)];

        int commaPos = commaNumbers.length - 1;
        for (int numberPos = 0; numberPos < numbers.length; numberPos++) {
            if (numberPos % 3 == 0 && numberPos > 0) {
                commaNumbers[commaPos] = ',';
                commaPos--;
            }
            commaNumbers[commaPos] = numbers[numbers.length - (numberPos + 1)];
            commaPos--;
        }

        return new String(commaNumbers);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        DisplayPreferences dp = DisplayPreferences.getInstance();

        if (dp.getCurrentStartLocation() != lastStartLocation && dp.getCurrentEndLocation() != lastEndLocation) {
            // We need to rescale the frequency with which we're drawing points
            scale = new AxisScale(0, dp.getCurrentLength());
            lastStartLocation = dp.getCurrentStartLocation();
            lastEndLocation = dp.getCurrentEndLocation();
        }

        int height = getHeight();
        width = getWidth();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.DARK_GRAY);

        // Draw a line along the top
        g.drawLine(0, 3, width, 3);

        // Now go through all the scale positions figuring out whether they need to be displayed

        int startBp = lastStartLocation;
        int endBp = lastEndLocation;

        int currentBase = 0;

        while (currentBase < endBp) {

            if (currentBase < startBp) {
                currentBase += scale.getInterval();
                continue;
            }

            String name = commify(currentBase);

            int nameWidth = g.getFontMetrics().stringWidth(name);

            int thisX = bpToPixel(currentBase);

            g.drawString(name, thisX - (nameWidth / 2), getHeight() - 2);

            g.drawLine(thisX, 3, thisX, height
                    - (g.getFontMetrics().getAscent() + 3));

            currentBase += scale.getInterval();
        }

    }

    /**
     * Bp to pixel.
     *
     * @param bp the bp
     * @return the int
     */
    private int bpToPixel(int bp) {
        return (int) (((double) (bp - viewer.currentStart()) / ((viewer.currentEnd() - viewer.currentStart()))) * width);
    }

    // There's no sense in letting the annotation tracks get too tall. We're better off using that space for data tracks.
    /*
     * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
    public Dimension getMinimumSize() {
        return new Dimension(30, 25);
    }

    public Dimension getPreferredSize() {
        return new Dimension(30, 25);
    }

}
