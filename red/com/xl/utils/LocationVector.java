package com.xl.utils;
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

import com.xl.datatypes.sequence.Location;

import java.io.Serializable;

/**
 * This class implements something like a vector but for primitive longs
 *
 * @author andrewss
 */
public class LocationVector implements Serializable {

    private Location[] locations = new Location[1000];
    private int length = 0;
    private boolean trimmed = false;


    public synchronized void add(Location location) {
        if (locations.length == length) {
            makeLonger();
        }

        locations[length] = location;
        length++;
        trimmed = false;
    }

    public int length() {
        return length;
    }

    public void setValues(Location[] locations) {
        this.locations = locations;
        length = locations.length;
        trimmed = true;
    }

    public void clear() {
        locations = new Location[1000];
        length = 0;
    }

    public Location[] toArray() {
        if (!trimmed)
            trim();
        return locations;
    }

    /**
     * This method causes the vector to trim its current storage to the
     * actual set of values it's storing so that no extraneous storage
     * is being used.  It's only useful if we want to keep the vector
     * around after all of the reads have been added.
     */
    public void trim() {
        Location[] trimmedArray = new Location[length];
        for (int i = 0; i < trimmedArray.length; i++) {
            trimmedArray[i] = locations[i];
        }
        locations = trimmedArray;
        trimmed = true;
    }


    private void makeLonger() {
        int newLength = length + (length / 4);
        Location[] newArray = new Location[newLength];
        for (int i = 0; i < locations.length; i++) {
            newArray[i] = locations[i];
        }
        locations = newArray;
    }

}
