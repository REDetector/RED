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

package com.xl.preferences;

import com.xl.datatypes.genome.Chromosome;
import com.xl.display.dialog.gotodialog.GoToDialog;
import com.xl.gradients.*;
import com.xl.interfaces.DisplayPreferencesListener;
import com.xl.main.REDApplication;
import com.xl.utils.ParsingUtils;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class is intended to be a single point at which all of the major display preferences can be stored and from which changes can be passed to any views
 * which care.
 * <p/>
 * This will cover any visual changes to the display, so colours, types of graph, scales would all fall into this object.
 */
public class DisplayPreferences {
    /**
     * The set of constants for the colour type being used.
     */
    public static final int COLOUR_TYPE_FIXED = 1001;
    public static final int COLOUR_TYPE_INDEXED = 12;
    public static final int COLOUR_TYPE_GRADIENT = 11;
    /**
     * The set of constants for the display mode.
     */
    /*
     * Some of these values are carried over from an older implementation hence the somewhat odd numbering
	 */
    public static final int DISPLAY_MODE_READS_AND_PROBES = 1;
    public static final int DISPLAY_MODE_READS_ONLY = 2;
    public static final int DISPLAY_MODE_PROBES_ONLY = 3;
    /**
     * The options for which gradient to use.
     */
    public static final int GRADIENT_HOT_COLD = 2001;
    public static final int GRADIENT_RED_GREEN = 2002;
    public static final int GRADIENT_GREYSCALE = 2003;
    public static final int GRADIENT_MAGENTA_GREEN = 2004;
    public static final int GRADIENT_RED_WHITE = 2005;
    /**
     * The options for the type of graph drawn *
     */
    public static final int GRAPH_TYPE_BAR = 3001;
    public static final int GRAPH_TYPE_LINE = 3002;
    public static final int GRAPH_TYPE_POINT = 3003;
    /**
     * The single instance of this class.
     */
    private static DisplayPreferences instance = new DisplayPreferences();
    private int currentColourType = COLOUR_TYPE_GRADIENT;
    private int currentDisplayMode = DISPLAY_MODE_READS_ONLY;
    private int currentGradientValue = GRADIENT_HOT_COLD;
    private int currentGraphType = GRAPH_TYPE_BAR;
    /**
     * The listener.
     */
    private Vector<DisplayPreferencesListener> listeners = new Vector<DisplayPreferencesListener>();
    /**
     * The colour gradient.
     */
    private ColourGradient currentGradient = new HotColdColourGradient();
    /**
     * The currently visible chromosome *
     */
    private Chromosome currentChromosome = null;
    /**
     * Start location in chromosome view.
     */
    private int currentStartLocation = 0;
    /**
     * End location in chromosome view.
     */
    private int currentEndLocation = 0;
    /**
     * Is fasta file available.
     */
    private boolean fastaEnable = false;

    /**
     * We make this a singleton so it's only accessible by a static method *
     */
    private DisplayPreferences() {
    }

    public static DisplayPreferences getInstance() {
        return instance;
    }

    public void reset() {
        listeners.removeAllElements();
        currentColourType = COLOUR_TYPE_GRADIENT;
        currentDisplayMode = DISPLAY_MODE_READS_ONLY;
        currentGradient = new HotColdColourGradient();
        currentGradientValue = GRADIENT_HOT_COLD;
        currentGraphType = GRAPH_TYPE_BAR;
    }

    /* We allow views to listen for changes */
    public void addListener(DisplayPreferencesListener displayPreferencesListener) {
        if (displayPreferencesListener != null && !listeners.contains(displayPreferencesListener))
            listeners.add(displayPreferencesListener);
    }

    public void removeListener(DisplayPreferencesListener displayPreferencesListener) {
        if (displayPreferencesListener != null && listeners.contains(displayPreferencesListener))
            listeners.remove(displayPreferencesListener);
    }

    private void optionsChanged() {
        Enumeration<DisplayPreferencesListener> en = listeners.elements();

        while (en.hasMoreElements()) {
            en.nextElement().displayPreferencesUpdated(this);
        }
    }

    /* The display mode */
    public int getDisplayMode() {
        return currentDisplayMode;
    }

