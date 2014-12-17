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

import java.text.DecimalFormat;

/**
 * Format the position.
 */
public class PositionFormat {
    public static final String UNIT_BYTE = "b";
    public static final String UNIT_BASEPAIR = "bp";

    /**
     * Provides a nicely formatted version of a double string.  Similar to what you can do with Decimal Format, but I'm really picky!
     *
     * @param originalLength the length
     * @return A formatted string
     */
    public static String formatLength(long originalLength, String unitName) {

        double length = originalLength;

        String unit = " " + unitName;

        if (length >= 1000000) {
            length /= 1000000;
            unit = " M" + unitName;
        } else if (length >= 1000) {
            length /= 1000;
            unit = " K" + unitName;
        }

        return new DecimalFormat("#.##").format(length) + unit;
    }


}
