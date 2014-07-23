package com.xl.thread;

import com.dw.denovo.*;
import com.dw.dnarna.DnaRnaVcf;
import com.dw.publicaffairs.Clear;
import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;
import com.xl.interfaces.ProgressListener;
import com.xl.preferences.LocationPreferences;
import com.xl.preferences.REDPreferences;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Administrator on 2014/7/22.
 */
public class ThreadNonDenovoInput implements Runnable {
    protected final ArrayList<ProgressListener> listeners;
    private final int ALL_STEP = 10;
    protected boolean cancel = false;

    public ThreadNonDenovoInput() {
        listeners = new ArrayList<ProgressListener>();

    }

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        LocationPreferences locationPreferences = LocationPreferences.getInstance();
        manager.createStatement();
        manager.setAutoCommit(true);
        progressUpdated("Creating non-denovo database...", 1, ALL_STEP);
        manager.createDatabase(DatabaseManager.NON_DENOVO_DATABASE_NAME);
        manager.useDatabase(DatabaseManager.NON_DENOVO_DATABASE_NAME);


        DnaRnaVcf df = new DnaRnaVcf(manager, locationPreferences.getRnaVcfFile(), locationPreferences.getDnaVcfFile()
                , DatabaseManager.RNA_VCF_TABLE_NAME, DatabaseManager.DNA_VCF_TABLE_NAME);
        Utilities.getInstance().createCalTable(locationPreferences.getDnaVcfFile());
        progressUpdated("Creating DNA vcf table...", 2, ALL_STEP);
        df.establishDnaTable();
        progressUpdated("Importing DNA vcf data...", 3, ALL_STEP);
        df.dnaVcf();

        Clear clear = new Clear();
        clear.clear(Utilities.getInstance().getS2(), Utilities.getInstance().getS3());

        Utilities.getInstance().createCalTable(locationPreferences.getRnaVcfFile());
        progressUpdated("Creating RNA vcf table...", 4, ALL_STEP);
        df.establishRnaTable();
        progressUpdated("Importing RNA vcf data...", 5, ALL_STEP);
        df.rnaVcf();

        progressUpdated("Filtering sites based on quality and coverage...", 6, ALL_STEP);
        BasicFilter bf = new BasicFilter(manager, DatabaseManager.RNA_VCF_TABLE_NAME, DatabaseManager.SPECIFIC_FILTER_TABLE_NAME,
                DatabaseManager.BASIC_FILTER_TABLE_NAME);
        bf.createSpecificTable();
        bf.specificf();
        bf.createBasicTable();
        // The first parameter means quality and the second means depth
        bf.basicFilter(20, 6);
        bf.distinctTable();


        RepeatFilter rf = new RepeatFilter(manager, locationPreferences.getRepeatFile(), DatabaseManager.REPEAT_FILTER_TABLE_NAME,
                "refrepeat", DatabaseManager.BASIC_FILTER_TABLE_NAME);
        progressUpdated("Importing repeatmasker data...", 7, ALL_STEP);
        rf.loadrepeat();
        rf.establishrepeat();
        rf.repeatFilter();
        rf.distinctTable();

        progressUpdated("Importing RefSeq Genes data...", 8, ALL_STEP);
        ComphrehensiveFilter cf = new
                ComphrehensiveFilter(manager, locationPreferences.getRefSeqFile(), DatabaseManager.COMPREHENSIVE_FILTER_TABLE_NAME,
                "refcomprehensive", DatabaseManager.REPEAT_FILTER_TABLE_NAME);
        cf.loadcom();
        cf.establishCom();
        cf.comphrehensiveF(2);
        cf.distinctTable();

        progressUpdated("Importing dbSNP data...", 9, ALL_STEP);
        DbsnpFilter sf = new
                DbsnpFilter(manager, locationPreferences.getDbSNPFile(), DatabaseManager.DBSNP_FILTER_TABLE_NAME, "refsnp",
                DatabaseManager.COMPREHENSIVE_FILTER_TABLE_NAME);
        sf.establishsnp();
        sf.loadRefdbSnp();
        sf.snpFilter();
        sf.distinctTable();

        progressUpdated("Importing DARNED data...", 10, ALL_STEP);
        PValueFilter pv = new
                PValueFilter(manager, locationPreferences.getDarnedFile(), DatabaseManager.PVALUE_FILTER_TABLE_NAME,
                "refdarned", DatabaseManager.DBSNP_FILTER_TABLE_NAME);
        pv.loadRefHg19();
        if (locationPreferences.getRScriptPath() != null && locationPreferences.getRScriptPath().length() != 0) {
            pv.fdr(locationPreferences.getRScriptPath());
        }
        REDPreferences.getInstance().setDataLoadedToDatabase(true);
        processingComplete();
    }

    /**
     * Adds a progress listener.
     *
     * @param l The listener to add
     */
    public void addProgressListener(ProgressListener l) {
        if (l == null) {
            throw new NullPointerException("DataInputListener can't be null");
        }

        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes a progress listener.
     *
     * @param l The listener to remove
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * Alerts all listeners to a progress update
     *
     * @param message The message to send
     * @param current The current level of progress
     * @param max     The level of progress at completion
     */
    protected void progressUpdated(String message, int current, int max) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressUpdated(message, current, max);
        }
    }

    /**
     * Alerts all listeners that an exception was received. The
     * parser is not expected to continue after issuing this call.
     *
     * @param e The exception
     */
    protected void progressExceptionReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressExceptionReceived(e);
        }
    }

    /**
     * Alerts all listeners that a warning was received.  The parser
     * is expected to continue after issuing this call.
     *
     * @param e The warning exception received
     */
    protected void progressWarningReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressWarningReceived(e);
        }
    }

    /**
     * Alerts all listeners that the user cancelled this import.
     */
    protected void progressCancelled() {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressCancelled();
        }
    }

    /**
     * Tells all listeners that the parser has finished parsing the data
     * The list of dataSets should be the same length as the original file list.
     */
    protected void processingComplete() {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressComplete("database_loaded", "");
        }
    }
}
