package com.dw.publicaffairs;

import com.dw.denovo.*;
import com.dw.dnarna.DnaRnaFilter;
import com.dw.dnarna.LLRFilter;

import java.sql.SQLException;

public class Connect {
    /**
     * @param args The following is for non-denovo detection.
     *             arg[0]: true if
     *             denovo arg[1]: RNA vcf file arg[2]: DNA vcf file arg[3]:
     *             Repeat file FROM
     *             http://www.repeatmasker.org/PreMaskedGenomes.html arg[4]: Ref
     *             Seq Genes file FROM http://genome.ucsc.edu/cgi-bin/hgTables
     *             arg[5]: dbSNP file FROM http://www.ncbi.nlm.nih.gov/SNP/
     *             arg[6]: DARNED database FROM
     *             http://darned.ucc.ie/static/downloads/hg19.txt arg[7]: The
     *             path of RScript arg[8]: Column index of vcf file to select
     *             normal or cancer people arg[9]: The database name.
     *             <p/>
     *             The following is for denovo detection
     *             arg[0]: true if denovo
     *             arg[1]: RNA vcf file
     *             arg[2]: Repeat file FROM http://www.repeatmasker.org/PreMaskedGenomes.html
     *             arg[3]: Ref Seq Genes FROM http://genome.ucsc.edu/cgi-bin/hgTables
     *             arg[4]: dbSNP file FROM http://www.ncbi.nlm.nih.gov/SNP/
     *             arg[5]: DARNED database FROM http://darned.ucc.ie/static/downloads/hg19.txt
     *             arg[6]: The path of RScript
     *             arg[7]: Column index of vcf file to select normal or cancer people
     *             arg[8]: The database name.
     */
    public static void main(String[] args) {

        // TODO Auto-generated method stub
        DatabaseManager manager = DatabaseManager.getInstance();
        try {
            manager.connectDatabase("localhost", "3306", "root", "sequencing");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        manager.setAutoCommit(true);

        // boolean isDenovo = false;
        // if (!isDenovo) {
        // manager.createDatabase("dnarna");
        // manager.useDatabase("dnarna");
        // Utilities.getInstance().createCalTable(
        // "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf");

        // "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf",
        // "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.DNA.chr8.snvs.vcf",
        // "loadRnaVcfTable", "loadDnaVcfTable");
        // df.establishDnaTable();
        // df.loadDnaVcfTable(10);
        // df.establishRnaTable();
        // df.loadRnaVcf(10);

        // BasicFilter bf = new BasicFilter(manager, "loadRnaVcfTable",
        // "specifictemp",
        // "basictemp");
        // bf.establishSpecificTable();
        // bf.executeSpecificFilter();
        // bf.establishQCTable();
        // // The first parameter means quality and the second means depth
        // bf.executeComprehensiveFilter(20, 6);
        // bf.distinctTable();

        // RepeatFilter rf = new RepeatFilter(manager,
        // "D:/TDDOWNLOAD/data/hg19.fa.out", "repeattemp",
        // "referencerepeat", "basictemp");
        // rf.loadRepeatTable();
        // rf.establishRepeatResultTable();
        // rf.executeRepeatFilter();
        // rf.rfilter();

        // ComphrehensiveFilter cf=new
        // ComphrehensiveFilter(manager,"D:/TDDOWNLOAD/data/genes.gtf","comphrehensivetemp","refcomphrehensive","repeattemp");
        // cf.establishSpliceJunctionResultTable();
        // cf.loadSpliceJunctionTable();
        // cf.executeComprehensiveFilter(2);
        //
        // dbSnpFilter sf=new
        // dbSnpFilter(manager,"D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf","snptemp","refsnp","comphrehensivetemp");
        // sf.establishDbSNPResultTable();
        // sf.dbSnpinput();
        // sf.executeDbSNPFilter();
        //
        // DnaRnaFilter dr=new DnaRnaFilter(manager, "loadDnaVcfTable", "DnaRnatemp",
        // "snptemp");
        // dr.esdr();
        // dr.executeDnaRnaFilter();
        // Vector<Probe> probes=bf.queryEditingSite();
        // for(Probe probe:probes){
        // System.out.println(probe.getChr()+" "+probe.getStart()+" "+probe.getAltBase());
        // }
        // llrFilter lf = new llrFilter(manager, "loadDnaVcfTable", "executeLLRFilter",
        // "DnaRnatemp");
        // lf.esllr();
        // lf.executeLLRFilter();
        //
        // pValueFilter pv=new
        // pValueFilter(manager,"D:/TDDOWNLOAD/data/hg19.txt","pvtemp","refHg19","executeLLRFilter");
        // pv.loadHg19();
        // pv.executeFDRFilter("D:/software/R-3.0.1/bin/Rscript.exe");
        //
        // }
        // else {
        // manager.createDatabase("denovo");
        // manager.useDatabase("denovo");
        //
        // denovoVcf df=new denovoVcf(manager,
        // "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf",
        // "loadRnaVcfTable");
        // df.loadRnaVcf(10);
        //
        // BasicFilter bf = new BasicFilter(manager, "loadRnaVcfTable",
        // "specifictemp",
        // "basictemp");
        // bf.establishSpecificTable();
        // bf.executeSpecificFilter();
        // // The first parameter means quality and the second means depth
        // bf.basicf(20, 6);
        //
        // RepeatFilter rf=new
        // RepeatFilter(manager,"D:/TDDOWNLOAD/data/hg19.fa.out","repeattemp","referencerepeat","basictemp");
        // rf.loadRepeatTable();
        // rf.establishRepeatResultTable();
        // rf.rfilter();
        //
        // ComphrehensiveFilter cf=new
        // ComphrehensiveFilter(manager,"D:/TDDOWNLOAD/data/genes.gtf","comphrehensivetemp","refcomphrehensive","repeattemp");
        // cf.establishSpliceJunctionResultTable();
        // cf.loadSpliceJunctionTable();
        // cf.executeComprehensiveFilter(2);
        //
        // dbSnpFilter sf=new
        // dbSnpFilter(manager,"D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf","snptemp","refsnp","comphrehensivetemp");
        // sf.establishDbSNPResultTable();
        // sf.dbSnpinput();
        // sf.executeDbSNPFilter();
        //
        // pValueFilter pv=new
        // pValueFilter(manager,"D:/TDDOWNLOAD/data/hg19.txt","pvtemp","refHg19","snptemp");
        // pv.loadHg19();
        // pv.executeFDRFilter("D:/software/R-3.0.1/bin/Rscript.exe");
        //
        // }

        /**
         * 0: false 1:
         * /Volumes/Macintosh_HD_4/BJ22_rnaEditing/RNA_Seq/BJ22.snvs_v2
         * .hard.filtered.vcf 2:
         * /Volumes/Macintosh_HD_4/BJ22_rnaEditing/WES/BJ22_sites
         * .hard.filtered.vcf 3: /Users/seq/SoftWare/MuTect/hg19.fa.out 4:
         * /Users
         * /seq/dataBase/iGenomes/Homo_sapiens/UCSC/hg19/Annotation/Genes/genes
         * .gtf 5: /Users/seq/SoftWare/MuTect/dbsnp_138.hg19.vcf 6:
         * /Users/seq/SoftWare/MuTect/hg19.txt 7: /usr/bin/Rscript 8: 10 9:
         * 1BJ22DnaRna
         */
        boolean isDenovo = Boolean.parseBoolean(args[0]);
        if (!isDenovo) {
            manager.createDatabase(args[9]);
            manager.useDatabase(args[9]);

            QCFilter bf = new QCFilter(manager);
//            bf.establishSpecificTable(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
//            bf.executeSpecificFilter(
//                    DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME,
//                    DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
            bf.establishQCTable(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
            // The first parameter means quality and the second means depth
            bf.executeQCFilter(
                    DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.QC_FILTER_RESULT_TABLE_NAME, 20, 6);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);

            RepeatFilter rf = new RepeatFilter(manager);
            rf.loadRepeatTable(DatabaseManager.REPEAT_FILTER_TABLE_NAME, args[3]);
            rf.establishRepeatResultTable(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);
            rf.establishAluResultTable(DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME);
            rf.executeRepeatFilter(DatabaseManager.REPEAT_FILTER_TABLE_NAME,
                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME, DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
//            rf.rfilter(DatabaseManager.REPEAT_FILTER_TABLE_NAME,
//                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME,
//                    DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);

            SpliceJunctionFilter cf = new SpliceJunctionFilter(manager);
            cf.establishSpliceJunctionResultTable(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
            cf.loadSpliceJunctionTable(
                    DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME, args[4]);
//            cf.executeComprehensiveFilter(
//                    DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME,
//                    DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME,
//                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME, 2);
            cf.executeSpliceJunctionFilter(
                    DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME,
                    DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME, 2);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);

            DBSNPFilter sf = new DBSNPFilter(manager);
            sf.establishDbSNPResultTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
            sf.loadDbSNPTable(DatabaseManager.DBSNP_FILTER_TABLE_NAME, args[5]);
            sf.executeDbSNPFilter(DatabaseManager.DBSNP_FILTER_TABLE_NAME,
                    DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);

            DnaRnaFilter dr = new DnaRnaFilter(manager);
            dr.establishDnaRnaTable(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);
            dr.executeDnaRnaFilter(
                    DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME, DatabaseManager.DNA_VCF_RESULT_TABLE_NAME,
                    DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);

            LLRFilter lf = new LLRFilter(manager);
            lf.establishLLRResultTable(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME);
            lf.executeLLRFilter(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.DNA_VCF_RESULT_TABLE_NAME,
                    DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME);

            PValueFilter pv = new PValueFilter(manager);
            pv.estblishPValueTable(DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME);
            pv.loadDarnedTable(DatabaseManager.PVALUE_FILTER_TABLE_NAME,
                    args[6]);
            pv.executeFDRFilter(DatabaseManager.PVALUE_FILTER_TABLE_NAME,
                    DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME, args[7]);

        } else {
            manager.createDatabase(args[8]);
            manager.useDatabase(args[8]);

//            DenovoVcf df = new DenovoVcf(manager);
//            df.establishRnaTable(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME, args[1]);
//            df.loadRnaVcfTable(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME,
//                    args[1], Integer.parseInt(args[7]));

            QCFilter bf = new QCFilter(manager);
//            bf.establishSpecificTable(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME);
//            bf.executeSpecificFilter(
//                    DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME,
//                    DatabaseManager.RNA_VCF_RESULT_TABLE_NAME);
            bf.establishQCTable(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
            // The first parameter means quality and the second means depth
            bf.executeQCFilter(
                    DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.QC_FILTER_RESULT_TABLE_NAME, 20, 6);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);

            RepeatFilter rf = new RepeatFilter(manager);
            rf.loadRepeatTable(DatabaseManager.REPEAT_FILTER_TABLE_NAME,
                    args[2]);
            rf.establishRepeatResultTable(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);
            rf.establishAluResultTable(DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME);
            rf.executeRepeatFilter(DatabaseManager.REPEAT_FILTER_TABLE_NAME,
                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME, DatabaseManager.ALU_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
//            rf.rfilter(DatabaseManager.REPEAT_FILTER_TABLE_NAME,
//                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME,
//                    DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME);

            SpliceJunctionFilter cf = new SpliceJunctionFilter(manager);
            cf.establishSpliceJunctionResultTable(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
            cf.loadSpliceJunctionTable(
                    DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME, args[3]);
//            cf.executeComprehensiveFilter(
//                    DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME,
//                    DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME,
//                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME, 2);
            cf.executeSpliceJunctionFilter(
                    DatabaseManager.SPLICE_JUNCTION_FILTER_TABLE_NAME,
                    DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME, 2);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);

            DBSNPFilter sf = new DBSNPFilter(manager);
            sf.establishDbSNPResultTable(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);
            sf.loadDbSNPTable(DatabaseManager.DBSNP_FILTER_TABLE_NAME, args[4]);
            sf.executeDbSNPFilter(DatabaseManager.DBSNP_FILTER_TABLE_NAME,
                    DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME);
            DatabaseManager.getInstance().distinctTable(
                    DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME);

            PValueFilter pv = new PValueFilter(manager);
            pv.estblishPValueTable(DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME);
            pv.loadDarnedTable(DatabaseManager.PVALUE_FILTER_TABLE_NAME,
                    args[5]);
            pv.executeFDRFilter(DatabaseManager.PVALUE_FILTER_TABLE_NAME,
                    DatabaseManager.PVALUE_FILTER_RESULT_TABLE_NAME,
                    DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME, args[6]);
        }
        manager.closeDatabase();
    }
}
