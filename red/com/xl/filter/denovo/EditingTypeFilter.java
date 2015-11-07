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

package com.xl.filter.denovo;

import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xl.database.DatabaseManager;
import com.xl.filter.Filter;
import com.xl.utils.NegativeType;
import com.xl.utils.Timer;

/**
 * Created by Xing Li on 2014/9/29.
 * <p>
 * The Class EditingTypeFilter is a rule-based filter that user enables to select the type of RNA editing as his
 * preference.
 */
public class EditingTypeFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(EditingTypeFilter.class);
    public static final String PARAMS_REF = "ref";

    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Perform editing type filter as user's preference. We just keep the type 'ref'->'alt' and the gene type is
     * homozygous at the same time.
     *
     * @param previousTable The table name of previous filter stored in the database.
     * @param currentTable  The table name of this filter stored in the database.
     * @param params        The reference base
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return;
        }

        logger.info("Start executing Editing Type Filter..." + Timer.getCurrentTime());
        try {
            String refAlt = params.get(PARAMS_REF);
            if (refAlt.equalsIgnoreCase("all")) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("insert into ");
                stringBuilder.append(currentTable);
                stringBuilder.append(" select * from ");
                stringBuilder.append(previousTable);
                databaseManager.insertClause(stringBuilder.toString());
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("insert into ");
                stringBuilder.append(currentTable);
                stringBuilder.append(" select * from ");
                stringBuilder.append(previousTable);
                stringBuilder.append(" WHERE REF='");
                stringBuilder.append(refAlt.substring(0, 1));
                stringBuilder.append("' AND ALT='");
                stringBuilder.append(refAlt.substring(1));
                stringBuilder.append("'");
                // stringBuilder.append("' AND GT!='0/0'");
                databaseManager.insertClause(stringBuilder.toString());

                String refAlt2 = NegativeType.getNegativeStrandEditingType(refAlt);
                stringBuilder = new StringBuilder();
                stringBuilder.append("insert into ");
                stringBuilder.append(currentTable);
                stringBuilder.append(" select * from ");
                stringBuilder.append(previousTable);
                stringBuilder.append(" WHERE REF='");
                stringBuilder.append(refAlt2.substring(0, 1));
                stringBuilder.append("' AND ALT='");
                stringBuilder.append(refAlt2.substring(1));
                stringBuilder.append("'");
                // stringBuilder.append("' AND GT!='0/0'");
                databaseManager.insertClause(stringBuilder.toString());
            }
        } catch (SQLException e) {
            logger.error("There is a syntax error for SQL clause", e);
        }
        logger.info("End executing Editing Type Filter..." + Timer.getCurrentTime());
    }

    @Override
    public String getName() {
        return DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME;
    }
}
