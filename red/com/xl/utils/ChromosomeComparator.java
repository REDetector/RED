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

import com.xl.datatypes.genome.Chromosome;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Chromosome comparator.
 */
public class ChromosomeComparator implements Comparator<Chromosome> {
    private final int minSizeImportant;

    /**
     * @param minSizeImportant The minimum size to be considered "important" by default. Note that a contig might still be considered important if it is named
     *                         chrXXX
     */
    public ChromosomeComparator(int minSizeImportant) {
        this.minSizeImportant = minSizeImportant;
    }

    public static LinkedHashMap<String, Chromosome> sortChromosomeList(List<Chromosome> tmpChromos, int minBig, LinkedHashMap<String, Chromosome> chromosomeMap) {
        chromosomeMap.clear();
        Collections.sort(tmpChromos, new ChromosomeComparator(minBig));
        for (Chromosome chromosome : tmpChromos) {
            chromosomeMap.put(chromosome.getName(), chromosome);
        }
        return chromosomeMap;
    }

    @Override
    public int compare(Chromosome o1, Chromosome o2) {
        boolean o1import = isImportant(o1);
        boolean o2import = isImportant(o2);
        boolean checkNames = (o1import == o2import);

        if (checkNames) {
            return ChromosomeNameComparator.getInstance().compare(o1.getName(), o2.getName());
        } else if (o1import) {
            return -1;
        } else {
            return +1;
        }
    }

    private boolean isImportant(Chromosome chromosome) {
        return (chromosome.getLength() > minSizeImportant) || (chromosome.getName().toLowerCase().startsWith("chr") && chromosome.getName().length() <= 6);
    }
}
