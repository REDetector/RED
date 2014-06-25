package com.xl.utils;

import com.xl.datatypes.sequence.SequenceRead;

import java.io.Serializable;

public class SequenceVector implements Serializable {

    private SequenceRead[] locations = new SequenceRead[1000];
    private int length = 0;
    private boolean trimmed = false;


    public synchronized void add(SequenceRead sequence) {
        if (locations.length == length) {
            makeLonger();
        }

        locations[length] = sequence;
        length++;
        trimmed = false;
    }

    public int length() {
        return length;
    }

    public void setValues(SequenceRead[] locations) {
        this.locations = locations;
        length = locations.length;
        trimmed = true;
    }

    public void clear() {
        locations = new SequenceRead[1000];
        length = 0;
    }

    public SequenceRead[] toArray() {
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
        SequenceRead[] trimmedArray = new SequenceRead[length];
        for (int i = 0; i < trimmedArray.length; i++) {
            trimmedArray[i] = locations[i];
        }
        locations = trimmedArray;
        trimmed = true;
    }


    private void makeLonger() {
        int newLength = length + (length / 4);
        SequenceRead[] newArray = new SequenceRead[newLength];
        for (int i = 0; i < locations.length; i++) {
            newArray[i] = locations[i];
        }
        locations = newArray;
    }

}
