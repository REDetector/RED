package com.xl.utils;

/**
 * Copyright 2010-13 Simon Andrews
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

import java.awt.*;

/**
 * This is a static class of fixed colour values which provides a single
 * point of reference from where we can change colours throughout the
 * whole application.
 */
public class ColourScheme {
    /**
     * The software name in title panel
     */
    public static final Color SOFTWARE = new Color(220, 0, 0);

    /**
     * The software name in title panel
     */
    public static final Color VERSION = new Color(0, 0, 220);

    /**
     * Any feature or read on the top strand
     */
    public static final Color FORWARD_FEATURE = new Color(198, 43, 158);
    /**
     * The border around a selected chromosome in the genome view
     */
    public static final Color GENOME_SELECTED_CHROMOSOME = FORWARD_FEATURE;
    /**
     * The list colour for datasets
     */
    public static final Color DATASET_LIST = FORWARD_FEATURE;
    /**
     * Any feature or read on the bottom strand
     */
    public static final Color REVERSE_FEATURE = new Color(19, 91, 28);
    /**
     * The list colour for data groups
     */
    public static final Color DATAGROUP_LIST = REVERSE_FEATURE;
    /**
     * Any feature or read without an assigned strand
     */
    public static final Color UNKNOWN_FEATURE = Color.GRAY;
    /**
     * An active feature or read
     */
    public static final Color ACTIVE_FEATURE = Color.YELLOW;
    /**
     * The standard color of base 'A' defined by ucsc.
     */
    public static final Color BASE_A = Color.GREEN;
    /**
     * The standard color of base 'G' defined by ucsc.
     */
    public static final Color BASE_G = Color.BLACK;
    /**
     * The standard color of base 'T' defined by ucsc.
     */
    public static final Color BASE_T = Color.RED;
    /**
     * The standard color of base 'C' defined by ucsc.
     */
    public static final Color BASE_C = Color.BLUE;
    public static final Color BASE_UNKNOWN = Color.DARK_GRAY;
    /**
     * A feature at the same position as the active feature
     */
    public static final Color ACTIVE_FEATURE_MATCH = new Color(0, 180, 0);
    /**
     * The list colour for quantitations requiring HiC data *
     */
    public static final Color HIC_QUANTITATION = ACTIVE_FEATURE_MATCH;
    /**
     * The background to odd numbered feature tracks
     */
    public static final Color FEATURE_BACKGROUND_ODD = new Color(220, 220, 255);
    /**
     * The background to even numbered feature tracks
     */
    public static final Color FEATURE_BACKGROUND_EVEN = new Color(180, 180, 255);
    /**
     * The background colour for a selection in progress
     */
    public static final Color DRAGGED_SELECTION = new Color(100, 255, 100);
    /**
     * The background to odd numbered data tracks
     */
    public static final Color DATA_BACKGROUND_ODD = new Color(230, 230, 230);
    /**
     * The background to even numbered data tracks
     */
    public static final Color DATA_BACKGROUND_EVEN = new Color(255, 255, 255);
    /**
     * The fill colour for histogram bars
     */
    public static final Color HISTOGRAM_BAR = Color.BLUE;
    /**
     * The fill colour for histogram bars
     */
    public static final Color HIGHLIGHTED_HISTOGRAM_BAR = Color.RED;
    /**
     * The Chromosome colour in the genome view
     */
    public static final Color GENOME_CHROMOSOME = new Color(100, 100, 255);
    /**
     * The selected region in the genome view
     */
    public static final Color GENOME_SELECTED = new Color(220, 0, 0);

    public static Color getBaseColor(char c) {
        if (c == 'a' || c == 'A') {
            return ColourScheme.BASE_A;
        } else if (c == 'g' || c == 'G') {
            return ColourScheme.BASE_G;
        } else if (c == 't' || c == 'T') {
            return ColourScheme.BASE_T;
        } else if (c == 'c' || c == 'C') {
            return ColourScheme.BASE_C;
        } else {
            return ColourScheme.BASE_UNKNOWN;
        }
    }

}
