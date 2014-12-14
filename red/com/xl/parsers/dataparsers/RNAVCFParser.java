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

package com.xl.parsers.dataparsers;


import com.xl.database.DatabaseManager;
import com.xl.preferences.DatabasePreferences;
import com.xl.utils.Indexer;
import com.xl.utils.Timer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

/**
 * Created by Administrator on 2014/9/29.
 * <p/>
 * VCFParser mainly parsers VCF file and insert all data into database. The class will delete old vcf table and create a new one.
 */
public class RNAVCFParser {
    public static final String VCF_CHROM = "CHROM";
    public static final String VCF_POS = "POS";
//    public static final String VCF_ID = "ID";
//    public static final String VCF_REF = "REF";
//    public static final String VCF_ALT = "ALT";
//    public static final String VCF_QUAL = "QUAL";
//    public static final String VCF_FILTER = "FILTER";
//    public static final String VCF_INFO = "INFO";
//    public static final String VCF_FORMAT = "FORMAT";

    //    private int chromColumn = 0;
//    private int posColumn = 1;
//    private int idColumn = 2;
//    private int refColumn = 3;
    private int altColumn = 4;
//    private int qualColumn = 5;
//    private int filterColumn = 6;
//    private int infoColumn = 7;


    private DatabaseManager databaseManager;

    public RNAVCFParser() {
        databaseManager = DatabaseManager.getInstance();
    }

    public void parseVCFFile(String vcfTable, String vcfPath, int dataColumnIndex) {
        System.out.println("Start Parsing RNA VCF file..." + " " + Timer.getCurrentTime());
        BufferedReader bufferedReader = null;
        try {
            int formatColumnIndex = 8;
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(vcfPath)));
            String line;
            String[] columnStrings = null;
            StringBuilder tableBuilders = new StringBuilder();
            databaseManager.setAutoCommit(false);
            int lineCount = 0;
            boolean hasEstablishTable = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    columnStrings = line.substring(1).split("\\t");
//                    if (columnStrings.length > dataColumn) {
//                        throw new REDException("Multiple people in a vcf file has not been supported");
//                    }
                    tableBuilders.append(columnStrings[0]).append(" varchar(30),").append(columnStrings[1]).append(" int,")
                            .append(columnStrings[2]).append(" varchar(30),").append(columnStrings[3]).append(" varchar(5),")
                            .append(columnStrings[4]).append(" varchar(5),").append(columnStrings[5]).append(" float(10,2),")
                            .append(columnStrings[6]).append(" text,").append(columnStrings[7]).append(" text,");
                    continue;
                }
                String[] sections = line.split("\\t");

                if (sections[altColumn].equals(".") || sections[dataColumnIndex].contains(".")) {
                    continue;
                }

                String[] formatColumns = sections[formatColumnIndex].split(":");
                int formatLength = formatColumns.length;
                String[] dataColumns = sections[dataColumnIndex].replaceAll(",", "/").split(":");
                int dataColumnLength = dataColumns.length;
                if (formatLength != dataColumnLength) {
                    continue;
                }

                if (!hasEstablishTable) {
                    for (String formatColumn : formatColumns) {
                        tableBuilders.append(formatColumn).append(" text,");
                    }
                    // We need to add ALU info at the first table so the following filters can get the alu info.
                    tableBuilders.append("alu varchar(1) default 'F'");
                    DatabasePreferences.getInstance().setDatabaseTableBuilder(tableBuilders.toString());
                    tableBuilders.append(",");
                    tableBuilders.append(Indexer.CHROM_POSITION);
                    databaseManager.deleteTable(vcfTable);
                    databaseManager.executeSQL("create table " + vcfTable + "(" + tableBuilders + ")");
                    databaseManager.commit();

                    hasEstablishTable = true;
                }
                // data for import '.' stands for undetected, so we discard it
//                if (calGTIndex) {
//                    if (dataColumns[gtIndex].startsWith(".") || dataColumns[gtIndex].endsWith(".")) {
//                        continue;
//                    }
//                }

                //INSERT INTO table_name (col1, col2,...) VALUES (value1, value2,....)

                //insert into
                StringBuilder sqlClause = new StringBuilder("insert into ");
                // insert into table_name (
                sqlClause.append(vcfTable).append("(");
                // insert into table_name (col1,col2,...,coli,
                for (int i = 0; i < formatColumnIndex; i++) {
                    sqlClause.append(columnStrings[i]).append(",");
                }
                // insert into table_name (col1,col2,...,coli,coli+1,coli+2,...,coln,
                for (String formatColumn : formatColumns) {
                    sqlClause.append(formatColumn).append(",");
                }
                // insert into table_name (col1,col2,...,colK,colK+1,colK+2,...,colK)
                sqlClause.deleteCharAt(sqlClause.length() - 1).append(") ");
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
                // 'valueK'
                for (int i = 1; i < formatColumnIndex; i++) {
                    sqlClause.append(",'").append(sections[i]).append("'");
                }
                // insert into table_name (col1,col2,...,colK,colK+1,colK+2,...,colN) values('value1','value2',...,
                // 'valueK','valueK+1','valueK+2',...,'valueN'
                for (String dataColumn : dataColumns) {
                    sqlClause.append(",'").append(dataColumn).append("'");
                }
                // insert into table_name (col1,col2,...,colK,colK+1,colK+2,...,colN) values('value1','value2',...,
                // 'valueK','valueK+1','valueK+2',...,'valueN')
                sqlClause.append(")");
//                System.out.println(lineCount+"\t"+sqlClause.toString());
                databaseManager.executeSQL(sqlClause.toString());

                if (++lineCount % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0) {
                    databaseManager.commit();
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + RNAVCFParser.class.getName() + ":run()");
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("End Parsing RNA VCF file..." + Timer.getCurrentTime());
    }

}
