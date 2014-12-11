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

import com.xl.datatypes.sequence.Sequence;
import com.xl.utils.ChromosomeNameComparator;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Implementation of Sequence backed by an indexed fasta file
 *
 * @author Jim Robinson
 * @date 3/24/12
 */

public class FastaDirectorySequence implements Sequence {

    Map<String, FastaIndexedSequence> sequenceMap;
    List<String> chromosomeNames;
    Map<String, Integer> chrLengths;

    public FastaDirectorySequence(String directoryPath, String[] fastaFiles) throws IOException {
        readIndexes(directoryPath, fastaFiles);
    }

    private void readIndexes(String directoryPath, String[] fastaFiles) throws IOException {
        sequenceMap = new LinkedHashMap<String, FastaIndexedSequence>();
        for (String file : fastaFiles) {
            String fastaPath = directoryPath + File.separator + file;
            FastaIndexedSequence fastaSequence = new FastaIndexedSequence(fastaPath);
            for (String chr : fastaSequence.getChromosomeNames()) {
                sequenceMap.put(chr, fastaSequence);
            }
        }

        chromosomeNames = new ArrayList<String>();
        for (FastaIndexedSequence fastaSequence : getFastaSequences()) {
            chromosomeNames.addAll(fastaSequence.getChromosomeNames());
        }
        Collections.sort(chromosomeNames, ChromosomeNameComparator.getInstance());

        chrLengths = new HashMap<String, Integer>(chromosomeNames.size());
        for (FastaIndexedSequence fastaSequence : getFastaSequences()) {
            for (String chr : fastaSequence.getChromosomeNames()) {
                int length = fastaSequence.getChromosomeLength(chr);
                chrLengths.put(chr, length);
            }
        }

    }

    public Collection<FastaIndexedSequence> getFastaSequences() {
        return sequenceMap.values();
    }

    public byte[] getSequence(String chr, int start, int end) {
        if (!sequenceMap.containsKey(chr)) {
            return null;
        }
        return sequenceMap.get(chr).getSequence(chr, start, end);
    }

    @Override
    public List<String> getChromosomeNames() {
        return chromosomeNames;
    }

    @Override
    public byte getBase(String chr, int position) {
        throw new RuntimeException("getBase() is not implemented for class " + FastaDirectorySequence.class.getName());
    }

    @Override
    public int getChromosomeLength(String chrname) {
        return chrLengths.get(chrname);
    }
}