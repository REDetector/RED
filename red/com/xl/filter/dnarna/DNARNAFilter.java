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

package com.xl.filter.dnarna;

import com.xl.database.DatabaseManager;
import com.xl.utils.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * The Class DNARNAFilter is a rule-based filter. RNA-seq variants where its counterparts in genomic DNA is not reference homozygote (e.g., AA) would be
 * excluded if DNA sequencing data is available.
 */
public class DNARNAFilter {
    private final Logger logger = LoggerFactory.getLogger(DNARNAFilter.class);
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager;

    /**
     * Initiate a new DNA-RNA filter.
     *
     * @param databaseManager the database manager
     */
    public DNARNAFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Perform DNA-RNA filter. RNA-seq variants where its counterparts in genomic DNA is not reference homozygote (e.g., AA) would be excluded if DNA sequencing
     * data is available.
     *
     * @param dnaRnaResultTable The result table
     * @param dnaVcfTable       The DNA VCF table
     * @param previousTable     The previous table
     */
    public void executeDnaRnaFilter(String dnaRnaResultTable, String dnaVcfTable, String previousTable) throws SQLException {
        logger.info("Start executing DNARNAFilter... {}", Timer.getCurrentTime());
        databaseManager.executeSQL("insert into " + dnaRnaResultTable + " select * from " + previousTable + " where " +
                "exists (select chrom from " + dnaVcfTable + " where (" + dnaVcfTable + ".chrom=" + previousTable +
                ".chrom and " + dnaVcfTable + ".pos=" + previousTable + ".pos))");
        logger.info("End executing DNARNAFilter... {}", Timer.getCurrentTime());
    }

}
