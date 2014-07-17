package com.test;

import java.sql.SQLException;

public class Connect {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Dbcon db=new Dbcon();
		try {
			db.dbcon();
//			db.usebase();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//construct table structure 
//		Utilities.getInstance().createCalTable("D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.DNA.chr8.snvs.vcf");
//		VcfInput vi=new VcfInput("D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.DNA.chr8.snvs.vcf","D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf");
		Utilities.getInstance().createCalTable(args[0]);
		VcfInput vi=new VcfInput(args[0],args[1]);
		//The parameter means whether we study N(9) or T(10)
//		vi.dnaVcf(10);
//		vi.RnaVcf(9);
		vi.dnaVcf(Integer.parseInt(args[7]));
		vi.RnaVcf(Integer.parseInt(args[7]));
//		Basicf bf=new Basicf("D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.DNA.chr8.snvs.vcf");
		Basicf bf=new Basicf(args[0]);
		bf.esSpecific();
		bf.specificf();
		bf.spePost();
		//The first parameter means quality and the second means depth
		bf.basicf(20,6);
//		Repeatedmask rm=new Repeatedmask("D:/TDDOWNLOAD/data/hg19.fa.out");
		Repeatedmask rm=new Repeatedmask(args[2]);
		rm.loadrepeat();
		rm.esrepeat();
		rm.rfilter();
//		Comphrehensivef cf=new Comphrehensivef("D:/TDDOWNLOAD/data/genes.gtf");
		Comphrehensivef cf=new Comphrehensivef(args[3]);
		cf.esCom();
		cf.loadcom();
		//The parameter means the edge base of each sequence
		cf.comphrehensive(2);
		cf.comPost();
		DnaRna dr=new DnaRna();
		dr.esdr();
		dr.dnaF();
//		Snpf sf=new Snpf("D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf");
		Snpf sf=new Snpf(args[4]);
		sf.essnp();
		sf.dbSnpinput();
		sf.snpF();
		LLR lr=new LLR();
		lr.esllr();
		lr.llrtemp();
		lr.post();
//		PValue pv=new PValue("D:/TDDOWNLOAD/data/hg19.txt");
		PValue pv=new PValue(args[5]);
		pv.loadHg19();
//		pv.fdr("D:/software/R-3.0.1/bin/Rscript.exe");
		pv.fdr(args[6]);
		db.dbclose();
	}
}


	


