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

package com.xl.datatypes.sequence;

import java.io.Serializable;

/**
 * A simple class to represent a simple location.
 */
public class Location implements Serializable, Comparable<Location> {
    int start;
    int end;

    /**
     * A location.
     *
     * @param start The start.
     * @param end   The end.
     */
    public Location(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * The start position of this location. Guaranteed to not be higher than the end position
     *
     * @return The start position
     */
    public int getStart() {
        return start;
    }

    /**
     * The end position of this location. Guaranteed to be the same or higher than the start position.
     *
     * @return The end position
     */
    public int getEnd() {
        return end;
    }


    @Override
    public int compareTo(Location o) {
        if (o == null) {
            return 0;
        }
        if (getStart() > o.getStart()) {
            return 1;
        } else if (getStart() < o.getStart()) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return start + "-" + end;
    }
}
