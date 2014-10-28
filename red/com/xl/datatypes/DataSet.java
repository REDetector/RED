package com.xl.datatypes;

import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.sequence.Alignment;
import com.xl.datatypes.sequence.Location;
import com.xl.dialog.CrashReporter;
import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;
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

    /**
     * This variable controls how many thread we allow to finalise at the same
     * time.
     * <p/>
     * We'll make as many threads as we have CPUs up to a limit of 6, above
     * which we're likely to hurt the throughput as we just thrash the
     * underlying disks.
     */
    private static final int MAX_CONCURRENT_FINALISE = Math.min(Runtime.getRuntime().availableProcessors(), 6);
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

    private Map<String, ChromosomeDataStore> readData = new HashMap<String, ChromosomeDataStore>();

    /**
     * The original file name - can't be changed by the user
     */
    private String fileName;
    /**
     * A flag to say if we've optimised this dataset
     */
    private boolean isFinalised = false;
    /**
     * This count allows us to keep track of the progress of finalisation for
     * the individual chromosomes
     */
    private ThreadSafeIntCounter chromosomesStillToFinalise;

    /**
     * The last cached chromosome.
     */
    private String lastCachedChromosome = null;
    /**
     * The reads last loaded from the cache
     */
    private List<Location> lastCachedReads = null;

    private HashMap<Probe, ReadIndexOfProbe> probeMap = new HashMap<Probe, ReadIndexOfProbe>();


    /**
     * Instantiates a new data set.
     *
     * @param name     The initial value for the user changeable name
     * @param fileName The name of the data source - which can't be changed by the
     *                 user
     */
    public DataSet(String name, String fileName) {
        super(name);
        this.fileName = fileName;

        // We need to set a shutdown hook to delete any cache files we hold
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    public void setName(String name) {
        super.setName(name);
        if (collection() != null) {
            collection().dataSetRenamed(this);
        }
    }

    public void finalise(String chr) {

    }

    /**
     * This call optimises the data structure from a flexible structure which
     * can accept more data, to a fixed structure optimised for size and speed
     * of access. If required it can also cache the data to disk.
     * <p/>
     * This call should only be made by DataParsers who know that no more data
     * will be added.
     */
    public synchronized void finalise1() {

        if (isFinalised)
            return;

        // To make querying the data more efficient we're going to convert
        // all of the vectors in our data structure into SequenceRead arrays
        // which are sorted by start position. This means that subsequent
        // access will be a lot more efficient.

        Set<String> chrs = readData.keySet();

        chromosomesStillToFinalise = new ThreadSafeIntCounter();

        for (String chr : chrs) {
            while (chromosomesStillToFinalise.value() >= MAX_CONCURRENT_FINALISE) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            chromosomesStillToFinalise.increment();
            readData.get(chr).finalise();
        }

        // Now we need to wait around for the last chromosome to finish processing

        while (chromosomesStillToFinalise.value() > 0) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        isFinalised = true;
    }

    public void addData(Alignment location) {
        String chr = location.getChr();
        if (readData.containsKey(chr)) {
            readData.get(chr).add(location);
        } else {
            ChromosomeDataStore cds = new ChromosomeDataStore();
            cds.add(location);
            readData.put(chr, cds);
        }
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


    @Override
    public List<Location> getReadsForProbe(Probe p) {
//        if (!isFinalised)
//            finalise();

        List<Location> allReads = getReadsForChromosome(p.getChr());

        if (allReads.size() == 0)
            return null;

        int startIndex = -1;
        int endIndex = -1;
        if (probeMap.containsKey(p)) {
            ReadIndexOfProbe readIndexOfProbe = probeMap.get(p);
            startIndex = readIndexOfProbe.getStartIndex();
            endIndex = readIndexOfProbe.getEndIndex();
        } else {
            for (int i = 0, len = allReads.size(); i < len; i++) {
                if (SequenceReadUtils.overlaps(allReads.get(i), p)) {
                    startIndex = i;
                    break;
                }
            }
            for (int len = allReads.size(), i = len; i > 0; i--) {
                if (SequenceReadUtils.overlaps(allReads.get(i), p)) {
                    endIndex = i;
                    break;
                }
            }
            probeMap.put(p, new ReadIndexOfProbe(p, startIndex, endIndex));
        }
        return new ArrayList<Location>(allReads.subList(startIndex, endIndex));
    }

    public synchronized List<Location> getReadsForChromosome(String c) {
//        if (!isFinalised)
//            finalise();

        // Check if we need to reset which chromosome was loaded last. We need to do this even if we're not caching since we use this to determine whether
        // the cached index we're holding is valid.
        boolean needToUpdate = lastCachedChromosome == null || lastCachedChromosome.equals(c);
        if (needToUpdate) {
            lastCachedChromosome = c;
        }
        if (readData.containsKey(c)) {
            if (readData.get(c).getSequenceReads() != null) {
                // We're not caching, so just give them back the reads
                return readData.get(c).getSequenceReads();
            } else {
                // This is a serialised dataset.

                // Check if we've cached this data
                if (!needToUpdate) {
                    return lastCachedReads;
                }

                REDApplication.getInstance().cacheUsed();
                // System.err.println("Cache miss for "+this.name()+" requested "+c+" but last cached was "+lastCachedChromosome);

                // If not then we need to reload the data from the temp file
                try {
                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(readData.get(c).tempFile)));
                    lastCachedReads = (List<Location>) ois.readObject();
                    ois.close();
                    return lastCachedReads;
                } catch (Exception e) {
                    new CrashReporter(e);
                    e.printStackTrace();
                }
            }
            return readData.get(c).getSequenceReads();
        } else {
            return new ArrayList<Location>();
        }
    }

    public void run() {
        // We need to delete any cache files we're still holding
        Set<String> chrs = readData.keySet();
        for (String chr : chrs) {
            File f = readData.get(chr).tempFile;
            if (f != null) {
                if (!f.delete())
                    System.err.println("Failed to delete cache file " + f.getAbsolutePath());
            }
        }
    }

    public int getReadCountForChromosome(String chr) {

//        if (!isFinalised)
//            finalise();

        if (readData.containsKey(chr)) {
            return getReadsForChromosome(chr).size();
        } else {
            return 0;
        }
    }

    public int getTotalReadCount() {
//		System.out.println(this.getClass().getDisplayName() + ":getTotalReadCount()");
//        if (!isFinalised)
//            finalise();

        return totalReadCount.value();
    }

    public int getMaxReadLength() {
        return minMaxLength.max();
    }

    public int getMinReadLength() {
        return minMaxLength.min();
    }

    public int getReadCountForStrand(Strand strand) {

//        if (!isFinalised)
//            finalise();

        if (strand == Strand.POSITIVE) {
            return forwardReadCount.value();
        } else if (strand == Strand.NEGATIVE) {
            return reverseReadCount.value();
        } else {
            return unknownReadCount.value();
        }
    }

    public long getTotalReadLength() {

//        if (!isFinalised)
//            finalise();

        return totalReadLength.value();
    }

    /**
     * The Class ChromosomeDataStore.
     */
    private class ChromosomeDataStore implements Runnable {
        private List<Location> sequenceReads = null;
        /**
         * The temp file.
         */
        private File tempFile = null;

        public ChromosomeDataStore() {
            sequenceReads = new ArrayList<Location>();
        }

        public List<Location> getSequenceReads() {
            if (sequenceReads == null) {
                sequenceReads = new ArrayList<Location>();
            }
            return sequenceReads;
        }

        public void add(Location location) {
            sequenceReads.add(location);
        }

        public void finalise() {
            Thread t = new Thread(this);
            t.start();
        }

        public void run() {
            // This method is only run when the store is being finalised. It allows us to process all of the chromosomes for a data store in parallel which
            // is quicker given that the processing is constrained by CPU

            Collections.sort(sequenceReads);

//            if (removeDuplicates()) {
//                List<SequenceRead> seqTmp = new ArrayList<SequenceRead>();
//                SequenceRead lastRead = sequenceReads.get(0);
//                seqTmp.add(sequenceReads.get(0));
//                for (int i = 1; i < sequenceReads.size(); i++) {
//                    SequenceRead temp = sequenceReads.get(i);
//                    if (!SequenceReadUtils.duplicate(lastRead, temp)) {
//                        seqTmp.add(temp);
//                        lastRead = temp;
//                    }
//                }
//                sequenceReads.clear();
//                sequenceReads = seqTmp;
//            }

            // We keep local counts here so we only have to do one update of the
            // synchronised counters

            int totalReads = 0;
            int forwardReads = 0;
            int reverseReads = 0;

            int readLengths = 0;

            int localMinLength = 0;
            int localMaxLength = 0;

            for (int i = 0, len = sequenceReads.size(); i < len; i++) {

                // This is really slow when lots of datasets are doing this
                // at the same time. Instead we can keep a local cache of
                // min max values and just send the extreme values to the
                // main set at the end.
                //
                Alignment sequence;
                if (sequenceReads.get(i) instanceof Alignment) {
                    sequence = (Alignment) sequenceReads.get(i);
                } else {
                    continue;
                }
                int sequenceLength = sequence.getEnd() - sequence.getStart() + 1;
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
                }
            }

            // Now update the min/max synchronized lengths
            minMaxLength.addValue(localMinLength);
            minMaxLength.addValue(localMaxLength);

            // Now update the syncrhonized counters
            totalReadCount.incrementBy(totalReads);
            forwardReadCount.incrementBy(forwardReads);
            reverseReadCount.incrementBy(reverseReads);

            totalReadLength.incrementBy(readLengths);

            try {
                tempFile = File.createTempFile("data_set", ".temp", new File(LocationPreferences.getInstance().getTempDirectory()));
                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
                oos.writeObject(sequenceReads);
                oos.close();
            } catch (IOException ioe) {
                new CrashReporter(ioe);
                ioe.printStackTrace();
            }
            chromosomesStillToFinalise.decrement();
        }

    }

    private class ReadIndexOfProbe {
        private Probe probe;
        private int startIndex = -1;
        private int endIndex = -1;

        public ReadIndexOfProbe(Probe probe, int startIndex, int endIndex) {
            this.probe = probe;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public Probe getProbe() {
            return probe;
        }

        public int getEndIndex() {
            return endIndex;
        }

    }
}
