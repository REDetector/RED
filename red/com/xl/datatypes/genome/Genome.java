package com.xl.datatypes.genome;

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

    // TODO A hack (obviously), we need to record a species in the genome definitions
    private static Map<String, String> ucscSpeciesMap;
    /**
     * The genome id.
     */
    private String genomeId;
    /**
     * The display name.
     */
    private String displayName;
    private List<String> chromosomeNames = null;
    private LinkedHashMap<String, Chromosome> chromosomeMap = new LinkedHashMap<String, Chromosome>();
    /**
     * The annotation collection.
     */
    private AnnotationCollection annotationCollection = null;
    private Sequence sequence = null;
    private String species = null;

    public Genome(String genomeId, String displayName, Sequence sequence) {
        this(genomeId, displayName, sequence, false);
    }

    public Genome(String genomeId, String displayName, Sequence sequence,
                  boolean chromosOrdered) {
        this.genomeId = genomeId;
        this.displayName = displayName;
        setSequence(sequence, chromosOrdered);
        // Initial Annotation for Genome.
        annotationCollection = new AnnotationCollection(this);

    }

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

    public void setSequence(Sequence sequence, boolean chromosOrdered) {
        this.sequence = sequence;
        if (sequence != null) {
            chromosomeNames = sequence.getChromosomeNames();
            int chromosomeLength = chromosomeNames.size();
            int maxLength = -1;
            if (chromosOrdered) {
                for (String chrName : chromosomeNames) {
                    int length = sequence.getChromosomeLength(chrName);
                    maxLength = Math.max(maxLength, length);
                    addChromosome(new Chromosome(chrName, length));
                }
            } else {
                List<Chromosome> tmpChromosomes = new ArrayList<Chromosome>(chromosomeLength);
                for (String chrName : chromosomeNames) {
                    int length = sequence.getChromosomeLength(chrName);
                    maxLength = Math.max(maxLength, length);
                    Chromosome chrom = new Chromosome(chrName, length);
                    tmpChromosomes.add(chrom);
                }
                ChromosomeComparator.sortChromosomeList(tmpChromosomes,
                        maxLength / 10, chromosomeMap);
                chromosomeNames = new ArrayList<String>(chromosomeMap.keySet());
            }
        }
    }

    public void setSequence(Sequence sequence) {
        setSequence(sequence, false);
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
        if (chromosomeNames == null) {
            chromosomeNames = new ArrayList<String>(chromosomeMap.keySet());
        }
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
        return chromosomeMap.containsValue(c);
    }

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

}
