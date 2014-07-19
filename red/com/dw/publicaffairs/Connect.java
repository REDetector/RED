package com.dw.publicaffairs;

import com.dw.denovo.BasicFilter;
import com.dw.denovo.ComphrehensiveFilter;
import com.dw.denovo.RepeatFilter;
import com.dw.denovo.dbSnpFilter;
import com.dw.denovo.denovoVcf;
import com.dw.denovo.pValueFilter;
import com.dw.dnarna.DnaRnaFilter;
import com.dw.dnarna.dnarnaVcf;
import com.dw.dnarna.llrFilter;

public class Connect {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DatabaseManager manager = DatabaseManager.getInstance();
		manager.connectDatabase("localhost", "3306", "root", "root");
		manager.createStatement();
		manager.setAutoCommit(true);
		Utilities.getInstance().createCalTable(
				args[1]);

//		boolean isDenovo = false;
//		if (!isDenovo) {
//			manager.createDatabase("dnarna");
//			manager.useDatabase("dnarna");
//
//			dnarnaVcf df = new dnarnaVcf(manager,
//					"D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf",
//					"D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.DNA.chr8.snvs.vcf",
//					"rnaVcf", "dnaVcf");
//			df.dnaVcf(10);
//			df.RnaVcf(10);
//
//			BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
//					"basictemp");
//			bf.createSpecificTable();
//			bf.specificf();
//			// The first parameter means quality and the second means depth
//			bf.basicf(20, 6);
//			
//			RepeatFilter rf=new RepeatFilter(manager,"D:/TDDOWNLOAD/data/hg19.fa.out","repeattemp","referencerepeat","basictemp");
//			rf.loadrepeat();
//			rf.establishrepeat();
//			rf.rfilter();
//			
//			ComphrehensiveFilter cf=new ComphrehensiveFilter(manager,"D:/TDDOWNLOAD/data/genes.gtf","comphrehensivetemp","refcomphrehensive","repeattemp");
//			 cf.establishCom();
//			 cf.loadcom();
//			 cf.comphrehensiveF(2);
//			
//			 dbSnpFilter sf=new dbSnpFilter(manager,"D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf","snptemp","refsnp","comphrehensivetemp");
//		     sf.establishsnp();
//			 sf.dbSnpinput();
//			 sf.snpFilter();
//			 
//			 DnaRnaFilter dr=new DnaRnaFilter(manager, "dnaVcf", "DnaRnatemp", "snptemp");
//			 dr.esdr();
//			 dr.dnarnaFilter();
//			 
//			 llrFilter lf = new llrFilter(manager, "dnaVcf", "llrtemp", "DnaRnatemp");
//			 lf.esllr();
//			 lf.llrtemp();
//			 
//			 pValueFilter pv=new pValueFilter(manager,"D:/TDDOWNLOAD/data/hg19.txt","pvtemp","refHg19","llrtemp");
//			 pv.loadHg19();
//			 pv.fdr("D:/software/R-3.0.1/bin/Rscript.exe");
//
//		} 
//		else {
//			manager.createDatabase("denovo");
//			manager.useDatabase("denovo");
//			
//			denovoVcf df=new denovoVcf(manager, "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf", "rnaVcf");
//			df.RnaVcf(10);
//			
//			BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
//					"basictemp");
//			bf.createSpecificTable();
//			bf.specificf();
//			// The first parameter means quality and the second means depth
//			bf.basicf(20, 6);
//			
//			RepeatFilter rf=new RepeatFilter(manager,"D:/TDDOWNLOAD/data/hg19.fa.out","repeattemp","referencerepeat","basictemp");
//			rf.loadrepeat();
//			rf.establishrepeat();
//			rf.rfilter();
//			
//			ComphrehensiveFilter cf=new ComphrehensiveFilter(manager,"D:/TDDOWNLOAD/data/genes.gtf","comphrehensivetemp","refcomphrehensive","repeattemp");
//			 cf.establishCom();
//			 cf.loadcom();
//			 cf.comphrehensiveF(2);
//			
//			 dbSnpFilter sf=new dbSnpFilter(manager,"D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf","snptemp","refsnp","comphrehensivetemp");
//		     sf.establishsnp();
//			 sf.dbSnpinput();
//			 sf.snpFilter();
//			 
//			 pValueFilter pv=new pValueFilter(manager,"D:/TDDOWNLOAD/data/hg19.txt","pvtemp","refHg19","snptemp");
//			 pv.loadHg19();
//			 pv.fdr("D:/software/R-3.0.1/bin/Rscript.exe");
//			
//		}

