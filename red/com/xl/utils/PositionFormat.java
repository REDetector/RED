/**
 * Copyright 2011-13 Simon Andrews
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
package com.xl.utils;

import java.text.DecimalFormat;

public class PositionFormat {
    public static final String UNIT_BYTE = "b";
    public static final String UNIT_BASEPAIR = "bp";

    /**
     * Provides a nicely formatted version of a double string.  Similar to
     * what you can do with Decimal Format, but I'm really picky!
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
//        String rawLength = "" + length;
//        char[] chars = rawLength.toCharArray();
//
//        int lastIndex = 0;
//
//        // Go through until we find a dot (if there is one)
//        for (int i = 0; i < chars.length; i++) {
//            lastIndex = i;
//            if (chars[i] == '.') break;
//        }
//
//        // We keep the next char as well if they are non
//        // zero numbers
//
//        if (lastIndex + 1 < chars.length && chars[lastIndex + 1] != '0') {
//            lastIndex += 1;
//        } else if (lastIndex > 0 && chars[lastIndex] == '.') {
//            lastIndex -= 1; // Lose the dot if its the last character
//        }
//
//        char[] finalChars = new char[lastIndex + 1];
//        for (int i = 0; i <= lastIndex; i++) {
//            finalChars[i] = chars[i];
//        }
//
//        return new String(finalChars) + unit;
    }


}
