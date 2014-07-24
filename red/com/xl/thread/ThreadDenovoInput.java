package com.xl.thread;

import com.dw.denovo.*;
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
public class ThreadDenovoInput implements Runnable {
    protected final ArrayList<ProgressListener> listeners;
    private final int ALL_STEP = 7;
    protected boolean cancel = false;

    public ThreadDenovoInput() {
        listeners = new ArrayList<ProgressListener>();
    }

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        LocationPreferences locationPreferences = LocationPreferences.getInstance();
        manager.createStatement();
        manager.setAutoCommit(true);

        progressUpdated("Creating denovo database...", 1, ALL_STEP);
        manager.createDatabase(DatabaseManager.DENOVO_DATABASE_NAME);
        manager.useDatabase(DatabaseManager.DENOVO_DATABASE_NAME);
        Utilities.getInstance().createCalTable(locationPreferences.getRnaVcfFile());

        progressUpdated("Importing RNA vcf data...", 2, ALL_STEP);
        DenovoVcf df = new DenovoVcf(manager);
        df.establishRnaTable(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
        df.loadRnaVcfTable(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME, locationPreferences.getRnaVcfFile());

        progressUpdated("Filtering sites based on quality and coverage...", 3, ALL_STEP);
        BasicFilter bf = new BasicFilter(manager);
        bf.establishSpecificTable(DatabaseManager.SPECIFIC_FILTER_RESULT_TABLE_NAME);
        bf.executeSpecificFilter(DatabaseManager.SPECIFIC_FILTER_RESULT_TABLE_NAME, DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
        bf.establishBasicTable(DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);
        // The first parameter means quality and the second means depth
        bf.executeBasicFilter(DatabaseManager.SPECIFIC_FILTER_RESULT_TABLE_NAME,
                DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME, 20, 6);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Importing repeatmasker data...", 4, ALL_STEP);
        RepeatFilter rf = new RepeatFilter(manager);
        rf.loadRepeatTable(DatabaseManager.REPEAT_FILTER_TABLE_NAME, locationPreferences.getRepeatFile());
        rf.establishRepeatResultTable(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);
        rf.rfilter(DatabaseManager.REPEAT_FILTER_TABLE_NAME, DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME,
                DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Importing RefSeq Genes data...", 5, ALL_STEP);
        ComprehensiveFilter cf = new ComprehensiveFilter(manager);
        cf.establishComprehensiveResultTable(DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME);
        cf.loadComprehensiveTable(DatabaseManager.COMPREHENSIVE_FILTER_TABLE_NAME, locationPreferences.getRefSeqFile());
        cf.executeComprehensiveFilter(DatabaseManager.COMPREHENSIVE_FILTER_TABLE_NAME,
                DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME, DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME
                , 2);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Importing dbSNP data...", 6, ALL_STEP);
        DbsnpFilter sf = new DbsnpFilter(manager);
        sf.establishDbSNPResultTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
        sf.loadDbSNPTable(DatabaseManager.DBSNP_FILTER_TABLE_NAME, locationPreferences.getDbSNPFile());
        sf.executeDbSNPFilter(DatabaseManager.DBSNP_FILTER_TABLE_NAME,
                DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME, DatabaseManager.COMPREHENSIVE_FILTER_RESULT_TABLE_NAME);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Importing DARNED data...", 7, ALL_STEP);
        PValueFilter pv = new PValueFilter(manager);
        pv.estblishPvTable(DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME);
        pv.loadDarnedTable(DatabaseManager.PVALUE_FILTER_TABLE_NAME, locationPreferences.getDarnedFile());
        if (locationPreferences.getRScriptPath() != null && locationPreferences.getRScriptPath().length() != 0) {
            pv.executeFDRFilter(DatabaseManager.PVALUE_FILTER_TABLE_NAME,
                    DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME, DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME,
                    locationPreferences.getRScriptPath());
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
            throw new NullPointerException("DataParserListener can't be null");
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
            i.next().progressComplete("database_loaded", null);
        }
    }
}
