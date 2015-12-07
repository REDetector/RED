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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.xl.database.DatabaseManager;
import com.xl.database.TableCreator;
import com.xl.exception.DataLoadException;
import com.xl.interfaces.ProgressListener;
import com.xl.utils.EmptyChecker;
import com.xl.utils.Indexer;

/**
 * Created by Administrator on 2014/9/29.
 * <p/>
 * The Class RnaVcfParser can parser RNA VCF file with single or multiple samples in a file, then insert all data into
 * database. Pay attention that the class will delete old sample tables and create new ones for all samples in this RNA
 * VCF file..
 */
public class RnaVcfParser extends AbstractParser {
    // public static final String VCF_ID = "ID";
    // public static final String VCF_REF = "REF";
    // public static final String VCF_ALT = "ALT";
    // public static final String VCF_QUAL = "QUAL";
    // public static final String VCF_FILTER = "FILTER";
    // public static final String VCF_INFO = "INFO";
    // public static final String VCF_FORMAT = "FORMAT";
    // private int chromColumn = 0;
    // private int posColumn = 1;
    // private int idColumn = 2;
    private int refColumn = 3;
    private int altColumn = 4;
    // private int qualColumn = 5;
    // private int filterColumn = 6;
    private int infoColumn = 7;
    private int formatColumnIndex = 8;

    private String[] sampleNames = null;
    private int columnLength = 0;
    private String[] tableNames = null;
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    public RnaVcfParser(String dataPath) {
        super(dataPath, DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
    }

    @Override
    protected void createTable() {
    }

    @Override
    protected void loadData(ProgressListener listener) {
        BufferedReader bufferedReader = null;
        StringBuilder sqlClause = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath)));
            String line;
            String[] columnStrings = new String[0];
            int countIndex = -1;
            int lineCount = 0;
            boolean hasGetIndexOfDp = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    columnStrings = line.substring(1).split("\\t");
                    columnLength = columnStrings.length;
                    sampleNames = Arrays.copyOfRange(columnStrings, formatColumnIndex + 1, columnLength);
                    tableNames = new String[sampleNames.length];
                    for (int j = 0, len = sampleNames.length; j < len; j++) {
                        tableNames[j] = sampleNames[j] + "_" + DatabaseManager.RNA_VCF_RESULT_TABLE_NAME;
                    }
                    if (isDataValid(tableNames)) {
                        logger.info("All tables are valid, return");
                        return;
                    } else {
                        for (int j = 0, len = sampleNames.length; j < len; j++) {
                            databaseManager.deleteTable(tableNames[j]);
                            TableCreator.createRnaVcfTable(tableNames[j]);
                        }
                    }

                    databaseManager.setAutoCommit(false);
                    continue;
                }
                if (sampleNames == null) {
                    logger.error("There are no samples in this vcf file.");
                    throw new DataLoadException("There are no samples in this vcf file.");
                }

                String[] sections = line.split("\\t");

                if (!hasGetIndexOfDp) {
                    if (sections[formatColumnIndex].contains("AD")) {
                        countIndex = findIndexOfAd(sections[formatColumnIndex]);
                    } else if (sections[formatColumnIndex].contains("DP4")) {
                        countIndex = findIndexOfDp4(sections[formatColumnIndex]);
                    }

                    if (countIndex == -1) {
                        throw new DataLoadException("This VCF file does not contain enough information of AD/DP4.");
                    }
                    hasGetIndexOfDp = true;
                }

                if (sections[altColumn].equals(".") || sections[refColumn].length() != 1
                    || sections[altColumn].length() != 1) {
                    continue;
                }

                for (int i = formatColumnIndex + 1; i < columnLength; i++) {

                    if (sections[i].contains(".")) {
                        continue;
                    }

                    String[] refAltCounts = sections[i].split(":")[countIndex].split(",");
                    int refCount;
                    int altCount;
                    if (refAltCounts.length == 2) {
                        refCount = Integer.parseInt(refAltCounts[0]);
                        altCount = Integer.parseInt(refAltCounts[1]);
                    } else if (refAltCounts.length == 4) {
                        refCount = Integer.parseInt(refAltCounts[0]) + Integer.parseInt(refAltCounts[1]);
                        altCount = Integer.parseInt(refAltCounts[2]) + Integer.parseInt(refAltCounts[3]);
                    } else {
                        continue;
                    }

                    // insert into
                    sqlClause = new StringBuilder("insert into ");
                    // insert into table_name (
                    sqlClause.append(tableNames[i - formatColumnIndex - 1]).append("(");
                    // insert into table_name (col1,col2,...,coli,
                    for (int j = 0; j < formatColumnIndex; j++) {
                        sqlClause.append(columnStrings[j]).append(",");
                    }
                    // insert into table_name (col1,col2,...,coli,coli+1,coli+2,...,coln,
                    sqlClause.append("REF_COUNT,ALT_COUNT)");
                    // insert into table_name (col1,col2,...,colK,colK+1,colK+2,...,colN) values('value1'
                    sqlClause.append("values('");
                    // CASE1: ch1,ch2,ch3,...chrY
                    if (sections[0].startsWith("ch") && !sections[0].startsWith("chr")) {
                        sqlClause.append(sections[0].replace("ch", "chr")).append("'");
                    }
                    // CASE2: 1,2,3,...,Y
                    else if (sections[0].length() < 3) {
                        sqlClause.append("chr").append(sections[0]).append("'");
                    }
                    // CASE3: chr1,chr2,chr3,...,chrY
                    else {
                        sqlClause.append(sections[0]).append("'");
                    }
                    // insert into table_name (col1,col2,...,colK,colK+1,colK+2,...,colN) values('value1','value2',...,
                    // 'valueK','valueK+1','valueK+2',...,'valueN')

                    for (int j = 1; j < formatColumnIndex; j++) {
                        sqlClause.append(",'").append(sections[j]).append("'");
                    }

                    sqlClause.append(",").append(refCount).append(",").append(altCount).append(")");

                    databaseManager.executeSQL(sqlClause.toString());

                    if (++lineCount % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0) {
                        databaseManager.commit();
                        if (listener != null) {
                            listener.progressUpdated("Importing " + lineCount + " lines from " + dataPath, 0, 0);
                        }
                    }
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (IOException e) {
            logger.error("Error open file: " + dataPath, e);
        } catch (SQLException e) {
            logger.error("Error execute sql clause: " + sqlClause, e);
        } catch (DataLoadException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    logger.error("Error close the buffered reader.", e);
                }
            }
        }
    }

    private int findIndexOfAd(String format) {
        String[] sections = format.split(":");
        for (int i = 0; i < sections.length; i++) {
            if (sections[i].equalsIgnoreCase("ad")) {
                return i;
            }
        }
        return -1;
    }

    private int findIndexOfDp4(String format) {
        String[] sections = format.split(":");
        for (int i = 0; i < sections.length; i++) {
            if (sections[i].equalsIgnoreCase("dp4")) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void recordInformation() {
        if (!EmptyChecker.isEmptyArray(tableNames)) {
            for (String tableName : tableNames) {
                databaseManager.insertOrUpdateInfo(tableName);
            }
        }
    }

    private boolean isDataValid(String[] tableNames) {
        boolean valid = true;
        for (String tableName : tableNames) {
            valid &= databaseManager.isTableExistAndValid(tableName);
        }
        return valid;
    }

    public String[] getSampleNames() {
        return sampleNames;
    }
}
