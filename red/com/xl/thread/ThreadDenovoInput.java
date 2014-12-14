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
import com.xl.filter.denovo.FisherExactTestFilter;
import com.xl.filter.denovo.KnownSNPFilter;
import com.xl.filter.denovo.RepeatRegionsFilter;
import com.xl.filter.denovo.SpliceJunctionFilter;
import com.xl.main.REDApplication;
import com.xl.parsers.dataparsers.RNAVCFMultiParser;
import com.xl.preferences.LocationPreferences;

/**
 * Created by Xing Li on 2014/7/22.
 */
public class ThreadDenovoInput implements Runnable {

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        LocationPreferences locationPreferences = LocationPreferences.getInstance();
        manager.setAutoCommit(true);

        manager.createDatabase(DatabaseManager.DENOVO_DATABASE_NAME);
        manager.useDatabase(DatabaseManager.DENOVO_DATABASE_NAME);

        RNAVCFMultiParser multiParser = new RNAVCFMultiParser();
        multiParser.parseMultiVCFFile(locationPreferences.getRnaVcfFile());

//        RNAVCFParser rnaVCFParser = new RNAVCFParser();
//        rnaVCFParser.parseVCFFile(sampleName + "_" + DatabaseManager.RNA_VCF_RESULT_TABLE_NAME, locationPreferences.getRnaVcfFile());

        RepeatRegionsFilter rf = new RepeatRegionsFilter(manager);
        TableCreator.createRepeatRegionsTable(DatabaseManager.REPEAT_MASKER_TABLE_NAME);
        rf.loadRepeatTable(DatabaseManager.REPEAT_MASKER_TABLE_NAME, locationPreferences.getRepeatFile());

        SpliceJunctionFilter cf = new SpliceJunctionFilter(manager);
        TableCreator.createSpliceJunctionTable(DatabaseManager.SPLICE_JUNCTION_TABLE_NAME);
        cf.loadSpliceJunctionTable(DatabaseManager.SPLICE_JUNCTION_TABLE_NAME, locationPreferences.getRefSeqFile());

        KnownSNPFilter sf = new KnownSNPFilter(manager);
        TableCreator.createDBSNPTable(DatabaseManager.DBSNP_DATABASE_TABLE_NAME);
        sf.loadDbSNPTable(DatabaseManager.DBSNP_DATABASE_TABLE_NAME, locationPreferences.getDbSNPFile());

        FisherExactTestFilter pv = new FisherExactTestFilter(manager);
        TableCreator.createDARNEDTable(DatabaseManager.DARNED_DATABASE_TABLE_NAME);
        pv.loadDarnedTable(DatabaseManager.DARNED_DATABASE_TABLE_NAME, locationPreferences.getDarnedFile());

        new DatabaseSelector(REDApplication.getInstance());
    }

}
