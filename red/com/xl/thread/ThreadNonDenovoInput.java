package com.xl.thread;

import com.dw.dbutils.DatabaseManager;
import com.dw.dbutils.Query;
import com.dw.denovo.FisherExactTestFilter;
import com.dw.denovo.KnownSNPFilter;
import com.dw.denovo.RepeatRegionsFilter;
import com.dw.denovo.SpliceJunctionFilter;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteSet;
import com.xl.main.REDApplication;
import com.xl.parsers.dataparsers.DNAVCFParser;
import com.xl.parsers.dataparsers.RNAVCFParser;
import com.xl.preferences.LocationPreferences;
import com.xl.preferences.REDPreferences;

import java.util.Vector;

/**
 * Created by Administrator on 2014/7/22.
 */
public class ThreadNonDenovoInput implements Runnable {
    protected boolean cancel = false;
    private DataCollection dataCollection;

    public ThreadNonDenovoInput(DataCollection dataCollection) {
        this.dataCollection = dataCollection;
    }

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        LocationPreferences locationPreferences = LocationPreferences.getInstance();
        REDPreferences.getInstance().setDenovo(false);
        manager.setAutoCommit(true);
        manager.createDatabase(DatabaseManager.NON_DENOVO_DATABASE_NAME);
        manager.useDatabase(DatabaseManager.NON_DENOVO_DATABASE_NAME);

        DNAVCFParser dnaVCFParser = new DNAVCFParser();
        dnaVCFParser.parseVCFFile(DatabaseManager.DNA_VCF_RESULT_TABLE_NAME, locationPreferences.getDnaVcfFile());

        RNAVCFParser rnaVCFParser = new RNAVCFParser();
        rnaVCFParser.parseVCFFile(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME, locationPreferences.getRnaVcfFile());

        Vector<Site> sites = Query.queryAllEditingSites(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
        Site[] siteArray = sites.toArray(new Site[0]);
        dataCollection.setSiteSet(new SiteSet("Original RNA editing sites by RNA vcf file", siteArray, DatabaseManager.RNA_VCF_RESULT_TABLE_NAME));


        RepeatRegionsFilter rf = new RepeatRegionsFilter(manager);
        rf.loadRepeatTable(DatabaseManager.REPEAT_FILTER_TABLE_NAME, locationPreferences.getRepeatFile());

        SpliceJunctionFilter cf = new SpliceJunctionFilter(manager);
        cf.loadSpliceJunctionTable(DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME, locationPreferences.getRefSeqFile());

        KnownSNPFilter sf = new KnownSNPFilter(manager);
        sf.loadDbSNPTable(DatabaseManager.DBSNP_FILTER_TABLE_NAME, locationPreferences.getDbSNPFile());

        FisherExactTestFilter pv = new FisherExactTestFilter(manager);
        pv.loadDarnedTable(DatabaseManager.PVALUE_FILTER_TABLE_NAME, locationPreferences.getDarnedFile());

        REDApplication.getInstance().progressComplete("database_loaded", null);

    }
}
