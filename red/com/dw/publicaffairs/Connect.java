package com.dw.publicaffairs;

import com.dw.denovo.BasicFilter;
import com.dw.denovo.RepeatFilter;

import java.sql.SQLException;

public class Connect {
    /**
     * @param args
     */
    public static void main(String[] args) {
        // final String PARAM_DNA_VCF = "-d";
        // int argsLength = args.length;
        // if(argsLength%2!=0){
        // System.out.println("Error Input");
        // }
        // Map<String,String> paramPath = new HashMap<String,String>();
        // for(int i=0;i<args.length;i+=2){
        // if(args[i].startsWith("-")){
        // System.out.println("Error Parameter Input");
        // }
        // paramPath.put(args[i], args[i+1]);
        // }
        // String dnavcfPath = paramPath.get(PARAM_DNA_VCF);

        // TODO Auto-generated method stub
        DatabaseManager manager = DatabaseManager.getInstance();
        try {
            manager.connectDatabase("localhost", "3306", "root", "root");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        manager.createStatement();
        manager.setAutoCommit(true);

        boolean isDenovo = false;
        if (!isDenovo) {
            manager.createDatabase("dnarna");
            manager.useDatabase("dnarna");
            Utilities.getInstance().createCalTable(
                    "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf");

            //
            // dnarnaVcf df = new dnarnaVcf(manager,
            // "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf",
            // "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.DNA.chr8.snvs.vcf",
            // "rnaVcf", "dnaVcf");
            // df.dnaVcf(10);
            // df.rnaVcf(10);
            //
            BasicFilter bf = new BasicFilter(manager, "rnaVcf",
                    "specifictemp",
                    "basictemp");
            bf.createSpecificTable();
            bf.specificf();
            // The first parameter means quality and the second means depth
            bf.basicf(20, 6);
            //
            RepeatFilter rf = new RepeatFilter(manager,
                    "D:/TDDOWNLOAD/data/hg19.fa.out", "repeattemp",
                    "referencerepeat", "basictemp");
            // rf.loadrepeat();
            rf.establishrepeat();
//			 rf.repeatFilter();
            rf.rfilter();

            // ComphrehensiveFilter cf=new
            // ComphrehensiveFilter(manager,"D:/TDDOWNLOAD/data/genes.gtf","comphrehensivetemp","refcomphrehensive","repeattemp");
            // cf.establishCom();
            // cf.loadcom();
            // cf.comphrehensiveF(2);
            //
            // dbSnpFilter sf=new
            // dbSnpFilter(manager,"D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf","snptemp","refsnp","comphrehensivetemp");
            // sf.establishsnp();
            // sf.dbSnpinput();
            // sf.snpFilter();
            //
            // DnaRnaFilter dr=new DnaRnaFilter(manager, "dnaVcf", "DnaRnatemp",
            // "snptemp");
            // dr.esdr();
            // dr.dnarnaFilter();
            //
            // llrFilter lf = new llrFilter(manager, "dnaVcf", "llrtemp",
            // "DnaRnatemp");
            // lf.esllr();
            // lf.llrtemp();
            //
            // pValueFilter pv=new
            // pValueFilter(manager,"D:/TDDOWNLOAD/data/hg19.txt","pvtemp","refHg19","llrtemp");
            // pv.loadHg19();
            // pv.fdr("D:/software/R-3.0.1/bin/Rscript.exe");
            //
        }
        // else {
        // manager.createDatabase("denovo");
        // manager.useDatabase("denovo");
        //
        // denovoVcf df=new denovoVcf(manager,
        // "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf", "rnaVcf");
        // df.rnaVcf(10);
        //
        // BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
        // "basictemp");
        // bf.createSpecificTable();
        // bf.specificf();
        // // The first parameter means quality and the second means depth
        // bf.basicf(20, 6);
        //
        // RepeatFilter rf=new
        // RepeatFilter(manager,"D:/TDDOWNLOAD/data/hg19.fa.out","repeattemp","referencerepeat","basictemp");
        // rf.loadrepeat();
        // rf.establishrepeat();
        // rf.rfilter();
        //
        // ComphrehensiveFilter cf=new
        // ComphrehensiveFilter(manager,"D:/TDDOWNLOAD/data/genes.gtf","comphrehensivetemp","refcomphrehensive","repeattemp");
        // cf.establishCom();
        // cf.loadcom();
        // cf.comphrehensiveF(2);
        //
        // dbSnpFilter sf=new
        // dbSnpFilter(manager,"D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf","snptemp","refsnp","comphrehensivetemp");
        // sf.establishsnp();
        // sf.dbSnpinput();
        // sf.snpFilter();
        //
        // pValueFilter pv=new
        // pValueFilter(manager,"D:/TDDOWNLOAD/data/hg19.txt","pvtemp","refHg19","snptemp");
        // pv.loadHg19();
        // pv.fdr("D:/software/R-3.0.1/bin/Rscript.exe");
        //
        // }

        // boolean isDenovo = Boolean.parseBoolean(args[0]);
        // if (!isDenovo) {
        // manager.createDatabase("dnarna");
        // manager.useDatabase("dnarna");
        //
        // dnarnaVcf df = new dnarnaVcf(manager,args[1],args[2],"rnaVcf",
        // "dnaVcf");
        // Utilities.getInstance().createCalTable(args[2]);
        // df.dnaVcf(Integer.parseInt(args[8]));
        //
        // Clear cl=new Clear();
        // cl.clear(Utilities.getInstance().getS2(),
        // Utilities.getInstance().getS3());
        //
        // Utilities.getInstance().createCalTable(args[1]);
        // df.rnaVcf(Integer.parseInt(args[8]));
        //
        // BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
        // "basictemp");
        // bf.createSpecificTable();
        // bf.specificf();
        // // The first parameter means quality and the second means depth
        // bf.basicf(20, 6);
        //
        // RepeatFilter rf=new
        // RepeatFilter(manager,args[3],"repeattemp","referencerepeat","basictemp");
        // rf.loadrepeat();
        // rf.establishrepeat();
        // rf.rfilter();
        //
        // ComphrehensiveFilter cf=new
        // ComphrehensiveFilter(manager,args[4],"comphrehensivetemp","refcomphrehensive","repeattemp");
        // cf.establishCom();
        // cf.loadcom();
        // cf.comphrehensiveF(2);
        //
        // dbSnpFilter sf=new
        // dbSnpFilter(manager,args[5],"snptemp","refsnp","comphrehensivetemp");
        // sf.establishsnp();
        // sf.dbSnpinput();
        // sf.snpFilter();
        //
        // DnaRnaFilter dr=new DnaRnaFilter(manager, "dnaVcf", "DnaRnatemp",
        // "snptemp");
        // dr.esdr();
        // dr.dnarnaFilter();
        //
        // llrFilter lf = new llrFilter(manager, "dnaVcf", "llrtemp",
        // "DnaRnatemp");
        // lf.esllr();
        // lf.llrtemp();
        //
        // pValueFilter pv=new
        // pValueFilter(manager,args[6],"pvtemp","refHg19","llrtemp");
        // pv.loadHg19();
        // pv.fdr(args[7]);
        //
        // }
        // else {
        // manager.createDatabase("denovo");
        // manager.useDatabase("denovo");
        // Utilities.getInstance().createCalTable(args[1]);
        //
        // denovoVcf df=new denovoVcf(manager, args[1], "rnaVcf");
        // df.rnaVcf(Integer.parseInt(args[7]));
        //
        // BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
        // "basictemp");
        // bf.createSpecificTable();
        // bf.specificf();
        // // The first parameter means quality and the second means depth
        // bf.basicf(20, 6);
        //
        // RepeatFilter rf=new
        // RepeatFilter(manager,args[2],"repeattemp","referencerepeat","basictemp");
        // rf.loadrepeat();
        // rf.establishrepeat();
        // rf.rfilter();
        //
        // ComphrehensiveFilter cf=new
        // ComphrehensiveFilter(manager,args[3],"comphrehensivetemp","refcomphrehensive","repeattemp");
        // cf.establishCom();
        // cf.loadcom();
        // cf.comphrehensiveF(2);
        //
        // dbSnpFilter sf=new
        // dbSnpFilter(manager,args[4],"snptemp","refsnp","comphrehensivetemp");
        // sf.establishsnp();
        // sf.dbSnpinput();
        // sf.snpFilter();
        //
        // pValueFilter pv=new
        // pValueFilter(manager,args[5],"pvtemp","refHg19","snptemp");
        // pv.loadHg19();
        // pv.fdr(args[6]);
        //
        // }
        manager.closeDatabase();
    }
}
