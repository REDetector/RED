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

package com.xl.filter.denovo;

import com.xl.database.DatabaseManager;
import com.xl.display.dialog.ProgressDialog;
import com.xl.display.dialog.REDProgressBar;
import com.xl.exception.DataLoadException;
import com.xl.utils.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The Class KnownSNPFilter is a rule-based filter that will filter out the site which was known SNP in DNA level for eliminating germline variants.
 */
public class KnownSNPFilter {
    private final Logger logger = LoggerFactory.getLogger(KnownSNPFilter.class);
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager;
    /**
     * The progress bar for loading dbSNP database.
     */
    private REDProgressBar progressBar = REDProgressBar.getInstance();

    /**
     * Initiate a new known SNP filter.
     *
     * @param databaseManager the database manager
     */
    public KnownSNPFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Check whether the dbSNP file has been loaded into database by calculating the row count of this table.
     *
     * @param dbSnpTable The dbSNP database table name, it is constant.
     * @return true if dbSNP data exists in the database.
     */
    public boolean hasEstablishDbSNPTable(String dbSnpTable) {
        return databaseManager.getRowCount(dbSnpTable) > 0;
    }


    /**
     * Load dbSNP file into database.
     *
     * @param dbSNPTable The dbSNP database table name, it is constant.
     * @param dbSNPPath  The dbSNP file path.
     */
    public void loadDbSNPTable(String dbSNPTable, String dbSNPPath) throws DataLoadException {
        logger.info("Start loading dbSNPTable... {}", Timer.getCurrentTime());
        progressBar.addProgressListener(new ProgressDialog("Import dbSNP file into database..."));
        int count = 0;
        BufferedReader rin = null;
        try {
            progressBar.progressUpdated("Start loading dbSNP data from " + dbSNPPath + " to " + dbSNPTable + " table", 0, 0);
            if (!hasEstablishDbSNPTable(dbSNPTable)) {
                rin = new BufferedReader(new InputStreamReader(new FileInputStream(dbSNPPath)));
                String line;
                while ((line = rin.readLine()) != null) {
                    if (line.startsWith("#")) {
                        count++;
                    } else {
                        break;
                    }
                }
                progressBar.progressUpdated("Importing dbSNP data from " + dbSNPPath + " to " + dbSNPTable + " table", 0, 0);
                databaseManager.executeSQL("load data local infile '" + dbSNPPath + "' into table " + dbSNPTable + " fields terminated by '\t' lines " +
                        "terminated by '\n' IGNORE " + count + " LINES");
            }

        } catch (IOException e) {
            DataLoadException de = new DataLoadException("Error load file", dbSNPPath);
            logger.error("Error load file from " + dbSNPPath + " to file stream", de);
            throw de;
        } finally {
            if (rin != null) {
                try {
                    rin.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
        progressBar.progressComplete("dbsnp_loaded", null);
        logger.info("End loading dbSNPTable... {}", Timer.getCurrentTime());
    }

    /**
     * Perform dbSNP filter. We filter out the site in previous filter which is in dbSNP database at the same time.
     *
     * @param dbSnpTable       The dbSNP database table name, it is constant.
     * @param dbSnpResultTable The result table
     * @param previousTable    The previous table
     */
    public void executeDbSNPFilter(String dbSnpTable, String dbSnpResultTable, String previousTable) {
        logger.info("Start executing KnownSNPFilter...... {}", Timer.getCurrentTime());
        databaseManager.executeSQL("insert into " + dbSnpResultTable + " select * from " + previousTable + " where not exists (select chrom from " +
                dbSnpTable + " where (" + dbSnpTable + ".chrom=" + previousTable + ".chrom and " + dbSnpTable + ".pos=" + previousTable + ".pos))");
        logger.info("End executing KnownSNPFilter...... {}", Timer.getCurrentTime());
    }

}
