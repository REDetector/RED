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

import java.sql.SQLException;

/**
 * Comprehensive phase we focus on base in exon we discard base in the rear or front of the sequence
 */

public class GTFParser extends AbstractParser {
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    public GTFParser(String dataPath, String tableName) {
        super(dataPath, tableName);
    }

    @Override
    protected void createTable() {
        if (!databaseManager.existTable(tableName)) {
            // "(chrom varchar(15),ref varchar(30),type varchar(9),begin int,end int,unuse1 float(8,6),unuse2
            // varchar(5),unuse3 varchar(5),
            // info varchar(100),index(chrom,type))");
            TableCreator.createReferenceTable(tableName,
                new String[] { "chrom", "ref", "type", "begin", "end", "score", "strand", "frame", "info" },
                new String[] { "varchar(30)", "varchar(30)", "varchar(10)", "int", "int", "float(8,6)", "varchar(1)",
                    "varchar(1)", "varchar(100)" },
                Indexer.CHROM_TYPE);
        }
    }

    @Override
    protected void loadData(ProgressListener listener) {
        if (!databaseManager.isTableExistAndValid(tableName)) {
            createTable();
            try {
                databaseManager.executeSQL("load data local infile '" + dataPath + "' into table " + tableName
                    + " fields terminated" + " by '\t' lines terminated by '\n'");
                if (listener != null) {
                    listener.progressUpdated(
                        "Start loading Gene Annotation data from " + dataPath + " to " + tableName + " table", 0, 0);
                }
            } catch (SQLException e) {
                logger.error("Error execute sql clause in " + GTFParser.class.getName() + ":loadSpliceJunctionTable().",
                    e);
            }
        }
    }

    @Override
    protected void recordInformation() {
        databaseManager.insertOrUpdateInfo(tableName);
    }
}
