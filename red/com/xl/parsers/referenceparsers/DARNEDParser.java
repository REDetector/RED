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
 * P_value based on alt and ref
 */
public class DARNEDParser extends AbstractParser {
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    public DARNEDParser(String filePath, String tableName) {
        super(filePath, DatabaseManager.KNOWN_RNA_EDITING_TABLE_NAME);
    }

    @Override
    protected void createTable() {
        if (!databaseManager.existTable(tableName)) {
            // "(chrom varchar(15),coordinate int,strand varchar(5),inchr varchar(5), inrna varchar(5)
            // ,index(chrom,coordinate))");
            TableCreator.createReferenceTable(tableName,
                new String[] { "chrom", "pos", "strand", "ref", "alt", "origin" },
                new String[] { "varchar(30)", "int", "varchar(5)", "varchar(5)", "varchar(5)", "varchar(10)" },
                Indexer.CHROM_POSITION);
        }
    }

    @Override
    protected void loadData(ProgressListener listener) {
        String darned = DatabaseManager.DARNED_DATABASE_TABLE_NAME;
        if (!databaseManager.isKnownRnaEditingTableValid(tableName, darned)) {
            createTable();
            try {
                databaseManager.executeSQL("delete from " + tableName + " where origin='" + darned + "'");
                int count = 0;
                databaseManager.setAutoCommit(false);
                FileInputStream inputStream = new FileInputStream(dataPath);
                BufferedReader rin = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                // Skip the first row.
                rin.readLine();
                while ((line = rin.readLine()) != null) {
                    String[] sections = line.trim().split("\\t");
                    StringBuilder stringBuilder = new StringBuilder("insert into ");
                    stringBuilder.append(tableName);
                    stringBuilder.append("(chrom,pos,strand,ref,alt,origin) values(");
                    for (int i = 0; i < 5; i++) {
                        if (i == 0) {
                            stringBuilder.append("'chr").append(sections[i]).append("',");
                        } else if (i == 4) {
                            stringBuilder.append("'").append(sections[i].replace("I", "G")).append("'");
                        } else if (i == 1) {
                            stringBuilder.append(sections[i]).append(",");
                        } else {
                            stringBuilder.append("'").append(sections[i]).append("',");
                        }
                    }
                    stringBuilder.append(",'").append(darned).append("')");

                    databaseManager.executeSQL(stringBuilder.toString());
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                    if (listener != null) {
                        listener.progressUpdated(
                            "Importing " + count + " lines from " + dataPath + " to " + tableName + " table", 0, 0);
                    }
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            } catch (IOException e) {
                logger.error("Error load file from " + dataPath + " to file stream", e);
            } catch (SQLException e) {
                logger.error("Error execute sql clause in " + DARNEDParser.class.getName() + ":loadDarnedTable()", e);
            }
        }
    }

    @Override
    protected void recordInformation() {
        databaseManager.insertOrUpdateKnownREInfo(DatabaseManager.DARNED_DATABASE_TABLE_NAME);
    }
}
