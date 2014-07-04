package com.xl.preferences;

import com.xl.datatypes.genome.Chromosome;
import com.xl.dialog.gotodialog.GotoDialog;
import com.xl.gradients.*;
import com.xl.interfaces.DisplayPreferencesListener;
import com.xl.main.REDApplication;
import com.xl.utils.ParsingUtils;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class is intended to be a single point at which all of the major display
 * preferences can be stored and from which changes can be passed to any views
 * which care.
 * <p/>
 * This will cover any visual changes to the display, so colours, types of
 * graph, scales would all fall into this object.
 */
public class DisplayPreferences {

    /**
     * The set of constants for the colour type being used *
     */
    /*
     * Some of these values are carried over from an older implementation hence
	 * the somewhat odd numbering
	 */
    public static final int COLOUR_TYPE_FIXED = 1001;
    public static final int COLOUR_TYPE_INDEXED = 12;
    public static final int COLOUR_TYPE_GRADIENT = 11;
    private int currentColourType = COLOUR_TYPE_GRADIENT;
    /**
     * The set of constants for the read density *
     */
    /*
     * Some of these values are carried over from an older implementation hence
	 * the somewhat odd numbering
	 */
    public static final int READ_DENSITY_LOW = 6;
    private int currentReadDensity = READ_DENSITY_LOW;
    public static final int READ_DENSITY_MEDIUM = 7;
    public static final int READ_DENSITY_HIGH = 8;
    /**
     * The set of constants for the display mode *
     */
    /*
     * Some of these values are carried over from an older implementation hence
	 * the somewhat odd numbering
	 */
    public static final int DISPLAY_MODE_READS_AND_QUANTITATION = 1;
    public static final int DISPLAY_MODE_READS_ONLY = 2;
    private int currentDisplayMode = DISPLAY_MODE_READS_ONLY;
    public static final int DISPLAY_MODE_QUANTITATION_ONLY = 3;
    /**
     * The options for which gradient to use *
     */
    public static final int GRADIENT_HOT_COLD = 2001;
    private int currentGradientValue = GRADIENT_HOT_COLD;
    public static final int GRADIENT_RED_GREEN = 2002;
    public static final int GRADIENT_GREYSCALE = 2003;
    public static final int GRADIENT_MAGENTA_GREEN = 2004;
    public static final int GRADIENT_RED_WHITE = 2005;
    /**
     * The options for the type of graph drawn *
     */
    public static final int GRAPH_TYPE_BAR = 3001;
    private int currentGraphType = GRAPH_TYPE_BAR;
    public static final int GRAPH_TYPE_LINE = 3002;
    public static final int GRAPH_TYPE_POINT = 3003;
    /**
     * The options for the scale used *
     */
    /*
     * Some of these values are carried over from an older implementation hence
	 * the somewhat odd numbering
	 */
    public static final int SCALE_TYPE_POSITIVE = 4;
    public static final int SCALE_TYPE_POSITIVE_AND_NEGATIVE = 5;
    private int currentScaleType = SCALE_TYPE_POSITIVE_AND_NEGATIVE;
    /**
     * The options for the read display *
     */
    /*
	 * Some of these values are carried over from an older implementation hence
	 * the somewhat odd numbering
	 */
    public static final int READ_DISPLAY_COMBINED = 9;
    public static final int READ_DISPLAY_SEPARATED = 10;
    private int currentReadDisplay = READ_DISPLAY_SEPARATED;
    /**
     * The single instance of this class *
     */
    private static DisplayPreferences instance = new DisplayPreferences();
    private Vector<DisplayPreferencesListener> listeners = new Vector<DisplayPreferencesListener>();
    private ColourGradient currentGradient = new HotColdColourGradient();
    /**
     * The Data zoom level *
     */
    private double maxDataValue = 1;

    /**
     * The currently visible chromosome *
     */
    private Chromosome currentChromosome = null;

