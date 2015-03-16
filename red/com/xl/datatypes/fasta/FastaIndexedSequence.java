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

package com.xl.datatypes.fasta;

import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.sequence.Sequence;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.FileUtils;
import net.sf.samtools.seekablestream.SeekableFileStream;
import net.sf.samtools.seekablestream.SeekableStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Sequence backed by an indexed fasta file
 *
 * @author jrobinso
 * @date 8/7/11
 */
public class FastaIndexedSequence implements Sequence {

    private final ArrayList<String> chromoNamesList;
    private FastaIndex index = null;
    private String path = null;

    public FastaIndexedSequence(String path) throws IOException {
        String indexFileName = FileUtils.getFileNameFromURL(path);
        File currentGenome = new File(LocationPreferences.getInstance().getFastaDirectory() + File.separator +
                GenomeDescriptor.getInstance().getDisplayName() + File.separator + indexFileName + ".fai");
        String indexPath;
        if (currentGenome.exists()) {
            indexPath = currentGenome.getPath();
        } else {
            indexPath = path + ".fai";
        }
        index = new FastaIndex(indexPath);
        chromoNamesList = new ArrayList<String>(index.getSequenceNames());
    }

    /**
     * Return the sequence for the query interval as a byte array. Coordinates are "ucsc" style (0 based)
     * <p/>
     * Example: 5 bases per line, 6 bytes per line
     * <p/>
     * Bases 0 1 2 3 4 * | 5 6 7 8 9 * | 10 11 12 13 14 * etc Offset 0 1 2 3 4 0 1 2 3 4 0 1 2 3 4 Bytes 0 1 2 3 4 5 | 6 7 8 9 10 | 11 12 13 14 15 16
     * <p/>
     * query 9 - 13 start line = 1 base0 = 1*5 = 5 offset = (9 - 5) = 4 start byte = (1*6) + 3 = 10 end line = 2
     *
     * @param chr the chr
     * @param qstart the start
     * @param qend the end
     * @return
     */

    public byte[] getSequence(String chr, int qstart, int qend) {

        FastaIndex.FastaSequenceIndexEntry idxEntry = index.getIndexEntry(chr);
        if (idxEntry == null) {
            return null;
        }

        try {

            final int start = Math.max(0, qstart); // qstart should never be < 0
            final int end = Math.min((int) idxEntry.getSize(), qend);

            final int bytesPerLine = idxEntry.getBytesPerLine();
            final int basesPerLine = idxEntry.getBasesPerLine();
            int nEndBytes = bytesPerLine - basesPerLine;

            int startLine = start / basesPerLine;
            int endLine = end / basesPerLine;

            int base0 = startLine * basesPerLine; // Base at beginning of start
            // line

            int offset = start - base0;
            final long position = idxEntry.getPosition();
            long startByte = position + startLine * bytesPerLine + offset;

            int base1 = endLine * basesPerLine;
            int offset1 = end - base1;
            long endByte = position + endLine * bytesPerLine + offset1;

            if (startByte >= endByte) {
                return null;
            }

            // Read all the bytes in the range. This will include endline
            // characters
            byte[] allBytes = readBytes(startByte, endByte);

            // Create the array for the sequence -- this will be "allBytes"
            // without the endline characters.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(end - start);

            int srcPos = 0;
            // int desPos = 0;
            // Copy first line
            final int allBytesLength = allBytes.length;
            if (offset > 0) {
                int nBases = Math.min(end - start, basesPerLine - offset);
                bos.write(allBytes, srcPos, nBases);
                srcPos += (nBases + nEndBytes);
                // desPos += nBases;
            }

            while (srcPos < allBytesLength) {
                int nBases = Math.min(basesPerLine, allBytesLength - srcPos);
                bos.write(allBytes, srcPos, nBases);
                srcPos += (nBases + nEndBytes);
                // desPos += nBases;
            }

            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
            return null;
        }
    }

    @Override
    public byte getBase(String chr, int position) {
        throw new RuntimeException("getBase() is not implemented for class "
                + FastaIndexedSequence.class.getName());
    }

    @Override
    public List<String> getChromosomeNames() {
        return chromoNamesList;
    }

    @Override
    public int getChromosomeLength(String chrname) {
        return index.getSequenceSize(chrname);
    }

    /**
     * Read the bytes between file position posStart and posEnd
     *
     * @throws IOException
     */
    private byte[] readBytes(long posStart, long posEnd) throws IOException {

        SeekableStream ss = null;
        try {
            ss = new SeekableFileStream(new File(path));
            int nBytes = (int) (posEnd - posStart);
            byte[] bytes = new byte[nBytes];
            ss.seek(posStart);
            ss.read(bytes);
            return bytes;
        } finally {
            if (ss != null) {
                ss.close();
            }
        }
    }
}