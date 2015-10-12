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

package com.xl.main;

/**
 * Created by Administrator on 2015/10/12.
 */

import com.xl.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Administrator on 2015/1/12.
 */
public class DataExporter {
    private static final Logger logger = LoggerFactory.getLogger(DataExporter.class);
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    public void exportData(String resultPath, String databaseName, String mode, String[] columns, String selection,
        String[] selectionArgs) {
        if (columns == null || columns.length == 0) {
            logger.warn("Incomplete parameters for columns");
            return;
        }
        List<String> currentTables;
        databaseManager.useDatabase(databaseName);
        currentTables = DatabaseManager.getInstance().getCurrentTables(databaseName);
        for (String currentTable : currentTables) {
            String sample = DatabaseManager.getInstance().getSampleName(currentTable);
            StringBuilder builder = new StringBuilder(sample);
            for (String column : columns) {
                builder.append("_").append(column);
            }
            if (mode.equalsIgnoreCase("denovo")) {
                builder.append("_denovo");
            } else {
                builder.append("_dnarna");
            }
            builder.append(".txt");

            ResultSet rs;
            PrintWriter pw = null;
            if (columns.length == 1 && columns[0].equalsIgnoreCase("all")) {
                if (currentTable.contains(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME)
                    && !currentTable.contains(DatabaseManager.FILTER)) {
                    logger.info("Export data for : " + builder.toString());
                    File f = new File(resultPath + File.separator + builder.toString());
                    try {
                        pw = new PrintWriter(new FileWriter(f));
                    } catch (IOException e) {
                        logger.error("Error open the print writer at: " + f.getAbsolutePath(), e);
                        return;
                    }
                    rs = databaseManager.query(currentTable, null, selection, selectionArgs);
                    List<String> columnNames;
                    try {
                        columnNames = databaseManager.getColumnNames(databaseName, currentTable);
                    } catch (SQLException e) {
                        logger.error("Could not get the column names from table '" + currentTable + "'", e);
                        return;
                    }
                    builder = new StringBuilder();
                    for (String column : columnNames) {
                        builder.append(column).append("\t");
                    }
                    pw.println(builder.toString().trim());
                    try {
                        while (rs.next()) {
                            builder = new StringBuilder();
                            for (String column : columnNames) {
                                builder.append(rs.getString(column)).append("\t");
                            }
                            pw.println(builder.toString().trim());
                        }
                    } catch (SQLException e) {
                        logger.warn("No results", e);
                    }
                }
            } else if (columns.length == 1 && columns[0].equalsIgnoreCase("annotation")) {
                if (currentTable.contains(DatabaseManager.FET_FILTER_RESULT_TABLE_NAME)) {
                    File f = new File(resultPath + File.separator + builder.toString());
                    try {
                        pw = new PrintWriter(new FileWriter(f));
                    } catch (IOException e) {
                        logger.error("Error open the print writer at: " + f.getAbsolutePath(), e);
                        return;
                    }
                    pw.println("chr\tstart\tend\tref_allele\talt_allele");
                    rs = databaseManager.query(currentTable, new String[] { "chrom", "pos", "ref", "alt" }, selection,
                        selectionArgs);
                    try {
                        while (rs.next()) {
                            pw.println(rs.getString(1).substring(3) + "\t" + rs.getInt(2) + "\t" + rs.getInt(2) + "\t"
                                + rs.getString(3) + "\t" + rs.getString(4));
                        }
                    } catch (SQLException e) {
                        logger.warn("No results", e);
                    }
                }
            } else {
                if (currentTable.contains(DatabaseManager.FET_FILTER_RESULT_TABLE_NAME)) {
                    File f = new File(resultPath + File.separator + builder.toString());
                    try {
                        pw = new PrintWriter(new FileWriter(f));
                    } catch (IOException e) {
                        logger.error("Error open the print writer at: " + f.getAbsolutePath(), e);
                        return;
                    }
                    rs = databaseManager.query(currentTable, columns, selection, selectionArgs);
                    builder = new StringBuilder();
                    for (String column : columns) {
                        builder.append(column).append("\t");
                    }
                    pw.println(builder.toString().trim());

                    try {
                        while (rs.next()) {
                            builder = new StringBuilder();
                            for (String column : columns) {
                                builder.append(rs.getString(column)).append("\t");
                            }
                            pw.println(builder.toString().trim());
                        }
                    } catch (SQLException e) {
                        logger.warn("No results", e);
                    }
                }
            }
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
    }

    private class StrandSite {
        private String chr;
        private String pos;
        private String ref;
        private String alt;
        private String strand;
        private String name;

        public StrandSite(String name, String chr, String pos, String ref, String alt) {
            this.name = name;
            this.chr = chr;
            this.pos = pos;
            this.ref = ref;
            this.alt = alt;
        }

        public void setStrand(String strand) {
            this.strand = strand;
        }

        @Override
        public String toString() {
            return chr.substring(3) + "\t" + pos + "\t" + ref + "\t" + alt + "\t" + strand + "\t" + name;
        }
    }
}
