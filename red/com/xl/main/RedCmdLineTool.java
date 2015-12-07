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

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xl.database.DatabaseManager;
import com.xl.database.TableCreator;
import com.xl.exception.DataLoadException;
import com.xl.filter.Filter;
import com.xl.filter.denovo.*;
import com.xl.filter.dnarna.DnaRnaFilter;
import com.xl.filter.dnarna.LikelihoodRatioFilter;
import com.xl.parsers.referenceparsers.AbstractParser;
import com.xl.parsers.referenceparsers.DnaVcfParser;
import com.xl.parsers.referenceparsers.ParserFactory;
import com.xl.parsers.referenceparsers.RnaVcfParser;
import com.xl.utils.FileUtils;
import com.xl.utils.Timer;

/**
 * Created by Administrator on 2015/10/11.
 */
public class RedCmdLineTool {
    private static Logger logger = LoggerFactory.getLogger(RedCmdLineTool.class);
    public static String HOST = "127.0.0.1";
    public static String PORT = "3306";
    public static String USER = "root";
    public static String PWD = "root";
    public static String DATABASE = DatabaseManager.DNA_RNA_MODE_DATABASE_NAME;
    public static String MODE = "dnarna";
    public static String INPUT = "";
    public static String OUTPUT = "";
    public static String RNAVCF = "";
    public static String DNAVCF = "";
    public static String DARNED = "";
    public static String RADAR = "";
    public static String SPLICE = "";
    public static String REPEAT = "";
    public static String DBSNP = "";
    public static String RSCRIPT = "/usr/bin/RScript";
    public static String TYPE = "AG";
    public static String ORDER = "12345678";
    public static String EXPORT = "";
    public static String DELETE = "";

