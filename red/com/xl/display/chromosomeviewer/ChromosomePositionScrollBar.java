package com.xl.display.chromosomeviewer;

import com.xl.interfaces.DisplayPreferencesListener;
import com.xl.preferences.DisplayPreferences;

import javax.swing.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class ChromosomePositionScrollBar extends JScrollBar implements
        DisplayPreferencesListener, AdjustmentListener {

    private boolean ignoreInternal = false;

    public ChromosomePositionScrollBar() {
        super(JScrollBar.HORIZONTAL, 0, 250, 0, 10000);
        DisplayPreferences.getInstance().addListener(this);
        addAdjustmentListener(this);
    }

    public void displayPreferencesUpdated(DisplayPreferences prefs) {

        // The value of a scroll bar is always its start position. You can get
        // the end position by adding the extent.

        int startPoint = prefs.getCurrentStartLocation();
        double proportion = startPoint
                / (double) prefs.getCurrentChromosome().getLength();
        double extentProportion = prefs.getCurrentLength()
                / (double) prefs.getCurrentChromosome().getLength();
        int extent = (int) (10000 * extentProportion);
        int value = (int) (10000 * proportion);
        ignoreInternal = true;
        getModel().setRangeProperties(value, extent, 0, 10000, false);
        // System.out.println(this.getClass().getDisplayName()+":displayPreferencesUpdated()");
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
        // System.out.println(this.getClass().getDisplayName()+":adjustmentValueChanged()");
    }

}
