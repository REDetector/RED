package com.xl.datatypes.sequence;

import com.xl.utils.Strand;

import java.util.Arrays;

/**
 * The Class SequenceReadWithChromosome is used in places where both the read
 * and chromsome need to passed together. Sequence Reads do not store their
 * chromosome by default to save memory
 */
public class SequenceRead implements Location {

    /**
     * This class is only to be used by data parsers which temporarily need to
     * associate a sequence read with a chromosome in a single object. All of
     * the main classes use the SequenceRead object which doesn't store the
     * chromosome to save memory.
     */

    private String chr = null;

    private Strand strand = null;
    private int start = 0;
    private byte[] readBases = null;
    private int length = -1;
    private byte[] qualities = null;
    private short[] counts = null;

    public SequenceRead(int start, int end) {
        this(null, start, end);
    }

    public SequenceRead(String chr, int start, int end) {
        this(chr, start, null, null, null);
        this.length = end - start;
    }

    /**
     * Instantiates a new sequence read with chromosome.
     *
     * @param chr       the chromosome
     * @param start
     * @param strand
     * @param readBases
     * @param qualities
     */
    public SequenceRead(String chr, int start, Strand strand, byte[] readBases,
                        byte[] qualities) {
        this.chr = chr;
        this.start = start;
        this.strand = strand;
        this.readBases = readBases;
        if (readBases != null) {
            this.length = readBases.length;
            if (qualities == null || qualities.length < readBases.length) {
                this.qualities = new byte[readBases.length];
                Arrays.fill(this.qualities, (byte) 126);
            } else {
                this.qualities = qualities;
            }
        }
    }

    public Strand getStrand() {
        return strand;
    }

    public byte[] getReadBases() {
        return readBases;
    }

    public byte getReadBaseAt(int index) {
        return readBases[index];
    }

    public byte[] getQualities() {
        return qualities;
    }

    public byte getQuality(int index) {
        return qualities[index];
    }

    public short[] getCounts() {
        return counts;
    }

    public void setCounts(short[] counts) {
        this.counts = counts;
    }

    public short getCount(int index) {
        return counts[index];
    }

    public boolean contains(int position) {
        if (position > getStart() && position < getEnd()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasCounts() {
        return counts != null;
    }

    @Override
    public String toString() {
        if (getChr() == null) {
            return getStart() + "-" + getEnd();
        } else {
            return getChr() + ":" + getStart() + "-" + getEnd();
        }
    }

    @Override
    public int compareTo(Location o) {
        if (getStart() > o.getStart()) {
            return 1;
        } else if (getStart() < o.getStart()) {
            return -1;
        } else {
            return 0;
        }
    }

    public String toWrite() {
        return getStart() + "\t" + Strand.parseStrand(strand) + "\t"
                + new String(readBases);
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return start + length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String getChr() {
        return chr;
    }
}