    public static void run(String[] args) {
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                printDoc();
                return;
            }
            if (arg.equals("-v") || arg.equals("--version")) {
                printVersion();
                return;
            }
        }
        Map<Character, String> singleMap = new HashMap<Character, String>();
        Map<String, String> doubleMap = new HashMap<String, String>();
        for (int i = 0, len = args.length; i < len; i++) {
            if (args[i].startsWith("--")) {
                String[] sections = args[i].substring(2).split("=");
                doubleMap.put(sections[0], sections[1]);
            } else if (args[i].startsWith("-") && !args[i].startsWith("--")) {
                char c = args[i].charAt(1);
                singleMap.put(c, args[i + 1]);
                i++;
            } else {
                logger.error("Wrong input parameters, please have a check.", new IllegalArgumentException());
                return;
            }
        }

        for (Map.Entry entry : singleMap.entrySet()) {
            char key = (Character) entry.getKey();
            String value = entry.getValue().toString();
            switch (key) {
                case 'H':
                    HOST = value;
                    break;
                case 'p':
                    PORT = value;
                    break;
                case 'u':
                    USER = value;
                    break;
                case 'P':
                    PWD = value;
                    break;
                case 'd':
                    DATABASE = value;
                    break;
                case 'm':
                    MODE = value;
                    break;
                case 'i':
                    INPUT = value;
                    break;
                case 'o':
                    OUTPUT = value;
                    break;
                case 'r':
                    RSCRIPT = value;
                    break;
                case 'O':
                    ORDER = value;
                    break;
                case 'D':
                    DELETE = value;
                    break;
                case 't':
                    TYPE = value;
                    break;
                case 'E':
                    EXPORT = value;
                    break;
                default:
                    logger.error("Unknown the argument '-" + key + "', please have a check.",
                        new IllegalArgumentException());
                    return;
            }
        }

        for (Map.Entry entry : doubleMap.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            if (key.equalsIgnoreCase("host")) {
                HOST = value;
            } else if (key.equalsIgnoreCase("port")) {
                PORT = value;
            } else if (key.equalsIgnoreCase("user")) {
                USER = value;
            } else if (key.equalsIgnoreCase("pwd")) {
                PWD = value;
            } else if (key.equalsIgnoreCase("mode")) {
                MODE = value;
            } else if (key.equalsIgnoreCase("database")) {
                DATABASE = value;
            } else if (key.equalsIgnoreCase("input")) {
                INPUT = value;
            } else if (key.equalsIgnoreCase("output")) {
                OUTPUT = value;
            } else if (key.equalsIgnoreCase("r") || key.equalsIgnoreCase("rscript")) {
                RSCRIPT = value;
            } else if (key.equalsIgnoreCase("rnavcf")) {
                RNAVCF = value;
            } else if (key.equalsIgnoreCase("dnavcf")) {
                DNAVCF = value;
            } else if (key.equalsIgnoreCase("darned")) {
                DARNED = value;
            } else if (key.equalsIgnoreCase("radar")) {
                RADAR = value;
            } else if (key.equalsIgnoreCase("splice")) {
                SPLICE = value;
            } else if (key.equalsIgnoreCase("repeat")) {
                REPEAT = value;
            } else if (key.equalsIgnoreCase("dbsnp")) {
                DBSNP = value;
            } else if (key.equalsIgnoreCase("order")) {
                ORDER = value;
            } else if (key.equalsIgnoreCase("delete")) {
                DELETE = value;
            } else if (key.equalsIgnoreCase("type")) {
                TYPE = value;
            } else if (key.equalsIgnoreCase("export")) {
                EXPORT = value;
            } else {
                logger.error("Unknown the argument '--" + key + "', please have a check.",
                    new IllegalArgumentException());
                return;
            }
        }

        if (INPUT.length() != 0) {
            String[] sections = INPUT.split(",");
            for (String section : sections) {
                String[] keyValues = section.split("-");
                if (keyValues.length != 2) {
                    logger.error(
                        "Unknown the argument '-i or --input " + INPUT + "' or it is incomplete, please have a check.",
                        new IllegalArgumentException());
                    return;
                }
                String key = keyValues[0].trim();
                String value = keyValues[1].trim();
                if (key.equalsIgnoreCase("rnavcf")) {
                    RNAVCF = value;
                } else if (key.equalsIgnoreCase("dnavcf")) {
                    DNAVCF = value;
                } else if (key.equalsIgnoreCase("darned")) {
                    DARNED = value;
                } else if (key.equalsIgnoreCase("radar")) {
                    RADAR = value;
                } else if (key.equalsIgnoreCase("splice")) {
                    SPLICE = value;
                } else if (key.equalsIgnoreCase("repeat")) {
                    REPEAT = value;
                } else if (key.equalsIgnoreCase("dbsnp")) {
                    DBSNP = value;
                } else {
                    logger.error("Unknown key '" + key + "' or value '" + value + "', please have a check.",
                        new IllegalArgumentException());
                    return;
                }
            }

        }

        logger.info("Start connecting the database...");
        DatabaseManager manager = DatabaseManager.getInstance();
        try {
            if (!manager.connectDatabase(HOST, PORT, USER, PWD)) {
                logger.error(
                    "Sorry, fail to connect to the database. You may input one of the wrong database host, port, user name or password.",
                    new SQLException());
                return;
            }
        } catch (SQLException e) {
            logger.error(
                "Sorry, fail to connect to the database. You may input one of the wrong database host, port, user name or password.",
                e);
            return;
        }
        logger.info("Connect database successfully.");
        manager.setAutoCommit(true);

        logger.info("Set up the output directory at:" + OUTPUT);
        File root = new File(OUTPUT);
        String rootPath = root.getAbsolutePath();
        if (!FileUtils.createDirectory(rootPath)) {
            logger.error("Root path '{}' can not be created. Make sure you have the permission.", rootPath);
            return;
        }

        boolean denovo;
        // Data import for five or six files, which is depended on denovo mode or DNA-RNA mode.
        if (DATABASE.length() != 0) {
            denovo = MODE.equalsIgnoreCase("denovo");
        } else if (MODE.equalsIgnoreCase("dnarna")) {
            DATABASE = DatabaseManager.DNA_RNA_MODE_DATABASE_NAME;
            denovo = false;
        } else if (MODE.equalsIgnoreCase("denovo")) {
            DATABASE = DatabaseManager.DENOVO_MODE_DATABASE_NAME;
            denovo = true;
        } else {
            logger.error("Unknown the mode '{}', please have a check.", MODE);
            return;
        }

        manager.createDatabase(DATABASE);
        manager.useDatabase(DATABASE);

        if (DELETE.length() != 0 && !DELETE.equalsIgnoreCase("all")) {
            String[] sections = DELETE.split(",");
            for (String section : sections) {
                logger.info("Deleting sample '" + section + "' in database '" + DATABASE + "'.");
                manager.deleteTableAndFilters(DATABASE, section);
            }
            logger.info("Delete complete!");
            return;
        }

        String resultPath = rootPath + File.separator + MODE;
        if (!FileUtils.createDirectory(resultPath)) {
            logger.error("Result path '{}' can not be created. Make sure you have the file permission.", resultPath);
            return;
        }

        try {

            // Next, print the start time of the data import.
            String startTime = Timer.getCurrentTime();
            logger.info("Start importing data:\t" + startTime);
            String[] rnaVCFSampleNames;
            if (RNAVCF.length() != 0) {
                AbstractParser rnaVcfParser =
                    ParserFactory.createParser(RNAVCF, DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
                rnaVcfParser.loadDataFromLocal(null);
                rnaVCFSampleNames = ((RnaVcfParser) rnaVcfParser).getSampleNames();
            } else {
                logger.error("RNA VCF file is empty, please have a check.");
                throw new NullPointerException();
            }
            String[] dnaVCFSampleNames;
            if (!denovo) {
                if (DNAVCF.length() != 0) {
                    AbstractParser dnaVcfParser =
                        ParserFactory.createParser(DNAVCF, DatabaseManager.DNA_VCF_RESULT_TABLE_NAME);
                    dnaVcfParser.loadDataFromLocal(null);
                    dnaVCFSampleNames = ((DnaVcfParser) dnaVcfParser).getSampleNames();
                } else {
                    logger.error("DNA VCF file is empty, please have a check.");
                    throw new NullPointerException();
                }

                boolean match = false;
                for (String rnaSample : rnaVCFSampleNames) {
                    match = false;
                    for (String dnaSample : dnaVCFSampleNames) {
                        if (rnaSample.equalsIgnoreCase(dnaSample)) {
                            match = true;
                        }
                    }
                }
                if (!match) {
                    logger.error(
                        "Samples in DNA VCF file does not match the RNA VCF, please have a check the sample name.");
                    throw new IllegalArgumentException();
                }
            }
            if (DELETE.length() != 0 && DELETE.equalsIgnoreCase("all")) {
                for (String rnaVCFSampleName : rnaVCFSampleNames) {
                    logger.info("Deleting sample '" + rnaVCFSampleName + "' in database '" + DATABASE + "'.");
                    manager.deleteTableAndFilters(DATABASE, rnaVCFSampleName);
                }
                logger.info("Delete all samples complete!");
                return;
            }

            if (REPEAT.length() != 0) {
                ParserFactory.createParser(REPEAT, DatabaseManager.REPEAT_MASKER_TABLE_NAME).loadDataFromLocal(null);
            }
            if (SPLICE.length() != 0) {
                ParserFactory.createParser(SPLICE, DatabaseManager.SPLICE_JUNCTION_TABLE_NAME).loadDataFromLocal(null);
            }
            if (DBSNP.length() != 0) {
                ParserFactory.createParser(DBSNP, DatabaseManager.DBSNP_DATABASE_TABLE_NAME).loadDataFromLocal(null);
            }
            if (DARNED.length() != 0) {
                ParserFactory.createParser(DARNED, DatabaseManager.DARNED_DATABASE_TABLE_NAME).loadDataFromLocal(null);
            }
            if (RADAR.length() != 0) {
                ParserFactory.createParser(RADAR, DatabaseManager.RADAR_DATABASE_TABLE_NAME).loadDataFromLocal(null);
            }

            String endTime = Timer.getCurrentTime();
            logger.info("End importing data :\t" + endTime);
            logger.info("Data import lasts for :\t" + Timer.calculateInterval(startTime, endTime));

            if (denovo && ORDER.equals("12345678")) {
                ORDER = "123456";
            }
            char[] charOrders = ORDER.toCharArray();
            int[] intOrders = new int[charOrders.length];
            for (int i = 0, len = charOrders.length; i < len; i++) {
                intOrders[i] = charOrders[i] - '0';
            }
            List<Filter> filters = new LinkedList<Filter>();
            filters.add(new EditingTypeFilter());
            filters.add(new QualityControlFilter());
            if (!denovo) {
                filters.add(new DnaRnaFilter());
            }
            filters.add(new SpliceJunctionFilter2());
            filters.add(new RepeatRegionsFilter2());
            filters.add(new KnownSnpFilter());
            if (!denovo) {
                filters.add(new LikelihoodRatioFilter());
            }
            filters.add(new FisherExactTestFilter());

            filters = sortFilters(filters, intOrders);

            for (String sample : rnaVCFSampleNames) {
                // First, print base information of all data.

                logger.info("------------------------ Sample name : " + sample + " ------------------------");
                if (!denovo) {
                    logger.info("Mode :\tDNA-RNA Mode");
                    logger.info("DNA VCF File :\t" + DNAVCF);
                } else {
                    logger.info("Mode :\tde novo Mode");
                }
                logger.info("RNA VCF File :\t" + RNAVCF);
                logger.info("DARNED File :\t" + DARNED);
                logger.info("Splice Junction File :\t" + SPLICE);
                logger.info("Repeat Masker File :\t" + REPEAT);
                logger.info("dbSNP File :\t" + DBSNP);
                logger.info("RScript Path :\t" + RSCRIPT);

                startTime = Timer.getCurrentTime();
                logger.info("Start performing filters at :\t" + startTime);

                String rawFilterName = sample + "_" + DatabaseManager.RNA_VCF_RESULT_TABLE_NAME;
                String dnavcfTableName = sample + "_" + DatabaseManager.DNA_VCF_RESULT_TABLE_NAME;

                for (int i = 0, len = filters.size(); i < len; i++) {
                    String previousFilterName;
                    String currentFilterName = filters.get(i).getName();
                    String previousTable;
                    String currentTable;
                    if (i == 0) {
                        previousFilterName = rawFilterName;
                        previousTable = rawFilterName;
                        currentTable = previousFilterName + "_" + currentFilterName;
                    } else {
                        previousFilterName = filters.get(i - 1).getName();
                        if (i == 1) {
                            previousTable = rawFilterName + "_" + previousFilterName;
                        } else {
                            previousTable = sample + "_" + filters.get(i - 2).getName() + "_" + previousFilterName;
                        }
                        currentTable = sample + "_" + previousFilterName + "_" + currentFilterName;
                    }
                    Map<String, String> params = new HashMap<String, String>();
                    if (currentFilterName.equals(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME)) {
                        params.put(EditingTypeFilter.PARAMS_REF, TYPE);
                    } else if (currentFilterName.equals(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME)) {
                        params.put(QualityControlFilter.PARAMS_STRING_QUALITY, 20 + "");
                        params.put(QualityControlFilter.PARAMS_INT_DEPTH, 6 + "");
                    } else if (currentFilterName.equals(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME)) {
                        params.put(DnaRnaFilter.PARAMS_STRING_DNA_VCF_TABLE, dnavcfTableName);
                        params.put(DnaRnaFilter.PARAMS_STRING_EDITING_TYPE, TYPE);
                    } else if (currentFilterName.equals(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME)) {
                        params.put(SpliceJunctionFilter.PARAMS_INT_EDGE, 2 + "");
                    } else if (currentFilterName.equals(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME)) {
                    } else if (currentFilterName.equals(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME)) {
                    } else if (currentFilterName.equals(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME)) {
                        params.put(LikelihoodRatioFilter.PARAMS_STRING_DNA_VCF_TABLE, dnavcfTableName);
                        params.put(LikelihoodRatioFilter.PARAMS_DOUBLE_LLR_THRESHOLD, 4 + "");
                    } else if (currentFilterName.equals(DatabaseManager.FET_FILTER_RESULT_TABLE_NAME)) {
                        params.put(FisherExactTestFilter.PARAMS_STRING_EDITING_TYPE, TYPE);
                        params.put(FisherExactTestFilter.PARAMS_STRING_R_SCRIPT_PATH, RSCRIPT);
                        params.put(FisherExactTestFilter.PARAMS_STRING_P_VALUE_THRESHOLD, 0.05 + "");
                        params.put(FisherExactTestFilter.PARAMS_STRING_FDR_THRESHOLD, 0.05 + "");
                    } else {
                        logger.error("Unknown current table : {}", currentFilterName);
                        throw new IllegalArgumentException();
                    }
                    logger.info("Current Running Filter: " + filters.get(i).getName());
                    if (manager.existTable(currentTable)) {
                        logger.info("Table has been existed!");
                        printDupeInfo(currentTable);
                        Scanner scanner = new Scanner(System.in);
                        String answer = "yes";
                        if (scanner.hasNext()) {
                            answer = scanner.nextLine();
                        }
                        if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
                            manager.deleteTable(currentTable);
                            createFilter(currentFilterName, previousTable, currentTable);
                            filters.get(i).performFilter(previousTable, currentTable, params);
                            DatabaseManager.getInstance().distinctTable(currentTable);
                        } else if (answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) {
                            logger.info("Use old data for next filter.");
                        } else {
                            logger.info("Cancel the filtering process.");
                            return;
                        }
                    } else {
                        manager.deleteTable(currentTable);
                        createFilter(currentFilterName, previousTable, currentTable);
                        filters.get(i).performFilter(previousTable, currentTable, params);
                        DatabaseManager.getInstance().distinctTable(currentTable);
                    }

                }
                endTime = Timer.getCurrentTime();
                logger.info("End performing filters :\t" + endTime);
                logger.info("Filter performance lasts for :\t" + Timer.calculateInterval(startTime, endTime));

                if (EXPORT != null && EXPORT.length() != 0) {
                    DataExporter exporter = new DataExporter();
                    exporter.exportData(resultPath, DATABASE, MODE, EXPORT.split(","), null, null);
                }
            }
        } catch (DataLoadException e) {
            logger.error("Data can't be loaded correctly, please have a check and try again.", e);
        }

    }

    public static ArrayList<Filter> sortFilters(List<Filter> filters, int[] orders) {
        if (filters.size() != orders.length) {
            logger.error("The number of the filters did not fit for the number of the order",
                new IllegalArgumentException());
            return new ArrayList<Filter>();
        }
        logger.info("Sort the filter by the order list.");
        Map<Integer, Filter> map = new TreeMap<Integer, Filter>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        for (int i = 0, len = orders.length; i < len; i++) {
            if (orders[i] != 0) {
                map.put(orders[i], filters.get(i));
            }
        }
        return new ArrayList<Filter>(map.values());
    }

    public static void createFilter(String currentFilterName, String previousTable, String currentTable) {
        TableCreator.createFilterTable(previousTable, currentTable);
    }

    public static void printVersion() {
        printResources("CommandLineToolVersion.txt");
    }

    public static void printDoc() {
        printResources("CommandLineToolDoc.txt");
    }

    public static void printDupeInfo(String tableName) {
        System.out.println(
            "Table '" + tableName + "' has been existed in the database. Would you like to override it? (yes/No)");
    }

    public static void printResources(String resource) {
        InputStream is = RedCmdLineTool.class.getResourceAsStream(resource);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            logger.error("Error when getting the document resource: " + resource, e);
        }
    }
}
