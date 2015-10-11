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

import com.xl.database.DatabaseManager;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Administrator on 2015/10/11.
 */
public class RedCommandRunner {
    private static Logger logger = LoggerFactory.getLogger(RedCommandRunner.class);
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
    public static String REFSEQ = "";
    public static String REPEAT = "";
    public static String DBSNP = "";
    public static String RSCRIPT = "/usr/bin/RScript";
    public static String ORDER = "12345678";
    public static String DELETE = "";

    public static void run(String[] args) {
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                System.out.println(getHelp());
                return;
            }
            if (arg.equals("-v") || arg.equals("--version")) {
                System.out.println(getVersion());
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
            } else if (key.equalsIgnoreCase("refseq")) {
                REFSEQ = value;
            } else if (key.equalsIgnoreCase("delete")) {
                DELETE = value;
            } else {
                logger.error("Unknown the argument '--" + key + "', please have a check.",
                    new IllegalArgumentException());
                return;
            }
        }

        if (INPUT.length() != 0) {
            String[] sections = INPUT.split(",");
            if (MODE.equalsIgnoreCase("dnarna") && sections.length >= 6) {
                RNAVCF = sections[0].trim();
                DNAVCF = sections[1].trim();
                DARNED = sections[2].trim();
                SPLICE = sections[3].trim();
                REPEAT = sections[4].trim();
                DBSNP = sections[5].trim();
            } else if (MODE.equalsIgnoreCase("denovo") && sections.length >= 5) {
                RNAVCF = sections[0].trim();
                DARNED = sections[1].trim();
                SPLICE = sections[2].trim();
                REPEAT = sections[3].trim();
                DBSNP = sections[4].trim();
            } else {
                logger.error("Unknown the argument '--INPUT " + INPUT + "' or it is incomplete, please have a check.",
                    new IllegalArgumentException());
                return;
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
                ParserFactory.createParser(REPEAT, DatabaseManager.SPLICE_JUNCTION_TABLE_NAME).loadDataFromLocal(null);
            }
            if (DBSNP.length() != 0) {
                ParserFactory.createParser(REPEAT, DatabaseManager.DBSNP_DATABASE_TABLE_NAME).loadDataFromLocal(null);
            }
            if (DARNED.length() != 0) {
                ParserFactory.createParser(REPEAT, DatabaseManager.DARNED_DATABASE_TABLE_NAME).loadDataFromLocal(null);
            }
            if (REFSEQ.length() != 0) {
                ParserFactory.createParser(REPEAT, DatabaseManager.REFSEQ_GENE_TABLE_NAME).loadDataFromLocal(null);
            }

            String endTime = Timer.getCurrentTime();
            logger.info("End importing data :\t" + endTime);
            logger.info("Data import lasts for :\t" + Timer.calculateInterval(startTime, endTime));

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
            filters.add(new SpliceJunctionFilter());
            filters.add(new RepeatRegionsFilter());
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
                        params.put(EditingTypeFilter.PARAMS_REF, "AG");
                    } else if (currentFilterName.equals(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME)) {
                        params.put(QualityControlFilter.PARAMS_STRING_QUALITY, 20 + "");
                        params.put(QualityControlFilter.PARAMS_INT_DEPTH, 6 + "");
                    } else if (currentFilterName.equals(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME)) {
                        params.put(DnaRnaFilter.PARAMS_STRING_DNA_VCF_TABLE, dnavcfTableName);
                        params.put(DnaRnaFilter.PARAMS_STRING_EDITING_TYPE, "AG");
                    } else if (currentFilterName.equals(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME)) {
                        params.put(SpliceJunctionFilter.PARAMS_INT_EDGE, 2 + "");
                    } else if (currentFilterName.equals(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME)) {
                    } else if (currentFilterName.equals(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME)) {
                    } else if (currentFilterName.equals(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME)) {
                        params.put(LikelihoodRatioFilter.PARAMS_STRING_DNA_VCF_TABLE, dnavcfTableName);
                        params.put(LikelihoodRatioFilter.PARAMS_DOUBLE_LLR_THRESHOLD, 4 + "");
                    } else if (currentFilterName.equals(DatabaseManager.FET_FILTER_RESULT_TABLE_NAME)) {
                        params.put(FisherExactTestFilter.PARAMS_STRING_EDITING_TYPE, "AG");
                        params.put(FisherExactTestFilter.PARAMS_STRING_R_SCRIPT_PATH, RSCRIPT);
                        params.put(FisherExactTestFilter.PARAMS_STRING_P_VALUE_THRESHOLD, 0.05 + "");
                        params.put(FisherExactTestFilter.PARAMS_STRING_FDR_THRESHOLD, 0.05 + "");
                    } else {
                        logger.error("Unknown current table : {}", currentFilterName);
                        throw new IllegalArgumentException();
                    }
                    logger.info("Current Running Filter: " + filters.get(i).getName());
                    filters.get(i).performFilter(previousTable, currentTable, params);
                    DatabaseManager.getInstance().distinctTable(currentTable);
                }
                endTime = Timer.getCurrentTime();
                logger.info("End performing filters :\t" + endTime);
                logger.info("Filter performance lasts for :\t" + Timer.calculateInterval(startTime, endTime));
            }
        } catch (DataLoadException e) {
            logger.error("Data can't be loaded correctly, please have a check and try again.", e);
        }
    }

    public static ArrayList<Filter> sortFilters(List<Filter> filters, int[] orders) {
        if (filters.size() != orders.length) {
            logger.error("The number of the filters did not fit for the number of the order",
                new IllegalArgumentException());
            return null;
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

    public static String getVersion() {
        return "Red Command Line Tool version 0.0.1 (2015-10-10)\n" + "\n" + "Copyright (C) <2014-2015>  <Xing Li>\n"
            + "\n"
            + "RCTL is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free "
            + "Software Foundation, either version 3 of the License, or (at your option) any later version.\n" + "\n"
            + "RCTL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.\n"
            + "\n"
            + "You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.";
    }

    public static String getHelp() {
        return "Usage: java -jar jarfile [-h|--help] [-v|--version] [-H|--host[=127.0.0.1]] [-p|--port[=3306]] [-u|--user[=root]] [-P|--pwd[=root]] [-m|--mode[=dnarna]] [-i|--input] [-o|--output[=./]] [-e|--export[=all]] [--rnavcf] [--dnavcf] [--darned] [--splice] [--repeat] [--dbsnp]\n"
            + "\n" + "The most commonly used REFilters commands are:\n"
            + "\t-h, --help   \t\t\tPrint short help message and exit;\n"
            + "\t-v, --version \t\t\tPrint version info and exit;\n"
            + "\t-H, --host=127.0.0.1    The host address of MySQL database;\n"
            + "\t-p, --port=3306    \t\tThe port used in MySQL;\n" + "\t-u, --user=root    \t\tMySQL user name;\n"
            + "\t-P, --pwd=root     \t\tMySQL password of user;\n"
            + "\t-m, --mode=dnarna  \t\tTell the program if it is denovo mode or DNARNA mode;\n"
            + "\t-i, --input  \t\t\tInput all required files in order (i.e., RNA VCF File, DNA VCF File, DARNED Database, Gene Annotation File, RepeatMasker Database File, dbSNP Database File) instead of single input, each file should be divided with ',' and there should not be blank with each file;\n"
            + "\t-o, --output=./    \t\tSet export path for the results in database, default path is current directory;\n"
            + "\t-e, --export=all  \t\tExport the needed columns in database, which must be the column name of a table in database, the column names should be divided by ',';\n"
            + "\t--rnavcf  \t\t\t\tFile path of RNA VCF file;\n" + "\t--dnavcf  \t\t\t\tFile path of DNA VCF file;\n"
            + "\t--darned  \t\t\t\tFile path of DARNED database;\n"
            + "\t--splice  \t\t\t\tFile path of annotation genes like \"gene.gft\";\n"
            + "\t--repeat  \t\t\t\tFile path of Repeat Masker database;\n"
            + "\t--dbsnp   \t\t\t\tFile path of dbSNP database;\n" + "\t-r, --rscript \t\t\tFile path of RScript.\n"
            + "\n" + "Example:\n" + "1) In Windows, use '--' patterns.\n"
            + "java -jar E:\\Workspace\\REFilters\\out\\artifacts\\REFilters\\REFilters.jar ^\n"
            + "--host=127.0.0.1 ^\n" + "--port=3306 ^\n" + "--user=root ^\n" + "--pwd=123456 ^\n"
            + "--mode=denovo --input=D:\\Downloads\\Documents\\BJ22.snvs.hard.filtered.vcf,D:\\Downloads\\Documents\\hg19.txt,D:\\Downloads\\Documents\\genes.gtf,D:\\Downloads\\Documents\\hg19.fa.out,D:\\Downloads\\Documents\\dbsnp_138.hg19.vcf ^\n"
            + "--output=E:\\Workspace\\REFilters\\Results ^\n" + "--export=all ^\n"
            + "--rscript=C:\\R\\R-3.1.1\\bin\\Rscript.exe\n" + "\n" + "2) In Windows, use '-' patterns.\n"
            + "java -jar E:\\Workspace\\REFilters\\out\\artifacts\\REFilters\\REFilters.jar ^\n" + "-H 127.0.0.1 ^\n"
            + "-p 3306 ^\n" + "-u root ^\n" + "-P 123456 ^\n" + "-m dnarna ^\n"
            + "-i D:\\Downloads\\Documents\\BJ22.snvs.hard.filtered.vcf,D:\\Downloads\\Documents\\BJ22_sites.hard.filtered.vcf,D:\\Downloads\\Documents\\hg19.txt,D:\\Downloads\\Documents\\genes.gtf,D:\\Downloads\\Documents\\hg19.fa.out,D:\\Downloads\\Documents\\dbsnp_138.hg19.vcf ^\n"
            + "-o E:\\Workspace\\REFilters\\Results ^\n" + "-e chrom,pos,level ^\n"
            + "-r C:\\R\\R-3.1.1\\bin\\Rscript.exe\n" + "\n" + "3) In CentOS, use '-' and '--' patterns.\n"
            + "java -jar /home/seq/softWare/RED/REFilter.jar \n" + "-h 127.0.0.1 \\\n" + "-p 3306 \\\n" + "-u seq \\\n"
            + "-P 123456 \\\n" + "-m denovo \\\n"
            + "--rnavcf=/data/rnaEditing/GM12878/GM12878.snvs.hard.filtered.vcf \\\n"
            + "--repeat=/home/seq/softWare/RED/hg19.fa.out \\\n" + "--splice=/home/seq/softWare/RED/genes.gtf \\\n"
            + "--dbsnp=/home/seq/softWare/RED/dbsnp_138.hg19.vcf \\\n" + "--darned=/home/seq/softWare/RED/hg19.txt \\\n"
            + "--rscript=/usr/bin/Rscript";
    }
}