    private int currentStartLocation = 0;
    private int currentEndLocation = 0;


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
        currentReadDensity = READ_DENSITY_LOW;
        currentReadDisplay = READ_DISPLAY_COMBINED;
        currentScaleType = SCALE_TYPE_POSITIVE_AND_NEGATIVE;
    }

    /* We allow views to listen for changes */
    public void addListener(
            DisplayPreferencesListener displayPreferencesListener) {
        if (displayPreferencesListener != null
                && !listeners.contains(displayPreferencesListener))
            listeners.add(displayPreferencesListener);
    }

    public void removeListener(
            DisplayPreferencesListener displayPreferencesListener) {
        if (displayPreferencesListener != null
                && listeners.contains(displayPreferencesListener))
            listeners.remove(displayPreferencesListener);
    }

    private void optionsChanged() {
        Enumeration<DisplayPreferencesListener> en = listeners.elements();

        while (en.hasMoreElements()) {
            en.nextElement().displayPreferencesUpdated(this);
        }
    }

    /* The max data value */
    public double getMaxDataValue() {
        return maxDataValue;
    }

    public void setMaxDataValue(double value) {
        if (value < 1)
            value = 1;
        if (value > Math.pow(2, 20))
            value = Math.pow(2, 20);
        maxDataValue = value;
        optionsChanged();
    }

    /* The display mode */
    public int getDisplayMode() {
        return currentDisplayMode;
    }

    public void setDisplayMode(int displayMode) {
        if (equalsAny(new int[]{DISPLAY_MODE_QUANTITATION_ONLY,
                        DISPLAY_MODE_READS_AND_QUANTITATION, DISPLAY_MODE_READS_ONLY},
                displayMode)) {
            currentDisplayMode = displayMode;
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + displayMode
                    + " is not a valid display mode");
        }
    }

    public int getCurrentStartLocation() {
        return currentStartLocation;
    }

    public int getCurrentEndLocation() {
        return currentEndLocation;
    }

    public int getCurrentLength() {
        return currentEndLocation - currentStartLocation;
    }

    /* The colour type */
    public int getColourType() {
        return currentColourType;
    }

    public void setColourType(int colourType) {
        if (equalsAny(new int[]{COLOUR_TYPE_FIXED, COLOUR_TYPE_GRADIENT,
                COLOUR_TYPE_INDEXED}, colourType)) {
            currentColourType = colourType;
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + colourType
                    + " is not a valid colour type");
        }
    }

    /* The read density */
    public int getReadDensity() {
        return currentReadDensity;
    }

    public void setReadDensity(int readDensity) {
        if (equalsAny(new int[]{READ_DENSITY_LOW, READ_DENSITY_MEDIUM,
                READ_DENSITY_HIGH}, readDensity)) {
            currentReadDensity = readDensity;
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + readDensity
                    + " is not a valid read density");
        }
    }

    /* The read display */
    public int getReadDisplay() {
        return currentReadDisplay;
    }

    public void setReadDisplay(int readDisplay) {
        if (equalsAny(
                new int[]{READ_DISPLAY_COMBINED, READ_DISPLAY_SEPARATED},
                readDisplay)) {
            currentReadDisplay = readDisplay;
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + readDisplay
                    + " is not a valid read display");
        }
    }

    public void setLocation(int start, int end) {
        this.currentStartLocation = start;
        this.currentEndLocation = end;
        GotoDialog.addRecentLocation(currentChromosome.getName(), start, end);
        optionsChanged();
    }

    public void setLocation(String chr, int start, int end) {
        currentChromosome = REDApplication.getInstance().dataCollection()
                .genome().getChromosome(chr);
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
            this.currentStartLocation = currentChromosome.getLength() / 16 * 7;
            this.currentEndLocation = currentChromosome.getLength() / 16 * 9;
            optionsChanged();
        }
    }

    /* The gradient */
    public ColourGradient getGradient() {
        return currentGradient;
    }

    public void setGradient(int gradientType) {
        if (equalsAny(
                new int[]{GRADIENT_GREYSCALE, GRADIENT_HOT_COLD,
                        GRADIENT_RED_GREEN, GRADIENT_MAGENTA_GREEN,
                        GRADIENT_RED_WHITE}, gradientType)) {

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
            throw new IllegalArgumentException("Value " + gradientType
                    + " is not a valid gradient type");
        }
    }

    public int getGradientValue() {
        return currentGradientValue;
    }

    /* The graph type */
    public int getGraphType() {
        return currentGraphType;
    }

    public void setGraphType(int graphType) {
        if (equalsAny(new int[]{GRAPH_TYPE_BAR, GRAPH_TYPE_LINE,
                GRAPH_TYPE_POINT}, graphType)) {
            currentGraphType = graphType;
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + graphType
                    + " is not a valid graph type");
        }
    }

    /* The scale type */
    public int getScaleType() {
        return currentScaleType;
    }

    public void setScaleType(int scaleType) {
        if (equalsAny(new int[]{SCALE_TYPE_POSITIVE,
                SCALE_TYPE_POSITIVE_AND_NEGATIVE}, scaleType)) {
            currentScaleType = scaleType;
            optionsChanged();
        } else {
            throw new IllegalArgumentException("Value " + scaleType
                    + " is not a valid scale type");
        }
    }

    public void writeConfiguration(PrintStream p) {
        // Make sure this number at the end equates to the number of
        // configuration lines to be written
        p.println(ParsingUtils.DISPLAY_PREFERENCES + "\t9");

        p.println("DataZoom\t" + getMaxDataValue());

        p.println("ScaleMode\t" + getScaleType());

        p.println("DisplayMode\t" + getDisplayMode());

        p.println("ReadDensity\t" + getReadDensity());

        p.println("SplitMode\t" + getReadDisplay());

        p.println("QuantitationColour\t" + getColourType());

        p.println("Gradient\t" + getGradientValue());

        p.println("GraphType\t" + getGraphType());

        p.println("CurrentView\t" + currentChromosome.getName() + "\t"
                + currentStartLocation + "\t" + currentEndLocation);
    }

    private boolean equalsAny(int[] valid, int test) {
        for (int i = 0; i < valid.length; i++) {
            if (test == valid[i])
                return true;
        }
        return false;
    }

}
