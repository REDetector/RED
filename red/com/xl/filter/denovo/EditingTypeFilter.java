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
import com.xl.utils.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by Xing Li on 2014/9/29.
 * <p/>
 * The Class EditingTypeFilter is a rule-based filter that user enables to select the type of RNA editing as his preference.
 */
public class EditingTypeFilter {
    private final Logger logger = LoggerFactory.getLogger(EditingTypeFilter.class);
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager;

    /**
     * Initiate a new editing type filter.
     *
     * @param databaseManager the database manager.
     */
    public EditingTypeFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Perform editing type filter as user's preference. We just keep the type 'ref'->'alt' and the gene type is homozygous at the same time.
     *
     * @param editingTypeTable The table name of this filter stored in the database.
     * @param previousTable    The table name of previous filter stored in the database.
     * @param ref              The reference base
     * @param alt              The alternative base
     */
    public void executeEditingTypeFilter(String editingTypeTable, String previousTable, String ref, String alt) {
        logger.info("Start executing EditingTypeFilter... {}", Timer.getCurrentTime());
        String sqlClause = "insert into " + editingTypeTable + " select * from " + previousTable + " WHERE REF='" + ref + "' AND ALT='" + alt + "' AND GT!='0/0'";
        try {
            databaseManager.insert(sqlClause);
        } catch (SQLException e) {
            logger.error("Error execute SQL clause: " + sqlClause);
        }
        logger.info("End executing EditingTypeFilter... {}", Timer.getCurrentTime());
    }

}
