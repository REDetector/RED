/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.thread;

import com.xl.database.DatabaseManager;
import com.xl.database.DatabaseSelector;
import com.xl.database.TableCreator;
import com.xl.display.dialog.DataImportDialog;
import com.xl.exception.DataLoadException;
import com.xl.filter.denovo.FisherExactTestFilter;
import com.xl.filter.denovo.KnownSNPFilter;
import com.xl.filter.denovo.RepeatRegionsFilter;
import com.xl.filter.denovo.SpliceJunctionFilter;
import com.xl.main.REDApplication;
import com.xl.parsers.dataparsers.RNAVCFParser;
import com.xl.preferences.DatabasePreferences;
import com.xl.preferences.LocationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Created by Xing Li on 2014/7/22.
 * <p/>
 * The Class ThreadDenovoInput generates a new thread to input all data with denovo mode.
 */
public class ThreadDenovoInput implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(ThreadDenovoInput.class);

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        LocationPreferences locationPreferences = LocationPreferences.getInstance();
        DatabasePreferences.getInstance().setCurrentDatabase(DatabaseManager.DENOVO_DATABASE_NAME);
        manager.setAutoCommit(true);
        try {
            manager.createDatabase(DatabaseManager.DENOVO_DATABASE_NAME);
            manager.useDatabase(DatabaseManager.DENOVO_DATABASE_NAME);

            RNAVCFParser rnaVcfParser = new RNAVCFParser();
            rnaVcfParser.parseVCFFile(locationPreferences.getRnaVcfFile());

            RepeatRegionsFilter repeatRegionsFilter = new RepeatRegionsFilter(manager);
            TableCreator.createRepeatRegionsTable(DatabaseManager.REPEAT_MASKER_TABLE_NAME);
            repeatRegionsFilter.loadRepeatTable(DatabaseManager.REPEAT_MASKER_TABLE_NAME, locationPreferences.getRepeatFile());

            SpliceJunctionFilter spliceJunctionFilter = new SpliceJunctionFilter(manager);
            TableCreator.createSpliceJunctionTable(DatabaseManager.SPLICE_JUNCTION_TABLE_NAME);
            spliceJunctionFilter.loadSpliceJunctionTable(DatabaseManager.SPLICE_JUNCTION_TABLE_NAME, locationPreferences.getRefSeqFile());

            KnownSNPFilter knownSNPFilter = new KnownSNPFilter(manager);
            TableCreator.createDBSNPTable(DatabaseManager.DBSNP_DATABASE_TABLE_NAME);
            knownSNPFilter.loadDbSNPTable(DatabaseManager.DBSNP_DATABASE_TABLE_NAME, locationPreferences.getDbSNPFile());

            FisherExactTestFilter fisherExactTestFilter = new FisherExactTestFilter(manager);
            TableCreator.createDARNEDTable(DatabaseManager.DARNED_DATABASE_TABLE_NAME);
            fisherExactTestFilter.loadDarnedTable(DatabaseManager.DARNED_DATABASE_TABLE_NAME, locationPreferences.getDarnedFile());
        } catch (DataLoadException e) {
            JOptionPane.showMessageDialog(REDApplication.getInstance(), "Sorry, fail to import the data to database. You may select one of wrong " +
                    "path for the relative data.", "Imported Failed", JOptionPane.ERROR_MESSAGE);
            logger.error("", e);
            new DataImportDialog(REDApplication.getInstance());
            return;
        }
        new DatabaseSelector(REDApplication.getInstance());
    }

}
