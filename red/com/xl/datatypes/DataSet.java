package com.xl.datatypes;

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

import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.dialog.CrashReporter;
import com.xl.exception.REDException;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;
import com.xl.thread.ThreadSafeIntCounter;
import com.xl.thread.ThreadSafeLongCounter;
import com.xl.thread.ThreadSafeMinMax;
import com.xl.utils.SequenceReadUtils;
import com.xl.utils.Strand;

import java.io.*;
import java.util.*;

/**
 * A DataSet represents a set of reads coming from a single source (usually a
 * file). It is able to store and retrieve reads in a very efficient manner. If
 * the user has requested that data be cached the DataSet is also responsible
 * for saving and loading this data.
 */
public class DataSet extends DataStore implements Runnable {

    // I've tried using a HashMap and a linked list instead of
    // a hashtable and a vector but they proved to be slower and
    // use more memory.

    /**
     * This variable controls how many thread we allow to finalise at the same
     * time.
     * <p/>
     * We'll make as many threads as we have CPUs up to a limit of 6, above
     * which we're likely to hurt the throughput as we just thrash the
     * underlying disks.
     */
    private static final int MAX_CONCURRENT_FINALISE = Math.min(Runtime
            .getRuntime().availableProcessors(), 6);
    /**
     * We cache the total read count to save having to reload every chromosome
     * just to get the read count
     */
    protected ThreadSafeIntCounter totalReadCount = new ThreadSafeIntCounter();
    /**
     * We cache the forward read count to save having to reload every chromosome
     * just to get the read count
     */
    protected ThreadSafeIntCounter forwardReadCount = new ThreadSafeIntCounter();
    /**
     * We cache the min and max read lengths so we can quickly access these for
     * some anlayses without having to go through the whole dataset to get them
     */
    protected ThreadSafeMinMax minMaxLength = new ThreadSafeMinMax();
    /**
     * We cache the reverse read count to save having to reload every chromosome
     * just to get the read count
     */
    protected ThreadSafeIntCounter reverseReadCount = new ThreadSafeIntCounter();
    /**
     * We cache the unknown read count to save having to reload every chromosome
     * just to get the read count
     */
    protected ThreadSafeIntCounter unknownReadCount = new ThreadSafeIntCounter();
    /**
     * The total read length.
     */
    protected ThreadSafeLongCounter totalReadLength = new ThreadSafeLongCounter();
    private Hashtable<String, ChromosomeDataStore> readData = new Hashtable<String, ChromosomeDataStore>();
    private List<SequenceRead> sequenceReads = null;
    /**
     * The original file name - can't be changed by the user
     */
    private String fileName;
    /**
     * A flag to say if we've optimised this dataset
     */
    private boolean isFinalised = false;
    /**
     * A flag which is set as soon as any unsorted data is added to the data set
     */
    private boolean needsSorting = false;
    /**
     * This count allows us to keep track of the progress of finalisation for
     * the individual chromosomes
     */
    private ThreadSafeIntCounter chromosomesStillToFinalise;

    // These are cached values used when we're saving excess data to temp files
    /**
     * A flag to say if we should remove duplicates when finalising
     */
    private boolean removeDuplicates = true;
    /**
     * The last cached chromosome.
     */
    private String lastCachedChromosome = null;
    /**
     * The reads last loaded from the cache
     */
    private SequenceRead[] lastCachedReads = null;
    /**
     * The last index at which a read was found
     */
    private int lastIndex = 0;
    private Location lastProbeLocation = null;

    /**
     * Instantiates a new data set.
     *
     * @param name     The initial value for the user changeable name
     * @param fileName The name of the data source - which can't be changed by the
     *                 user
     */
    public DataSet(String name, String fileName, boolean removeDuplicates) {
        super(name);
        this.fileName = fileName;
        this.removeDuplicates = removeDuplicates;

        // We need to set a shutdown hook to delete any cache files we hold
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#setName(java.lang.String)
     */
    public void setName(String name) {
        super.setName(name);
        if (collection() != null) {
            collection().dataSetRenamed(this);
        }
    }

    protected boolean removeDuplicates() {
        return removeDuplicates;
    }

    /**
     * This call optimises the data structure from a flexible structure which
     * can accept more data, to a fixed structure optimised for size and speed
     * of access. If required it can also cache the data to disk.
     * <p/>
     * This call should only be made by DataParsers who know that no more data
     * will be added.
     */
    public synchronized void finalise() {

        if (isFinalised)
            return;

        // To make querying the data more efficient we're going to convert
        // all of the vectors in our data structure into SequenceRead arrays
        // which are sorted by start position. This means that subsequent
        // access will be a lot more efficient.

        Enumeration<String> e = readData.keys();

        chromosomesStillToFinalise = new ThreadSafeIntCounter();

        while (e.hasMoreElements()) {

            while (chromosomesStillToFinalise.value() >= MAX_CONCURRENT_FINALISE) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                }
            }

            String chr = e.nextElement();
            chromosomesStillToFinalise.increment();
            readData.get(chr).finalise();
        }

