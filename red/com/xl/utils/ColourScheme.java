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

package com.xl.utils;

import java.awt.*;

/**
 * This is a static class of fixed colour values which provides a single point of reference from where we can change colours throughout the whole application.
 */
public class ColourScheme {
    /**
     * The software name in title panel.
     */
    public static final Color PROGRAM_NAME = new Color(220, 0, 0);

    /**
     * The software name in title panel.
     */
    public static final Color PROGRAM_VERSION = new Color(0, 0, 220);

    /**
     * Any feature or read on the top strand.
     */
    public static final Color FEATURE_TRACK = Color.BLUE;

    /**
     * The track name on chromosome view.
     */
    public static final Color TRACK_NAME = Color.GRAY;

    /**
     * An active feature or read
     */
    public static final Color ACTIVE_FEATURE = Color.YELLOW;
    public static final Color ACTIVE_READ = ACTIVE_FEATURE;
    /**
     * The border around a selected chromosome in the genome view
     */
    public static final Color GENOME_SELECTED_CHROMOSOME = new Color(220, 0, 0);
    /**
     * The list colour for data sets
     */
    public static final Color DATASET_LIST = new Color(220, 0, 0);
    /**
     * The list colour for data groups
     */
    public static final Color DATAGROUP_LIST = new Color(0, 0, 220);
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
    /**
     * The background to odd numbered data tracks
     */
    public static final Color DATA_BACKGROUND_ODD = new Color(230, 196, 192);
    /**
     * The background to even numbered data tracks
     */
    public static final Color DATA_BACKGROUND_EVEN = new Color(204, 207, 255);
    /**
     * Shown with block status when the chromosome viewer length is larger than screen pixel.
     */
    public static final Color DATA_TRACK = Color.GRAY;
    /**
     * The background colour for a selection in progress
     */
    public static final Color DRAGGED_SELECTION = new Color(100, 255, 100);

    /**
     * The fill colour for histogram bars
     */
    public static final Color HISTOGRAM_BAR = Color.BLUE;
    /**
     * The fill colour for histogram bars
     */
    public static final Color HIGHLIGHTED_HISTOGRAM_BAR = Color.YELLOW;
    /**
     * The Chromosome colour in the genome view
     */
    public static final Color GENOME_CHROMOSOME = new Color(100, 100, 255);
    /**
     * The selected region in the genome view
     */
    public static final Color GENOME_SELECTED = new Color(220, 0, 0);

    public static final Color READ_INTEVAL = Color.GRAY;

    public static Color getBaseColor(char c) {
        if (c == 'a' || c == 'A') {
            return BASE_A;
        } else if (c == 'g' || c == 'G') {
            return BASE_G;
        } else if (c == 't' || c == 'T') {
            return BASE_T;
        } else if (c == 'c' || c == 'C') {
            return BASE_C;
        } else {
            return DATA_TRACK;
        }
    }

}
