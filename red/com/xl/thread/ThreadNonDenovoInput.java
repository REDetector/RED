package com.xl.thread;

import com.dw.denovo.DbsnpFilter;
import com.dw.denovo.PValueFilter;
import com.dw.denovo.RepeatFilter;
import com.dw.denovo.SpliceJunctionFilter;
import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Query;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.interfaces.ProgressListener;
import com.xl.parsers.dataparsers.VCFParser;
import com.xl.preferences.LocationPreferences;
import com.xl.preferences.REDPreferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Administrator on 2014/7/22.
 */
public class ThreadNonDenovoInput implements Runnable {
    protected final ArrayList<ProgressListener> listeners;
    private final int ALL_STEP = 10;
    protected boolean cancel = false;
    private DataCollection dataCollection;

    public ThreadNonDenovoInput(DataCollection dataCollection) {
        listeners = new ArrayList<ProgressListener>();
        this.dataCollection = dataCollection;
    }

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        LocationPreferences locationPreferences = LocationPreferences.getInstance();
        REDPreferences.getInstance().setDenovo(false);
        manager.setAutoCommit(true);
        progressUpdated("Creating non-denovo database...", 1, ALL_STEP);
        manager.createDatabase(DatabaseManager.NON_DENOVO_DATABASE_NAME);
        manager.useDatabase(DatabaseManager.NON_DENOVO_DATABASE_NAME);

        progressUpdated("Importing DNA vcf data...", 2, ALL_STEP);
        VCFParser vcfParser = new VCFParser();
        vcfParser.parseVCFFile(DatabaseManager.DNA_VCF_RESULT_TABLE_NAME, locationPreferences.getDnaVcfFile());

        progressUpdated("Importing RNA vcf data...", 3, ALL_STEP);
        vcfParser.parseVCFFile(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME, locationPreferences.getRnaVcfFile());

        Vector<Probe> probes = Query.queryAllEditingSites(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
        Probe[] probeArray = probes.toArray(new Probe[0]);
        dataCollection.setProbeSet(new ProbeSet("Original RNA editing sites by RNA vcf file", probeArray,
                DatabaseManager.RNA_VCF_RESULT_TABLE_NAME));

        progressUpdated("Filtering sites by quality and coverage...", 4, ALL_STEP);
//        BasicFilter bf = new BasicFilter(manager);
//        bf.establishSpecificTable(DatabaseManager.SPECIFIC_FILTER_RESULT_TABLE_NAME);
//        bf.executeSpecificFilter(DatabaseManager.SPECIFIC_FILTER_RESULT_TABLE_NAME, DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
//        bf.establishBasicTable(DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);
        // The first parameter means quality and the second means depth
//        bf.executeBasicFilter(DatabaseManager.SPECIFIC_FILTER_RESULT_TABLE_NAME,
//                DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME, 20, 6);
//        DatabaseManager.getInstance().distinctTable(DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Importing repeatmasker data...", 5, ALL_STEP);
        RepeatFilter rf = new RepeatFilter(manager);
        rf.loadRepeatTable(DatabaseManager.REPEAT_FILTER_TABLE_NAME, locationPreferences.getRepeatFile());
//        rf.establishRepeatResultTable(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);
//        rf.establishAluResultTable(DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME);
//        rf.executeRepeatFilter(DatabaseManager.REPEAT_FILTER_TABLE_NAME, DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME,
//                DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME,
//                DatabaseManager.BASIC_FILTER_RESULT_TABLE_NAME);
//        DatabaseManager.getInstance().distinctTable(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Importing RefSeq Genes data...", 6, ALL_STEP);
        SpliceJunctionFilter cf = new SpliceJunctionFilter(manager);
        cf.loadSpliceJunctionTable(DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME, locationPreferences.getRefSeqFile());
//        cf.establishSpliceJunctionResultTable(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
//        cf.executeSpliceJunctionFilter(DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME,
//                DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME, DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME
//                , 2);
//        DatabaseManager.getInstance().distinctTable(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Importing dbSNP data...", 7, ALL_STEP);
        DbsnpFilter sf = new DbsnpFilter(manager);
        sf.loadDbSNPTable(DatabaseManager.DBSNP_FILTER_TABLE_NAME, locationPreferences.getDbSNPFile());
//        sf.establishDbSNPResultTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
//        sf.executeDbSNPFilter(DatabaseManager.DBSNP_FILTER_TABLE_NAME,
//                DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME, DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
//        DatabaseManager.getInstance().distinctTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Executing DNA & RNA Filter...", 8, ALL_STEP);
//        DnaRnaFilter dr = new DnaRnaFilter(manager);
//        dr.establishDnaRnaTable(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);
//        dr.executeDnaRnaFilter(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME, DatabaseManager.DNA_VCF_RESULT_TABLE_NAME,
//                DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
//        DatabaseManager.getInstance().distinctTable(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Executing LLR Filter...", 9, ALL_STEP);
//        LLRFilter lf = new LLRFilter(manager);
//        lf.establishLLRResultTable(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME);
//        lf.executeLLRFilter(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME,
//                DatabaseManager.DNA_VCF_RESULT_TABLE_NAME, DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);
//        DatabaseManager.getInstance().distinctTable(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME);

        progressUpdated("Importing DARNED data...", 10, ALL_STEP);
        PValueFilter pv = new PValueFilter(manager);
        pv.loadDarnedTable(DatabaseManager.PVALUE_FILTER_TABLE_NAME, locationPreferences.getDarnedFile());
//        pv.estblishPValueTable(DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME);
//        if (locationPreferences.getRScriptPath() != null && locationPreferences.getRScriptPath().length() != 0) {
//            pv.executeFDRFilter(DatabaseManager.PVALUE_FILTER_TABLE_NAME,
//                    DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME, DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME,
//                    locationPreferences.getRScriptPath());
//        }

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
