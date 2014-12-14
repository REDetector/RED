package htsjdk.samtools.util;

import htsjdk.samtools.SAMRecord;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Little progress logging class to facilitate consistent output of useful information when progressing
 * through a stream of SAM records.
 *
 * @author Tim Fennell
 */
public class ProgressLogger implements ProgressLoggerInterface {
    private final Log log;
    private final int n;
    private final String verb;
    private final String noun;
    private final long startTime = System.currentTimeMillis();
    
    private final NumberFormat fmt = new DecimalFormat("#,###");
    private final NumberFormat timeFmt = new DecimalFormat("00");
    
    private long processed = 0;

	// Set to -1 until the first record is added
    private long lastStartTime = -1;

    /**
     * Construct a progress logger.
     * @param log the Log object to write outputs to
     * @param n the frequency with which to output (i.e. every N records)
     * @param verb the verb to log, e.g. "Processed, Read, Written".
     * @param noun the noun to use when logging, e.g. "Records, Variants, Loci"
     */
    public ProgressLogger(final Log log, final int n, final String verb, final String noun) {
        this.log = log;
        this.n = n;
        this.verb = verb;
        this.noun = noun;
    }

    /**
     * Construct a progress logger.
     * @param log the Log object to write outputs to
     * @param n the frequency with which to output (i.e. every N records)
     * @param verb the verb to log, e.g. "Processed, Read, Written".
     */
    public ProgressLogger(final Log log, final int n, final String verb) {
        this(log, n, verb, "records");
    }

    /**
     * Construct a progress logger with the desired log and frequency and the verb "Processed".
     * @param log the Log object to write outputs to
     * @param n the frequency with which to output (i.e. every N records)
     */
    public ProgressLogger(final Log log, final int n) { this(log, n, "Processed"); }

    /**
     * Construct a progress logger with the desired log, the verb "Processed" and a period of 1m records.
     * @param log the Log object to write outputs to
     */
    public ProgressLogger(final Log log) { this(log, 1000000); }

    public synchronized boolean record(final String chrom, final int pos) {
	    if (this.lastStartTime == -1) this.lastStartTime = System.currentTimeMillis();
	    if (++this.processed % this.n == 0) {
            final long now = System.currentTimeMillis();
            final long lastPeriodSeconds = (now - this.lastStartTime) / 1000;
            this.lastStartTime = now;

            final long seconds = (System.currentTimeMillis() - startTime) / 1000;
            final String elapsed   = formatElapseTime(seconds);
            final String period    = pad(fmt.format(lastPeriodSeconds), 4);
            final String processed = pad(fmt.format(this.processed), 13);

            final String readInfo;
            if (chrom == null) readInfo = "*/*";
            else readInfo = chrom + ":" + fmt.format(pos);

            log.info(this.verb, " ", processed, " " + noun + ".  Elapsed time: ", elapsed, "s.  Time for last ", fmt.format(this.n),
                     ": ", period, "s.  Last read position: ", readInfo);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Records that a given record has been processed and triggers logging if necessary.
     * @return boolean true if logging was triggered, false otherwise
     */
    public synchronized boolean record(final SAMRecord rec) {
        if (rec.getReferenceIndex() == SAMRecord.NO_ALIGNMENT_REFERENCE_INDEX) {
            return record(null, 0);
        }
        else {
            return record(rec.getReferenceName(), rec.getAlignmentStart());
        }
    }
    
    /** Records multiple SAMRecords and triggers logging if necessary. */
    public boolean record(final SAMRecord... recs) {
        boolean triggered = false;
        for (final SAMRecord rec : recs) triggered = record(rec) || triggered;
        return triggered;
    }
    
    /** Returns the count of records processed. */
    public long getCount() { return this.processed; }

    /** Returns the number of seconds since progress tracking began. */
    public long getElapsedSeconds() { return (System.currentTimeMillis() - this.startTime) / 1000; }
    
    /** Left pads a string until it is at least the given length. */
    private String pad (String in, final int length) {
        while (in.length() < length) {
            in = " " + in;
        }
        
        return in;
    }
    
    /** Formats a number of seconds into hours:minutes:seconds. */
    private String formatElapseTime(final long seconds) {
        final long s = seconds % 60;
        final long allMinutes = seconds / 60;
        final long m = allMinutes % 60;
        final long h = allMinutes / 60;

        return timeFmt.format(h) + ":" + timeFmt.format(m) + ":" + timeFmt.format(s);
    }
}
