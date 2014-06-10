package com.xl.datatypes.genome;

/**
 * Copyright Copyright 2007-13 Simon Andrews
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

import com.xl.datatypes.annotation.AnnotationCollection;
import com.xl.datatypes.annotation.Cytoband;
import com.xl.datatypes.sequence.Sequence;
import com.xl.utils.ChromosomeComparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * The Class Genome represents an entire annotated genome assembly
 */
public class Genome {

    /**
     * The genome id.
     */
    private String genomeId;

    /**
     * The display name.
     */
    private String displayName;

    private List<String> chromosomeNames = null;

    private LinkedHashMap<String, Chromosome> chromosomeMap = null;

    /**
     * The annotation collection.
     */
    private AnnotationCollection annotationCollection = null;

    private Sequence sequence = null;

    private String species = null;

    public Genome(String genomeId, String displayName, Sequence sequence,
                  boolean chromosOrdered) {
        this.genomeId = genomeId;
        this.displayName = displayName;
        this.sequence = sequence;
        if (sequence != null) {
            chromosomeNames = sequence.getChromosomeNames();
            int chromosomeLength = chromosomeNames.size();

            List<Chromosome> tmpChromosomes = null;
            if (!chromosOrdered) {
                tmpChromosomes = new ArrayList<Chromosome>(chromosomeLength);
            }
            int maxLength = -1;
            chromosomeMap = new LinkedHashMap<String, Chromosome>(chromosomeLength);

            for (int i = 0; i < chromosomeLength; i++) {
                String chr = chromosomeNames.get(i);
                int length = sequence.getChromosomeLength(chr);
                maxLength = Math.max(maxLength, length);
                Chromosome chrom = new Chromosome(chr, length);
                if (chromosOrdered) {
                    addChromosome(chrom);
                } else {
                    tmpChromosomes.add(chrom);
                }
            }

            if (!chromosOrdered) {
                ChromosomeComparator.sortChromosomeList(tmpChromosomes,
                        maxLength / 10, chromosomeMap);
                chromosomeNames = new ArrayList<String>(chromosomeMap.keySet());
            }

        }
        // Initial Annotation for Genome.
        annotationCollection = new AnnotationCollection(this);

    }

    public void addChromosome(Chromosome chromosome) {
        if (chromosome == null) {
            return;
        }
        if (!chromosomeMap.containsValue(chromosome)) {
            chromosomeMap.put(chromosome.getName(), chromosome);
        }
    }

    public Chromosome getChromosome(String chrName) {
        return chromosomeMap.get(chrName);
    }

    public String[] getAllChromosomeNames() {
        return chromosomeNames.toArray(new String[0]);
    }

    public Chromosome[] getAllChromosomes() {
        return chromosomeMap.values().toArray(new Chromosome[0]);
    }

    /**
     * Method description
     *
     * @return
     */
    public String getGenomeId() {
        return genomeId;
    }

    public String getSpecies() {
        if (species == null) {
            species = Genome.getSpeciesForID(genomeId);
        }
        return species;
    }

    /**
     * Return the nucleotide sequence on the + strand for the genomic interval.
     * This method can return null if sequence is not available.
     *
     * @param chr
     * @param start start position in "zero-based" coordinates
     * @param end   end position
     * @return sequence, or null if not available
     */
    public byte[] getSequence(String chr, int start, int end) {

        if (sequence == null) {
            return null;
        }

        Chromosome c = getChromosome(chr);
        if (c == null) {
            return null;
        }
        end = Math.min(end, c.getLength());
        if (end <= start) {
            int temp = end;
            end = start;
            start = temp;
        }
        return sequence.getSequence(chr, start, end);
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Return the reference base at the given position. Can return null if
     * reference sequence is unknown
     *
     * @param chr
     * @param pos
     * @return the reference base, or null if unknown
     */
    public byte getReference(String chr, int pos) {
        return sequence == null ? null : sequence.getBase(chr, pos);
    }

    public void setCytobands(LinkedHashMap<String, List<Cytoband>> chrCytoMap) {

        for (Map.Entry<String, List<Cytoband>> entry : chrCytoMap.entrySet()) {
            String chr = entry.getKey();
            List<Cytoband> cytobands = entry.getValue();

            Chromosome chromosome = chromosomeMap.get(chr);
            if (chromosome != null) {
                chromosome.setCytobands(cytobands);
            }
        }
    }

    /**
     * Annotation collection.
     *
     * @return the annotation collection
     */
    public AnnotationCollection getAnnotationCollection() {
        return annotationCollection;
    }

    /**
     * Gets the chromosome count.
     *
     * @return the chromosome count
     */
    public int getAllChromosomeCount() {
        return chromosomeMap.size();
    }

    /**
     * Checks for chromosome.
     *
     * @param c the c
     * @return true, if successful
     */
    public boolean hasChromosome(Chromosome c) {
        if (chromosomeMap.containsValue(c)) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return displayName;
    }

    /**
     * Gets the longest chromosome length.
     *
     * @return the longest chromosome length
     */
    public int getLongestChromosomeLength() {
        int longest = 0;
        Collection<Chromosome> collection = chromosomeMap.values();
        Iterator<Chromosome> chromosomeIterator = collection.iterator();
        while (chromosomeIterator.hasNext()) {
            int chr = chromosomeIterator.next().getLength();
            if (chr > longest) {
                longest = chr;
            }
        }
        return longest;
    }

    // TODO A hack (obviously), we need to record a species in the genome definitions
    private static Map<String, String> ucscSpeciesMap;

    private static synchronized String getSpeciesForID(String id) {
        if (ucscSpeciesMap == null) {
            ucscSpeciesMap = new HashMap<String, String>();

            InputStream is = null;

            try {
                is = Genome.class.getResourceAsStream("speciesMapping.txt");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));

                String nextLine;
                while ((nextLine = br.readLine()) != null) {
                    if (nextLine.startsWith("#"))
                        continue;
                    String[] tokens = nextLine.split("\t");
                    if (tokens.length == 2) {
                        ucscSpeciesMap.put(tokens[0], tokens[1]);
                    } else {
                        // log.error("Unexpected number of tokens in species mapping file for line: "
                        // + nextLine);
                    }
                }
            } catch (IOException e) {
                // log.error("Error reading species mapping table", e);
            } finally {
                if (is != null)
                    try {
                        is.close();
                    } catch (IOException e) {
                        // log.error("", e);
                    }
            }

        }

        for (Map.Entry<String, String> entry : ucscSpeciesMap.entrySet()) {
            if (id.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

}
