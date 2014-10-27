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

import com.xl.utils.Strand;
import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class SequenceRead is used in places where both the read
 * and chromsome need to passed together. Sequence Reads do not store their
 * chromosome by default to save memory
 */
public class SequenceRead extends Alignment {

    private byte[] readBases = null;
    private byte[] qualities = null;

    private List<AlignmentBlock> alignmentBlocks = null;

    public SequenceRead(SAMRecord record) {
        super(record.getReferenceName(), record.getAlignmentStart(), record.getAlignmentEnd(), record.getReadNegativeStrandFlag());
        this.readBases = record.getReadBases();
        this.qualities = record.getBaseQualities();
        this.alignmentBlocks = record.getAlignmentBlocks();
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
    public SequenceRead(String chr, int start, int end, Strand strand, byte[] readBases, byte[] qualities) {
        super(chr, start, end, strand);
        this.readBases = readBases;
        if (readBases != null) {
            if (qualities == null || qualities.length < readBases.length) {
                this.qualities = new byte[readBases.length];
                Arrays.fill(this.qualities, (byte) 255);
            } else {
                this.qualities = qualities;
            }
        }
    }

    public List<SmallPieceSequence> getAlignmentBlocks() {
        List<SmallPieceSequence> list = new ArrayList<SmallPieceSequence>(alignmentBlocks.size());
        for (AlignmentBlock block : alignmentBlocks) {
            list.add(new SmallPieceSequence(block.getReadStart(), block.getReferenceStart(), block.getLength(), readBases, qualities));
        }
        return list;
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


    public boolean contains(int position) {
        if (position > getStart() && position < getEnd()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if (getChr() == null) {
            return getStart() + "-" + getEnd();
        } else {
            return getChr() + ":" + getStart() + "-" + getEnd();
        }
    }

    public class SmallPieceSequence {
        private int referenceStart;
        private int length;
        private byte[] bases;
        private byte[] qualities;

        public SmallPieceSequence(int readStart, int referenceStart, int length, byte[] rawBases, byte[] rawQualities) {
            this.referenceStart = referenceStart;
            this.length = length;
            bases = Arrays.copyOfRange(rawBases, readStart - 1, readStart + length - 1);
            qualities = Arrays.copyOfRange(rawQualities, readStart - 1, readStart + length - 1);
        }

        public int getEnd() {
            return referenceStart + length;
        }

        public int getReferenceStart() {
            return referenceStart;
        }

        public byte[] getQualities() {
            return qualities;
        }

        public byte[] getBases() {
            return bases;
        }

    }

}
