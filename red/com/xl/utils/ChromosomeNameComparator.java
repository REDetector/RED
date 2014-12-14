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

import java.util.Comparator;

public class ChromosomeNameComparator implements Comparator<String> {

    private static ChromosomeNameComparator instance;

    private ChromosomeNameComparator() {
    }

    public static ChromosomeNameComparator getInstance() {
        if (instance == null) {
            instance = new ChromosomeNameComparator();
        }
        return instance;
    }

    private boolean isMito(String chr) {
        return chr.equalsIgnoreCase("chrm") || chr.equalsIgnoreCase("mt");
    }

    /**
     * Return start/end (inclusive/exclusive) locations of first range in string which represent a digit.
     *
     * @param chr
     * @return
     */
    private int[] findDigitRange(String chr) {
        int[] locs = null;
        int loc = 0;
        for (char c : chr.toCharArray()) {
            if (Character.isDigit(c)) {
                if (locs == null) {
                    locs = new int[]{loc, chr.length()};
                }
            } else if (locs != null) {
                locs[1] = loc;
                break;
            }
            loc++;
        }
        return locs;
    }

    @Override
    public int compare(String chr0, String chr1) {
        int[] range0 = findDigitRange(chr0);
        int[] range1 = findDigitRange(chr1);

        if (range0 == null || range1 == null || range0[0] != range1[0]) {
            // Special rule -- put the mitochondria at the end
            boolean mito0 = isMito(chr0);
            boolean mito1 = isMito(chr1);
            if (mito0 && !mito1) {
                return +1;
            } else if (!mito0 && mito1) {
                return -1;
            } else if (mito0 && mito1) {
                return 0;
            }

            return chr0.compareToIgnoreCase(chr1);
        } else {
            String alpha1 = chr0.substring(0, range0[0]);
            String alpha2 = chr1.substring(0, range1[0]);
            int alphaCmp = alpha1.compareToIgnoreCase(alpha2);
            if (alphaCmp != 0) {
                return alphaCmp;
            } else {
                int dig1;
                int dig2;
                try {
                    dig1 = Integer.parseInt(chr0.substring(range0[0], range0[1]));
                    dig2 = Integer.parseInt(chr1.substring(range1[0], range1[1]));
                } catch (NumberFormatException e) {
                    // This can occur if numbers are too large for Long. In this
                    // case revert to alpha compare
                    return chr0.compareTo(chr1);
                }
                if (dig1 > dig2) {
                    return 1;
                } else if (dig1 < dig2) {
                    return -1;
                } else {
                    return compare(chr0.substring(range0[1]),
                            chr1.substring(range1[1]));
                }
            }
        }
    }

}
