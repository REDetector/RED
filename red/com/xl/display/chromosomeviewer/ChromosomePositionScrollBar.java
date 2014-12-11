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

import com.xl.interfaces.DisplayPreferencesListener;
import com.xl.preferences.DisplayPreferences;

import javax.swing.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class ChromosomePositionScrollBar extends JScrollBar implements DisplayPreferencesListener, AdjustmentListener {

    private boolean ignoreInternal = false;

    public ChromosomePositionScrollBar() {
        super(JScrollBar.HORIZONTAL, 0, 250, 0, 10000);
        DisplayPreferences.getInstance().addListener(this);
        addAdjustmentListener(this);
    }

    public void displayPreferencesUpdated(DisplayPreferences prefs) {

        // The value of a scroll bar is always its start position. You can get the end position by adding the extent.

        int startPoint = prefs.getCurrentStartLocation();
        double proportion = startPoint / (double) prefs.getCurrentChromosome().getLength();
        double extentProportion = prefs.getCurrentLength() / (double) prefs.getCurrentChromosome().getLength();
        int extent = (int) (10000 * extentProportion);
        int value = (int) (10000 * proportion);
        ignoreInternal = true;
        getModel().setRangeProperties(value, extent, 0, 10000, false);
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (ignoreInternal) {
            ignoreInternal = false;
        } else {
            DisplayPreferences dp = DisplayPreferences.getInstance();
            double proportion = getValue() / 10000d;
            int newStart = (int) (dp.getCurrentChromosome().getLength() * proportion);
            int distance = dp.getCurrentLength();
            int newEnd = newStart + (distance - 1);
            dp.setLocation(newStart, newEnd);
        }
    }

}
