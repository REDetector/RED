package com.xl.datatypes.sequence;

import java.io.Serializable;

/**
 * A simple class to represent a simple location in the genome. For complex
 * positions containing sublocations you should use the SplitLocation class
 * instead.
 */
public class Location implements Serializable, Comparable<Location> {
    int start;
    int end;

    public Location(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * The start position of this location. Guaranteed to not be higher than the
     * end position
     *
     * @return The start position
     */
    public int getStart() {
        return start;
    }

    /**
     * The end position of this location. Guaranteed to be the same or higher
     * than the start position.
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
