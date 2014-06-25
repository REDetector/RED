package com.xl.utils;

import com.xl.datatypes.genome.Chromosome;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class ChromosomeComparator implements Comparator<Chromosome> {
    private final int minSizeImportant;

    /**
     * @param minSizeImportant The minimum size to be considered "important" by default. Note
     *                         that a contig might still be considered important if it is
     *                         named chrXXX
     */
    public ChromosomeComparator(int minSizeImportant) {
        this.minSizeImportant = minSizeImportant;
    }

    @Override
    public int compare(Chromosome o1, Chromosome o2) {
        boolean o1import = isImportant(o1);
        boolean o2import = isImportant(o2);
        boolean checkNames = (o1import == o2import);

        if (checkNames) {
            return ChromosomeNameComparator.getInstance().compare(o1.getName(),
                    o2.getName());
        } else if (o1import) {
            return -1;
        } else {
            return +1;
        }
    }

    private boolean isImportant(Chromosome chromo) {
        if (chromo.getLength() > minSizeImportant)
            return true;
        if (chromo.getName().toLowerCase().startsWith("chr")
                && chromo.getName().length() <= 6)
            return true;
        return false;
    }

    public static LinkedHashMap<String, Chromosome> sortChromosomeList(
            List<Chromosome> tmpChromos, int minBig,
            LinkedHashMap<String, Chromosome> chromosomeMap) {
        chromosomeMap.clear();
        Collections.sort(tmpChromos, new ChromosomeComparator(minBig));
        for (int i = 0; i < tmpChromos.size(); i++) {
            Chromosome chromo = tmpChromos.get(i);
            chromosomeMap.put(chromo.getName(), chromo);
        }
        return chromosomeMap;
    }
}