    public void setDisplayMode(int displayMode) {
        if (equalsAny(new int[]{DISPLAY_MODE_PROBES_ONLY, DISPLAY_MODE_READS_AND_PROBES, DISPLAY_MODE_READS_ONLY}, displayMode)) {
            currentDisplayMode = displayMode;
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + displayMode + " is not a valid display mode");
        }
    }

    public int getCurrentStartLocation() {
        return currentStartLocation;
    }

    public int getCurrentEndLocation() {
        return currentEndLocation;
    }

    public int getCurrentMidPoint() {
        return (currentEndLocation + currentStartLocation) / 2;
    }

    public int getCurrentLength() {
        return currentEndLocation - currentStartLocation + 1;
    }

    /* The colour type */
    public int getColourType() {
        return currentColourType;
    }

    public void setColourType(int colourType) {
        if (equalsAny(new int[]{COLOUR_TYPE_FIXED, COLOUR_TYPE_GRADIENT, COLOUR_TYPE_INDEXED}, colourType)) {
            currentColourType = colourType;
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + colourType + " is not a valid colour type");
        }
    }

    public void setLocation(int start, int end) {
        this.currentStartLocation = start;
        this.currentEndLocation = end;
        GoToDialog.addRecentLocation(currentChromosome.getName(), start, end);
        optionsChanged();
    }

    public void setLocation(String chr, int start, int end) {
        currentChromosome = REDApplication.getInstance().dataCollection().genome().getChromosome(chr);
        setLocation(start, end);
    }

    public void setLocation(Chromosome c, int start, int end) {
        currentChromosome = c;
        setLocation(start, end);
    }

    /* The chromosome */
    public Chromosome getCurrentChromosome() {
        return currentChromosome;
    }

    public void setChromosome(Chromosome c) {
        currentChromosome = c;
        // Set the location to be a 1Mbp chunk in the middle if we can
        if (currentChromosome != null && (currentStartLocation == 0 || currentEndLocation == 0)) {
            setLocation(currentChromosome.getLength() / 16 * 7, currentChromosome.getLength() / 16 * 9);
        }
    }

    /* The gradient */
    public ColourGradient getGradient() {
        return currentGradient;
    }

    public void setGradient(int gradientType) {
        if (equalsAny(new int[]{GRADIENT_GREYSCALE, GRADIENT_HOT_COLD, GRADIENT_RED_GREEN, GRADIENT_MAGENTA_GREEN, GRADIENT_RED_WHITE}, gradientType)) {

            currentGradientValue = gradientType;

            switch (gradientType) {

                case GRADIENT_GREYSCALE:
                    currentGradient = new GreyscaleColourGradient();
                    break;
                case GRADIENT_HOT_COLD:
                    currentGradient = new HotColdColourGradient();
                    break;
                case GRADIENT_RED_GREEN:
                    currentGradient = new RedGreenColourGradient();
                    break;
                case GRADIENT_MAGENTA_GREEN:
                    currentGradient = new MagentaGreenColourGradient();
                    break;
                case GRADIENT_RED_WHITE:
                    currentGradient = new RedWhiteColourGradient();
                    break;
            }
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + gradientType + " is not a valid gradient type");
        }
    }

    public boolean isFastaEnable() {
        return fastaEnable;
    }

    public void setFastaEnable(boolean fastaEnable) {
        this.fastaEnable = fastaEnable;
    }

    public int getGradientValue() {
        return currentGradientValue;
    }

    /* The graph type */
    public int getGraphType() {
        return currentGraphType;
    }

    public void setGraphType(int graphType) {
        if (equalsAny(new int[]{
                GRAPH_TYPE_BAR, GRAPH_TYPE_LINE, GRAPH_TYPE_POINT
        }, graphType)) {
            currentGraphType = graphType;
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + graphType + " is not a valid graph type");
        }
    }

    public void writeConfiguration(PrintStream p) {
        // Make sure this number at the end equates to the number of configuration lines to be written
        p.println(ParsingUtils.DISPLAY_PREFERENCES + "\t5");

        p.println("DisplayMode\t" + getDisplayMode());

        p.println("Gradient\t" + getGradientValue());

        p.println("GraphType\t" + getGraphType());

        p.println("Fasta\t" + fastaEnable);

        p.println("CurrentView\t" + currentChromosome.getName() + "\t" + currentStartLocation + "\t" + currentEndLocation);
    }

    private boolean equalsAny(int[] valids, int test) {
        for (int valid : valids) {
            if (test == valid)
                return true;
        }
        return false;
    }


}
