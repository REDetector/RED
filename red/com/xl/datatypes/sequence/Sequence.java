package com.xl.datatypes.sequence;

import java.util.List;

/**
 * @author jrobinso
 * @date 8/8/11
 * <p/>
 * Represents a reference sequence.
 */
public interface Sequence {

    byte[] getSequence(String chr, int start, int end);

    public byte getBase(String chr, int position);

    List<String> getChromosomeNames();

    int getChromosomeLength(String chrname);
}