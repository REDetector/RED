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

public enum Strand {
    NONE, POSITIVE, NEGATIVE;

    public static String parseStrand(Strand strand) {
        switch (strand) {
            case POSITIVE:
                return "+";
            case NEGATIVE:
                return "-";
            case NONE:
                return ".";
            default:
                return ".";
        }
    }

    public static Strand parseStrand(String string) {
        if (string.equals("+")) {
            return Strand.POSITIVE;
        } else if (string.equals("-")) {
            return Strand.NEGATIVE;
        } else if (string.equals(".")) {
            return Strand.NONE;
        } else {
            throw new IllegalArgumentException(
                    "Wrong string received. The parse string muse be '+', '-' or '.'");
        }
    }
}
