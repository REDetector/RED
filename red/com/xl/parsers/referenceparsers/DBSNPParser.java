/*
 * REFilters: RNA Editing Filters Copyright (C) <2014> <Xing Li>
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

package com.xl.parsers.referenceparsers;

import com.xl.database.DatabaseManager;
import com.xl.database.TableCreator;
import com.xl.interfaces.ProgressListener;
import com.xl.utils.Indexer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

/**
 * we will filter out base which already be recognized
 */
public class DBSNPParser extends AbstractParser {
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    public DBSNPParser(String dataPath, String tableName) {
        super(dataPath, tableName);
    }

    @Override
    protected void createTable() {
        if (!databaseManager.existTable(tableName)) {
            // chrom varchar(15),pos int,index(chrom,pos);
            TableCreator.createReferenceTable(tableName, new String[] { "chrom", "pos" }, new String[] { "varchar(30)",
                "int" }, Indexer.CHROM_POSITION);
        }
    }

    @Override
    protected void loadData(ProgressListener listener) {
        try {
            if (!databaseManager.isTableExistAndValid(tableName)) {
                createTable();
                int count = 0;
                FileInputStream inputStream = new FileInputStream(dataPath);
                BufferedReader rin = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = rin.readLine()) != null) {
                    if (line.startsWith("#")) {
                        count++;
                    } else {
                        break;
                    }
                }
                rin.close();
                if (listener != null) {
                    listener.progressUpdated("Start loading dbSNP data from " + dataPath + " to " + tableName
                            + " table", 0, 0);
                }
                databaseManager.executeSQL("load data local infile '" + dataPath + "' into table " + tableName + ""
                    + " fields terminated by '\t' lines terminated by '\n' IGNORE " + count + " LINES");
            }
        } catch (IOException e) {
            logger.error("Error load file from " + dataPath + " to file stream", e);
        } catch (SQLException e) {
            logger.error("Error execute sql clause in " + DBSNPParser.class.getName() + ":loadDbSNPTable()", e);
        }
    }

    @Override
    protected void recordInformation() {
        databaseManager.insertOrUpdateInfo(tableName);
    }
}
