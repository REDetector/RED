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
 * we will filter out base in repeated area except for SINE/alu
 */
public class RepeatMaskerParser extends AbstractParser {
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    public RepeatMaskerParser(String dataPath, String tableName) {
        super(dataPath, tableName);
    }

    @Override
    protected void createTable() {
        if (!databaseManager.existTable(tableName)) {
            // chrom varchar(30),begin int,end int,type varchar(40),index(chrom,begin,end);
            TableCreator.createReferenceTable(tableName, new String[] { "chrom", "begin", "end", "type" },
                new String[] { "varchar(30)", "int", "int", "varchar(40)" }, Indexer.CHROM_BEGIN_END);
        }
    }

    @Override
    protected void loadData(ProgressListener listener) {
        BufferedReader rin = null;
        try {
            if (!databaseManager.isTableExistAndValid(tableName)) {
                createTable();
                databaseManager.setAutoCommit(false);
                int count = 0;
                FileInputStream inputStream = new FileInputStream(dataPath);
                rin = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                rin.readLine();
                rin.readLine();
                rin.readLine();
                while ((line = rin.readLine()) != null) {
                    String section[] = line.trim().split("\\s+");
                    databaseManager.executeSQL("insert into " + tableName + "(chrom,begin,end,type) values('"
                        + section[4] + "','" + section[5] + "','" + section[6] + "','" + section[10] + "')");
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            }

        } catch (IOException e) {
            logger.error("Error load file from " + dataPath + " to file stream", e);
        } catch (SQLException e) {
            logger.error("Error execute sql clause in " + RepeatMaskerParser.class.getName() + ":loadRepeatTable()", e);
        } finally {
            if (rin != null) {
                try {
                    rin.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }
}
