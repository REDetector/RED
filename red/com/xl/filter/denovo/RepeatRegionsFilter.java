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
 * The Class RepeatRegionsFilter is a rule-based filter. Variants that were within repeat regions were excluded. However, sites in SINE/Alu regions were
 * remained since A-I RNA editing is pervasive in Alu repeats and it has been implicated in human diseases such as breast cancer and Ewing's sarcoma.
 */
public class RepeatRegionsFilter {
    private final Logger logger = LoggerFactory.getLogger(RepeatRegionsFilter.class);
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager;
    /**
     * The progress bar for loading repeat regions file from RepeatMasker.
     */
    private REDProgressBar progressBar = REDProgressBar.getInstance();

    /**
     * Initiate a new repeat regions filter.
     *
     * @param databaseManager the database manager
     */
    public RepeatRegionsFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Check whether the repeat file has been loaded into database by calculating the row count of this table.
     *
     * @param repeatTable The repeat file table name, it is constant.
     * @return true if repeat data exists in the database.
     */
    public boolean hasEstablishedRepeatTable(String repeatTable) {
        return databaseManager.getRowCount(repeatTable) > 0;
    }

    /**
     * Load repeat file into database.
     *
     * @param repeatTable The repeat file table name, it is constant.
     * @param repeatPath  The repeat file path.
     */
    public void loadRepeatTable(String repeatTable, String repeatPath) throws DataLoadException {
        logger.info("Start loading RepeatTable... {}", Timer.getCurrentTime());
        progressBar.addProgressListener(new ProgressDialog("Import repeat regions file into database..."));
        progressBar.progressUpdated("Start loading repeated region data from " + repeatPath + " to " + repeatTable + " table", 0, 0);
        BufferedReader rin = null;
        try {
            if (!hasEstablishedRepeatTable(repeatTable)) {
                databaseManager.setAutoCommit(false);
                int count = 0;
                FileInputStream inputStream = new FileInputStream(repeatPath);
                rin = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                rin.readLine();
                rin.readLine();
                rin.readLine();
                while ((line = rin.readLine()) != null) {
                    String section[] = line.trim().split("\\s+");
                    if (count % 1000 == 0) {
                        progressBar.progressUpdated("Importing " + count + " lines from " + repeatPath + " to " + repeatTable + " table", 0, 0);
                    }
                    databaseManager.executeSQL("insert into " + repeatTable + "(chrom,begin,end,type) values('" + section[4] + "','" + section[5] + "'," +
                            "'" + section[6] + "','" + section[10] + "')");
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            }

        } catch (IOException e) {
            DataLoadException de = new DataLoadException("Error load file", repeatPath);
            logger.error("Error load file from " + repeatPath + " to file stream", de);
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
        progressBar.progressComplete("repeat_loaded", null);
        logger.info("End loading RepeatTable... {}", Timer.getCurrentTime());
    }

    /**
     * Perform repeat regions filter. Variants that were within repeat regions were excluded.
     *
     * @param repeatTable       The repeat file table name, it is constant.
     * @param repeatResultTable The repeat result table
     * @param aluResultTable    The Alu result table
     * @param previousTable     The previous table
     */
    public void executeRepeatFilter(String repeatTable, String repeatResultTable, String aluResultTable, String previousTable) {
        logger.info("Start executing RepeatRegionsFilter... {}", Timer.getCurrentTime());
        databaseManager.executeSQL("insert into " + repeatResultTable + " select * from " + previousTable + " where not exists (select * from " +
                repeatTable + " where (" + repeatTable + ".chrom= " + previousTable + ".chrom and  " + repeatTable + ".begin<=" + previousTable + ".pos and " +
                repeatTable + ".end>=" + previousTable + ".pos)) ");

        logger.info("Start executing AluFilter... {}", Timer.getCurrentTime());

        databaseManager.executeSQL("insert into " + aluResultTable + " SELECT * from " + previousTable + " where exists (select chrom from " + repeatTable
                + " where " + repeatTable + ".chrom = " + previousTable + ".chrom and " + repeatTable + ".begin<=" + previousTable + ".pos and " + repeatTable
                + ".end>=" + previousTable + ".pos and " + repeatTable + ".type='SINE/Alu')");

        logger.info("End executing AluFilter... {}", Timer.getCurrentTime());

        databaseManager.executeSQL("update " + aluResultTable + " set alu = 'T'");
        databaseManager.executeSQL("insert into " + repeatResultTable + " select * from " + aluResultTable);
        databaseManager.executeSQL("drop table if exists " + aluResultTable);

        logger.info("End executing RepeatRegionsFilter... {}", Timer.getCurrentTime());
    }
}