        // Now we need to wait around for the last chromosome to finish
        // processing

        while (chromosomesStillToFinalise.value() > 0) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
            }
        }

        isFinalised = true;
    }

    public void addData(SequenceRead sequence) throws REDException {
        addData(sequence, false);
    }

    public void addData(SequenceRead sequence, boolean skipSorting)
            throws REDException {
        if (isFinalised) {
            throw new REDException(
                    "This data set is finalised.  No more data can be added");
        }
        if (readData.containsKey(sequence.getChr())) {
            readData.get(sequence.getChr()).getSequenceReads().add(sequence);
        } else {
            ChromosomeDataStore cds = new ChromosomeDataStore();
            cds.getSequenceReads().add(sequence);
            readData.put(sequence.getChr(), cds);
        }
        if (!skipSorting) {
            needsSorting = true;
        }
    }

    /**
     * Adds more data to this set.
     *
     * @param chr         The chromosome to which data will be added
     * @param start
     * @param strand
     * @param readBases   The data to add
     * @param qualities
     * @param skipSorting
     * @throws REDException if this DataSet has been finalised.
     */
    public void addData(String chr, int start, Strand strand, byte[] readBases,
                        byte[] qualities, boolean skipSorting) throws REDException {
        addData(new SequenceRead(chr, start, strand, readBases, qualities), skipSorting);
    }

    /**
     * Gets the original data source name for this DataSet - usually the name of
     * the file from which it was parsed.
     *
     * @return the file name
     */
    public String fileName() {
        return fileName;
    }

    /**
     * A quick check to see if any data overlaps with a probe
     *
     * @param p the probe to check
     * @return true, if at leas one read overlaps with this probe
     */
    public boolean containsReadForProbe(Probe p) {

        if (!isFinalised)
            finalise();

        SequenceRead[] allReads = getReadsForChromosome(p.getChr());

        if (allReads.length == 0)
            return false;

        int startPos;

        // Use the cached position if we're on the same chromosome
        // and this probe position is higher than the last one we
        // fetched.

        if (lastCachedChromosome != null
                && p.getChr() == lastCachedChromosome
                && (lastProbeLocation == null || SequenceReadUtils.compare(p,
                lastProbeLocation) >= 0)) {
            startPos = lastIndex;
            // System.out.println("Using cached start pos "+lastIndex);
        }

        // If we're on the same chromosome then we'll simply backtrack until
        // we're far
        // enough back that we can't have missed even the longest read in the
        // set.
        else if (lastCachedChromosome != null
                && p.getChr() == lastCachedChromosome) {

            // System.out.println("Last chr="+lastCachedChromosome+" this chr="+p.chromosome()+" lastProbeLocation="+lastProbeLocation+" diff="+SequenceRead.compare(p.packedPosition(),
            // lastProbeLocation));

            int longestRead = getMaxReadLength();

            for (; lastIndex > 0; lastIndex--) {
                if (p.getStart() - allReads[lastIndex].getStart() > longestRead) {
                    break;
                }
            }

            // System.out.println("Starting from index "+lastIndex+" which starts at "+SequenceRead.start(allReads[lastIndex])+" for "+p.start()+" when max length is "+longestRead);

            startPos = lastIndex;

        }

        // If we can't cache then start from the beginning. It's not worth
        // the hassle of trying to guess starting positions
        else {
            startPos = 0;
            lastIndex = 0;
            // System.out.println("Starting from the beginning");
            // System.out.println("Last chr="+lastCachedChromosome+" this chr="+p.chromosome()+" lastProbeLocation="+lastProbeLocation+" diff="+SequenceRead.compare(p.packedPosition(),
            // lastProbeLocation));
        }

        lastProbeLocation = p;

        // We now go forward to see what we can find

        for (int i = startPos; i < allReads.length; i++) {
            // Reads come in order, so we can stop when we've seen enough.
            if (allReads[i].getStart() > p.getEnd()) {
                return false;
            }

            if (SequenceReadUtils.overlaps(allReads[i], p)) {
                // They overlap.
                lastIndex = i;
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadsForProbe(uk.ac.babraham
     * .SeqMonk.DataTypes.Probes.Probe)
     */
    public SequenceRead[] getReadsForProbe(Probe p) {

        if (!isFinalised)
            finalise();

        SequenceRead[] allReads = getReadsForChromosome(p.getChr());

        if (allReads.length == 0)
            return new SequenceRead[0];

        int startPos;

        // Use the cached position if we're on the same chromosome
        // and this probe position is higher than the last one we
        // fetched.

        if (lastCachedChromosome != null
                && p.getChr() == lastCachedChromosome
                && (lastProbeLocation == null || SequenceReadUtils.compare(p,
                lastProbeLocation) >= 0)) {
            startPos = lastIndex;
            // System.out.println("Using cached start pos "+lastIndex);
        }

        // If we're on the same chromosome then we'll simply backtrack until
        // we're far
        // enough back that we can't have missed even the longest read in the
        // set.
        else if (lastCachedChromosome != null
                && p.getChr() == lastCachedChromosome) {

            // System.out.println("Last chr="+lastCachedChromosome+" this chr="+p.chromosome()+" lastProbeLocation="+lastProbeLocation+" diff="+SequenceRead.compare(p.packedPosition(),
            // lastProbeLocation));

            int longestRead = getMaxReadLength();

            for (; lastIndex > 0; lastIndex--) {
                if (p.getStart() - allReads[lastIndex].getStart() > longestRead) {
                    break;
                }
            }

            // System.out.println("Starting from index "+lastIndex+" which starts at "+SequenceRead.start(allReads[lastIndex])+" for "+p.start()+" when max length is "+longestRead);

            startPos = lastIndex;

        }

        // If we're on a different chromosome then start from the very beginning
        else {
            startPos = 0;
            lastIndex = 0;
            // System.out.println("Starting from the beginning");
        }

        lastProbeLocation = p;

        // We now go forward to see what we can find

        boolean cacheSet = false;

        for (int i = startPos; i < allReads.length; i++) {
            // Reads come in order, so we can stop when we've seen enough.
            if (allReads[i].getStart() > p.getEnd()) {
                break;
            }

            if (SequenceReadUtils.overlaps(allReads[i], p)) {
                // They overlap.

                // If this is the first hit we've seen for this probe
                // then update the cache
                if (!cacheSet) {
                    lastIndex = i;
                    cacheSet = true;
                }
                sequenceReads.add(allReads[i]);
            }
        }

        SequenceRead[] returnReads = (SequenceRead[]) sequenceReads.toArray();

        // With the new way of tracking we shouldn't ever need to sort these.
        // SequenceRead.sort(returnReads);
        return returnReads;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadsForChromsome(uk.ac
     * .babraham.SeqMonk.DataTypes.Genome.Chromosome)
     */
    public synchronized SequenceRead[] getReadsForChromosome(String c) {

        if (!isFinalised)
            finalise();

        // Check if we need to reset which chromosome was loaded last.
        // We need to do this even if we're not caching since we use
        // this to determine whether the cached index we're holding
        // is valid.
        boolean needToUpdate = lastCachedChromosome == null
                || lastCachedChromosome != c;
        if (needToUpdate) {
            lastCachedChromosome = c;
            lastProbeLocation = null;
            lastIndex = 0;
        }
        if (readData.containsKey(c)) {

            if (readData.get(c).getSequenceReads() != null) {
                // We're not caching, so just give them back the reads
                return readData.get(c).getSequenceReads().toArray(new SequenceRead[0]);
            } else {
                // This is a serialised dataset.

                // Check if we've cached this data
                if (!needToUpdate) {
                    return lastCachedReads;
                }

                REDApplication.getInstance().cacheUsed();
                // System.err.println("Cache miss for "+this.name()+" requested "+c+" but last cached was "+lastCachedChromosome);

                // If not then we need to reload the data from the
                // temp file
                try {
                    ObjectInputStream ois = new ObjectInputStream(
                            new BufferedInputStream(new FileInputStream(
                                    readData.get(c).tempFile)));
                    lastCachedReads = (SequenceRead[]) ois.readObject();
                    ois.close();
                    return lastCachedReads;
                } catch (Exception e) {
                    new CrashReporter(e);
                }
            }

            return readData.get(c).getSequenceReads().toArray(new SequenceRead[0]);
        } else {
            return new SequenceRead[0];
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // We need to delete any cache files we're still holding

        Enumeration<String> e = readData.keys();
        while (e.hasMoreElements()) {
            String c = e.nextElement();

            File f = readData.get(c).tempFile;
            if (f != null) {
                if (!f.delete())
                    System.err.println("Failed to delete cache file "
                            + f.getAbsolutePath());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadCountForChromosome(
     * uk.ac.babraham.SeqMonk.DataTypes.Genome.Chromosome)
     */
    public int getReadCountForChromosome(String chr) {

        if (!isFinalised)
            finalise();

        if (readData.containsKey(chr)) {
            return getReadsForChromosome(chr).length;
        } else {
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getTotalReadCount()
     */
    public int getTotalReadCount() {
//		System.out.println(this.getClass().getName() + ":getTotalReadCount()");
        if (!isFinalised)
            finalise();

        return totalReadCount.value();
    }

    public int getMaxReadLength() {
        return minMaxLength.max();
    }

    public int getMinReadLength() {
        return minMaxLength.min();
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadCountForStrand(int
     * strand)
     */
    public int getReadCountForStrand(Strand strand) {

        if (!isFinalised)
            finalise();

        if (strand == Strand.POSITIVE) {
            return forwardReadCount.value();
        } else if (strand == Strand.NEGATIVE) {
            return reverseReadCount.value();
        } else {
            return unknownReadCount.value();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getTotalReadLength()
     */
    public long getTotalReadLength() {

        if (!isFinalised)
            finalise();

        return totalReadLength.value();
    }

    /**
     * The Class ChromosomeDataStore.
     */
    private class ChromosomeDataStore implements Runnable {

        /**
         * The temp file.
         */
        private File tempFile = null;

        public List<SequenceRead> getSequenceReads() {
            if (sequenceReads == null) {
                sequenceReads = new ArrayList<SequenceRead>();
            }
            return sequenceReads;
        }

        public void finalise() {
            Thread t = new Thread(this);
            t.start();
        }

        public void run() {
            // This method is only run when the store is being finalised. It
            // allows
            // us to process all of the chromosomes for a data store in parallel
            // which is quicker given that the processing is constrained by CPU

            if (needsSorting) {
                // System.err.println("Sorting unsorted reads");
                Collections.sort(sequenceReads);
            }


            if (DataSet.this.removeDuplicates) {
                List<SequenceRead> seqTmp = new ArrayList<SequenceRead>();
                SequenceRead lastRead = sequenceReads.get(0);
                seqTmp.add(sequenceReads.get(0));
                for (int i = 1; i < sequenceReads.size(); i++) {
                    SequenceRead temp = sequenceReads.get(i);
                    if (!SequenceReadUtils.duplicate(lastRead, temp)) {
                        seqTmp.add(temp);
                        lastRead = temp;
                    }
                }
                sequenceReads.clear();
                sequenceReads = seqTmp;
            }

            // Work out the cached values for total length,count and
            // for/rev/unknown counts

            // We keep local counts here so we only have to do one update of the
            // synchronised counters

            int totalReads = 0;
            int forwardReads = 0;
            int reverseReads = 0;
            int unknownReads = 0;

            int readLengths = 0;

            int localMinLength = 0;
            int localMaxLength = 0;

            for (int i = 0; i < sequenceReads.size(); i++) {

                // This is really slow when lots of datasets are doing this
                // at the same time. Instead we can keep a local cache of
                // min max values and just send the extreme values to the
                // main set at the end.
                //
                SequenceRead sequence = sequenceReads.get(i);
                int sequenceLength = sequence.length();
                if (i == 0 || sequenceLength < localMinLength)
                    localMinLength = sequenceLength;
                if (i == 0 || sequenceLength > localMaxLength)
                    localMaxLength = sequenceLength;

                // Add this length to the total
                readLengths += sequenceLength;

                // Increment the appropriate counts
                totalReads++;
                if (sequence.getStrand() == Strand.POSITIVE) {
                    forwardReads++;
                } else if (sequence.getStrand() == Strand.NEGATIVE) {
                    reverseReads++;
                } else {
                    unknownReads++;
                }
            }

            // Now update the min/max synchronized lengths
            minMaxLength.addValue(localMinLength);
            minMaxLength.addValue(localMaxLength);

            // Now update the syncrhonized counters
            totalReadCount.incrementBy(totalReads);
            forwardReadCount.incrementBy(forwardReads);
            reverseReadCount.incrementBy(reverseReads);
            unknownReadCount.incrementBy(unknownReads);

            totalReadLength.incrementBy(readLengths);

            try {
                tempFile = File.createTempFile("red_data_set", ".temp",
                        REDPreferences.getInstance().tempDirectory());
                ObjectOutputStream oos = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(tempFile)));
                oos.writeObject(sequenceReads);
                oos.close();
            } catch (IOException ioe) {
                new CrashReporter(ioe);
            }

            chromosomesStillToFinalise.decrement();

        }

    }

}
