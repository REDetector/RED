/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 * 
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xl.database.DatabaseManager;
import com.xl.database.DatabaseSelector;
import com.xl.display.dialog.DataImportDialog;
import com.xl.display.dialog.ProgressDialog;
import com.xl.exception.DataLoadException;
import com.xl.exception.RedException;
import com.xl.main.RedApplication;
import com.xl.parsers.referenceparsers.AbstractParser;
import com.xl.parsers.referenceparsers.DnaVcfParser;
import com.xl.parsers.referenceparsers.ParserFactory;
import com.xl.parsers.referenceparsers.RnaVcfParser;
import com.xl.preferences.DatabasePreferences;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.ui.OptionDialogUtils;

/**
 * Created by Xing Li on 2014/7/22.
 * <p/>
 * The Class ThreadDnaRnaInput generates a new thread to input all data with DNA-RNA mode.
 */
public class ThreadDnaRnaInput implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(ThreadDnaRnaInput.class);

    @Override
    public void run() {
        logger.info("DNA-RNA mode Input Start.");
        DatabaseManager manager = DatabaseManager.getInstance();
        LocationPreferences locationPreferences = LocationPreferences.getInstance();
        DatabasePreferences.getInstance().setCurrentDatabase(DatabaseManager.DNA_RNA_MODE_DATABASE_NAME);
        manager.setAutoCommit(true);
        try {
            manager.createDatabase(DatabaseManager.DNA_RNA_MODE_DATABASE_NAME);
            manager.useDatabase(DatabaseManager.DNA_RNA_MODE_DATABASE_NAME);

            AbstractParser rnaVcfParser = ParserFactory.createParser(locationPreferences.getRnaVcfFile(),
                DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
            AbstractParser dnaVcfParser = ParserFactory.createParser(locationPreferences.getDnaVcfFile(),
                DatabaseManager.DNA_VCF_RESULT_TABLE_NAME);
            AbstractParser repeatParser = ParserFactory.createParser(locationPreferences.getRepeatFile(),
                DatabaseManager.REPEAT_MASKER_TABLE_NAME);
            AbstractParser spliceParser = ParserFactory.createParser(locationPreferences.getRefSeqFile(),
                DatabaseManager.SPLICE_JUNCTION_TABLE_NAME);
            AbstractParser dbSNPParser = ParserFactory.createParser(locationPreferences.getDbSNPFile(),
                DatabaseManager.DBSNP_DATABASE_TABLE_NAME);
            AbstractParser darnedParser = ParserFactory.createParser(locationPreferences.getDarnedFile(),
                DatabaseManager.DARNED_DATABASE_TABLE_NAME);
            AbstractParser radarParser = ParserFactory.createParser(locationPreferences.getRadarFile(),
                DatabaseManager.RADAR_DATABASE_TABLE_NAME);

            rnaVcfParser.loadDataFromLocal(new ProgressDialog("Import RNA VCF file into database..."));
            String[] rnaVcfSamples = ((RnaVcfParser) rnaVcfParser).getSampleNames();
            dnaVcfParser.loadDataFromLocal(new ProgressDialog("Import DNA VCF file into database..."));
            String[] dnaVcfSamples = ((DnaVcfParser) dnaVcfParser).getSampleNames();

            if (!sampleMatches(rnaVcfSamples, dnaVcfSamples)) {
                throw new RedException(
                    "Samples in DNA VCF file does not match the RNA VCF, please check the sample name.");
            }
            repeatParser.loadDataFromLocal(new ProgressDialog("Import Repeat Masker file into database..."));
            spliceParser.loadDataFromLocal(new ProgressDialog("Import Ref Seq Gene file into database..."));
            dbSNPParser.loadDataFromLocal(new ProgressDialog("Import dbSNP file into database..."));
            darnedParser.loadDataFromLocal(new ProgressDialog("Import DARNED file into database..."));
            radarParser.loadDataFromLocal(new ProgressDialog("Import RADAR file into database..."));
        } catch (RedException e) {
            logger.error("", e);
        } catch (DataLoadException e) {
            OptionDialogUtils.showErrorDialog(RedApplication.getInstance(),
                "Sorry, fail to import the data to database. You may select one of wrong "
                    + "path for the relative data.");
            logger.error("", e);
            new DataImportDialog(RedApplication.getInstance());
            return;
        }
        new DatabaseSelector(RedApplication.getInstance());
    }

    private boolean sampleMatches(String[] rnaVcfSamples, String[] dnaVcfSamples) {
        boolean match = false;
        for (String rnaSample : rnaVcfSamples) {
            match = false;
            for (String dnaSample : dnaVcfSamples) {
                if (rnaSample.equals(dnaSample)) {
                    match = true;
                }
            }
        }
        return match;
    }
}
