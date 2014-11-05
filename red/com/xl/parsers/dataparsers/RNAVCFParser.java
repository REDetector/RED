package com.xl.parsers.dataparsers;

import com.dw.dbutils.DatabaseManager;
import com.xl.dialog.ProgressDialog;
import com.xl.dialog.REDProgressBar;
import com.xl.exception.REDException;
import com.xl.preferences.REDPreferences;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2014/9/29.
 * <p/>
 * VCFParser mainly parsers VCF file and insert all data into database.
 * The class will delete old vcf table and create a new one.
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

    private REDProgressBar progressBar = REDProgressBar.getInstance();

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RNAVCFParser() {
        databaseManager = DatabaseManager.getInstance();
        progressBar.addProgressListener(new ProgressDialog("Import rna vcf data"));
    }

    public void parseVCFFile(String vcfTable, String vcfPath) {
        System.out.println("Start Parsing RNA VCF file..." + " " + df.format(new Date()));
        BufferedReader bufferedReader = null;
        try {
            int formatColumnIndex = 8;
            int dataColumnIndex = 9;
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(vcfPath)));
            String line;
            String[] columnStrings = null;
            StringBuilder tableBuilders = new StringBuilder();
            databaseManager.setAutoCommit(false);
            int lineCount = 0;
            boolean calGTIndex = false;
            int gtIndex = -1;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    columnStrings = line.substring(1).split("\\t");
                    if (columnStrings.length < formatColumnIndex) {
                        throw new REDException("The column information is not complete.");
                    }
//                    if (columnStrings.length > dataColumn) {
//                        throw new REDException("Multiple people in a vcf file has not been supported");
//                    }
                    tableBuilders.append(columnStrings[0]).append(" varchar(15),")
                            .append(columnStrings[1]).append(" int,")
                            .append(columnStrings[2]).append(" varchar(30),")
                            .append(columnStrings[3]).append(" varchar(5),")
                            .append(columnStrings[4]).append(" varchar(5),")
                            .append(columnStrings[5]).append(" float(8,2),")
                            .append(columnStrings[6]).append(" text,")
                            .append(columnStrings[7]).append(" text,");
                    continue;
                }
                String[] sections = line.split("\\t");

                if (sections[altColumn].equals(".")) {
                    continue;
                }

                String[] formatColumns = sections[formatColumnIndex].split(":");
                int formatLength = formatColumns.length;
                String[] dataColumns = sections[dataColumnIndex].replaceAll(",", "/").split(":");
                int dataColumnLength = dataColumns.length;
                if (formatLength != dataColumnLength) {
                    continue;
                }
                if (!calGTIndex) {
                    for (int i = 0; i < formatLength; i++) {
                        if (formatColumns[i].equals("GT")) {
                            gtIndex = i;
                            calGTIndex = true;
                        }
                    }
                    for (String formatColumn : formatColumns) {
                        tableBuilders.append(formatColumn).append(" text,");
                    }
                    tableBuilders.append("alu varchar(1) default 'F',");
                    tableBuilders.append("index(" + VCF_CHROM + "," + VCF_POS + ")");
                    databaseManager.deleteTable(vcfTable);
                    databaseManager.executeSQL("create table " + vcfTable + "(" + tableBuilders + ")");
                    databaseManager.commit();
                    REDPreferences.getInstance().setDatabaseTableBuilder(tableBuilders.toString());
                }
                // data for import '.' stands for undetected, so we discard it
                if (calGTIndex) {
                    if (dataColumns[gtIndex].startsWith(".") || dataColumns[gtIndex].endsWith(".")) {
                        continue;
                    }
                }
                if (columnStrings == null) {
                    throw new REDException("The columns information in vcf file is missing.");
                }

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
                if (lineCount % 1000 == 0) {
                    progressBar.progressUpdated("Importing " + lineCount + " lines from " + vcfPath + " to " + vcfTable, 0, 0);
                }
                if (++lineCount % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0) {
                    databaseManager.commit();
                }
            }
            databaseManager.commit();
            databaseManager.setAutoCommit(true);
        } catch (IOException e) {
            e.printStackTrace();
            progressBar.progressWarningReceived(e);
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + RNAVCFParser.class.getName() + ":run()");
            e.printStackTrace();
            progressBar.progressWarningReceived(e);
        } catch (REDException e) {
            e.printStackTrace();
            progressBar.progressWarningReceived(e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        progressBar.progressComplete("rna_vcf_loaded", null);
        System.out.println("End Parsing RNA VCF file..." + df.format(new Date()));
    }

}