		boolean isDenovo = Boolean.parseBoolean(args[0]);
		if (!isDenovo) {
			manager.createDatabase("dnarna");
			manager.useDatabase("dnarna");

			dnarnaVcf df = new dnarnaVcf(manager,
					args[1],
					args[2],
					"rnaVcf", "dnaVcf");
			df.dnaVcf(Integer.parseInt(args[8]));
			df.RnaVcf(Integer.parseInt(args[8]));

			BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
					"basictemp");
			bf.createSpecificTable();
			bf.specificf();
			// The first parameter means quality and the second means depth
			bf.basicf(20, 6);
			
			RepeatFilter rf=new RepeatFilter(manager,args[3],"repeattemp","referencerepeat","basictemp");
			rf.loadrepeat();
			rf.establishrepeat();
			rf.rfilter();
			
			ComphrehensiveFilter cf=new ComphrehensiveFilter(manager,args[4],"comphrehensivetemp","refcomphrehensive","repeattemp");
			 cf.establishCom();
			 cf.loadcom();
			 cf.comphrehensiveF(2);
			
			 dbSnpFilter sf=new dbSnpFilter(manager,args[5],"snptemp","refsnp","comphrehensivetemp");
		     sf.establishsnp();
			 sf.dbSnpinput();
			 sf.snpFilter();
			 
			 DnaRnaFilter dr=new DnaRnaFilter(manager, "dnaVcf", "DnaRnatemp", "snptemp");
			 dr.esdr();
			 dr.dnarnaFilter();
			 
			 llrFilter lf = new llrFilter(manager, "dnaVcf", "llrtemp", "DnaRnatemp");
			 lf.esllr();
			 lf.llrtemp();
			 
			 pValueFilter pv=new pValueFilter(manager,args[6],"pvtemp","refHg19","llrtemp");
			 pv.loadHg19();
			 pv.fdr(args[7]);

		} 
		else {
			manager.createDatabase("denovo");
			manager.useDatabase("denovo");
			
			denovoVcf df=new denovoVcf(manager, args[1], "rnaVcf");
			df.RnaVcf(Integer.parseInt(args[7]));
			
			BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
					"basictemp");
			bf.createSpecificTable();
			bf.specificf();
			// The first parameter means quality and the second means depth
			bf.basicf(20, 6);
			
			RepeatFilter rf=new RepeatFilter(manager,args[2],"repeattemp","referencerepeat","basictemp");
			rf.loadrepeat();
			rf.establishrepeat();
			rf.rfilter();
			
			ComphrehensiveFilter cf=new ComphrehensiveFilter(manager,args[3],"comphrehensivetemp","refcomphrehensive","repeattemp");
			 cf.establishCom();
			 cf.loadcom();
			 cf.comphrehensiveF(2);
			
			 dbSnpFilter sf=new dbSnpFilter(manager,args[4],"snptemp","refsnp","comphrehensivetemp");
		     sf.establishsnp();
			 sf.dbSnpinput();
			 sf.snpFilter();
			 
			 pValueFilter pv=new pValueFilter(manager,args[5],"pvtemp","refHg19","snptemp");
			 pv.loadHg19();
			 pv.fdr(args[6]);
			
		}
		manager.closeDatabase();
	}
}
