package com.dw.publicaffairs;

import com.dw.denovo.*;
import com.dw.dnarna.DnaRnaFilter;
import com.dw.dnarna.DnaRnaVcf;
import com.dw.dnarna.LLRFilter;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manager.createStatement();
		manager.setAutoCommit(true);

//		boolean isDenovo = false;
//		if (!isDenovo) {
//			manager.createDatabase("dnarna");
//			manager.useDatabase("dnarna");
//			Utilities.getInstance().createCalTable(
//					"D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf");

			
//			 DnaRnaVcf df = new DnaRnaVcf(manager,
//			 "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf",
//			 "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.DNA.chr8.snvs.vcf",
//			 "rnaVcf", "dnaVcf");
//			 df.establishDnaTable();
//			 df.dnaVcf(10);
//			 df.establishRnaTable();
//			 df.RnaVcf(10);
			
//			 BasicFilter bf = new BasicFilter(manager, "rnaVcf",
//			 "specifictemp",
//			 "basictemp");
//			 bf.createSpecificTable();
//			 bf.specificf();
//			 bf.createBasicTable();
//			 // The first parameter means quality and the second means depth
//			 bf.basicFilter(20, 6);
//			 bf.distinctTable();
			 
//			RepeatFilter rf = new RepeatFilter(manager,
//					"D:/TDDOWNLOAD/data/hg19.fa.out", "repeattemp",
//					"referencerepeat", "basictemp");
			// rf.loadrepeat();
//			rf.establishrepeat();
//			 rf.repeatFilter();
//			rf.rfilter();
			 
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
//			 Vector<Probe> probes=bf.queryEditingSite();
//				for(Probe probe:probes){
//					System.out.println(probe.getChr()+" "+probe.getStart()+" "+probe.getEditingBase());
//				}
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
//		}
		// else {
		// manager.createDatabase("denovo");
		// manager.useDatabase("denovo");
		//
		// denovoVcf df=new denovoVcf(manager,
		// "D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf", "rnaVcf");
		// df.RnaVcf(10);
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

		 boolean isDenovo = Boolean.parseBoolean(args[0]);
		 if (!isDenovo) {
		 manager.createDatabase(args[9]);
		 manager.useDatabase(args[9]);
		
		 DnaRnaVcf df = new DnaRnaVcf(manager,args[1],args[2],"rnaVcf",
		 "dnaVcf");
		 Utilities.getInstance().createCalTable(args[2]);
		 df.establishDnaTable();
		 df.dnaVcf(Integer.parseInt(args[8]));
		
		 Clear cl=new Clear();
		 cl.clear(Utilities.getInstance().getS2(),Utilities.getInstance().getS3());

		 Utilities.getInstance().createCalTable(args[1]);
		 df.establishRnaTable();
		 df.RnaVcf(Integer.parseInt(args[8]));
		
		 BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
		 "basictemp");
		 bf.createSpecificTable();
		 bf.specificf();
		 bf.createBasicTable();
		 // The first parameter means quality and the second means depth
		 bf.basicFilter(20, 6);
		 bf.distinctTable();
		
		 RepeatFilter rf=new
		 RepeatFilter(manager,args[3],"repeattemp","referencerepeat");
		 rf.loadrepeat();
		 rf.establishrepeat();
		 rf.rfilter("basictemp");
//		 rf.repeatFilter("basictemp");
		 rf.distinctTable();
		
		 ComphrehensiveFilter cf=new
		 ComphrehensiveFilter(manager,args[4],"comphrehensivetemp","refcomphrehensive");
		 cf.establishCom();
		 cf.loadcom();
		 cf.comphrehensiveF("repeattemp",2);
		 cf.distinctTable();
		
		 DbsnpFilter sf=new
		 DbsnpFilter(manager,args[5],"snptemp","refdbsnp");
		 sf.establishsnp();
		 sf.loadRefdbSnp();
		 sf.snpFilter("comphrehensivetemp");
		 sf.distinctTable();
		
		 DnaRnaFilter dr=new DnaRnaFilter(manager, "dnaVcf", "DnaRnatemp",
		 "snptemp");
		 dr.createDnaRnaTable();
		 dr.dnarnaFilter();
		 dr.distinctTable();

             LLRFilter lf = new LLRFilter(manager, "dnaVcf", "llrtemp"
                     );
		 lf.createLlrTable();
		 lf.llrtemp("DnaRnatemp");
		 lf.distinctTable();
		
		 PValueFilter pv=new
		 PValueFilter(manager,args[6],"pvtemp","refHg19");
		 pv.estblishPvTable();

		 pv.loadRefHg19();
		 pv.fdr("llrtemp",args[7]);
		
		 }
		 else {
		 manager.createDatabase(args[8]);
		 manager.useDatabase(args[8]);
		 Utilities.getInstance().createCalTable(args[1]);
		
		 DenovoVcf df=new DenovoVcf(manager, args[1], "rnaVcf");
		 df.establishRnaTable();
		 df.rnaVcf(Integer.parseInt(args[7]));
		
		 BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
		 "basictemp");
		 bf.createSpecificTable();
		 bf.specificf();
		 bf.createBasicTable();
		 // The first parameter means quality and the second means depth
		 bf.basicFilter(20, 6);
		 bf.distinctTable();
		
		 RepeatFilter rf=new
		 RepeatFilter(manager,args[2],"repeattemp","referencerepeat");
		 rf.loadrepeat();
		 rf.establishrepeat();
		 rf.rfilter("basictemp");
		 rf.repeatFilter("basictemp");
		 rf.distinctTable();
		
		 ComphrehensiveFilter cf=new
		 ComphrehensiveFilter(manager,args[3],DatabaseManager.COMPREHENSIVE_FILTER_TABLE_NAME,"refcomphrehensive");
		 cf.establishCom();
		 cf.loadcom();
		 cf.comphrehensiveF("repeattemp",2);
		 cf.distinctTable();
		
		 DbsnpFilter sf=new
		 DbsnpFilter(manager,args[4],"snptemp","refsnp");
		 sf.establishsnp();
		 sf.loadRefdbSnp();
		 sf.snpFilter("comphrehensivetemp");
		 sf.distinctTable();
		
		 PValueFilter pv=new
		 PValueFilter(manager,args[5],"pvtemp","refHg19");
		 pv.estblishPvTable();
		 pv.loadRefHg19();
		 pv.fdr("snptemp",args[6]);
		
		 }
		manager.closeDatabase();
	}
}
