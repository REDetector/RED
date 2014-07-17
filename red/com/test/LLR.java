package com.test;

/**
 * LLR used for detecting editing sites
 */

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LLR {
	private String[] sql = new String[3];
	Dbcon db = new Dbcon();
	// private File file=new File("D:/TDDOWNLOAD/data/BJ22T.chr8.sam");
	// private FileInputStream inputStream;
	private String chr;
	private String ps;
	private String chrom=null;
	private StringBuffer s2 = new StringBuffer();
	private StringBuffer s3 = new StringBuffer();
	private StringBuffer ref = new StringBuffer();
	private StringBuffer alt = new StringBuffer();
	int ref_n=0;
	int alt_n=0;
	int i = 0;
	private int[] refqual = new int[100];
	private int[] altqual = new int[100];
	// 设置日期格式
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public LLR() {
	   
		}
	public boolean esllr() {
		try {
			System.out.println("esllr start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();

			sql[0] = "drop table if exists llrtemp";
			sql[1] = "create table llrtemp(chrome text," + Utilities.getInstance().getS2() + ")";
			db.result = db.stmt.executeUpdate(sql[0]);
			System.out.println(Utilities.getInstance().getS2());
			db.result = db.stmt.executeUpdate(sql[1]);
			db.con.commit();

			System.out.println("esllr end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public void llrtemp() {
		try {
			System.out.println("llrtemp start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
			
			boolean bool=false;
//			sql[0]="select chrome from refDnavcf limit 0,1";
			sql[0]="select chrome from Dnavcf limit 0,1";
			db.rs=db.stmt.executeQuery(sql[0]);
			db.con.commit();
			if(db.rs.next()&&db.rs.getString(1).length()<3)
			{
			bool=true;
			}
			
//			sql[0] = "select chrome,pos,AD from DnaRnatemp";
			sql[0] = "select chrome,pos,AD from snptemp";
			db.rs = db.stmt.executeQuery(sql[0]);
			db.con.commit();
			
			while(db.rs.next())
			{
				if(bool)
				{
				chrom=db.rs.getString(1).replace("chr", "");
				s2.append(chrom+"\t");
				s3.append(db.rs.getString(2)+"\t");
				String[] col=db.rs.getString(3).split(";");
				ref.append(col[0]+"\t");
				alt.append(col[1]+"\t");
				}
				else
				{
					s2.append(db.rs.getString(1)+"\t");
					s3.append(db.rs.getString(2)+"\t");
					String[] col=db.rs.getString(3).split(";");
					ref.append(col[0]+"\t");
					alt.append(col[1]+"\t");
				}
			}

			
//		Find for q
		int j=s2.toString().split("\\t").length;
		for(int i=0;i<j;i++)
		{
			chr=s2.toString().split("\\t")[i];
			ps=s3.toString().split("\\t")[i];
			ref_n=Integer.parseInt(ref.toString().split("\\t")[i]);
			alt_n=Integer.parseInt(alt.toString().split("\\t")[i]);

			double q=0;
//		sql[0] = "select qual from refDnavcf where chrome='"+chr+"' and pos="+ps+"";
		sql[0] = "select qual from Dnavcf where chrome='"+chr+"' and pos="+ps+"";
		db.rs = db.stmt.executeQuery(sql[0]);
		db.con.commit();
		while (db.rs.next()) {
			q=db.rs.getDouble(1);
		}
			if(alt_n+ref_n>0){
				
			double f_ml=1.0*ref_n/(ref_n+alt_n);
			double y=Math.pow(f_ml,ref_n)*Math.pow(1-f_ml,alt_n); 
			y=Math.log(y)/Math.log(10.0);
			double judge=0.0;
			judge=y+q/10.0;
//			System.out.println(judge);
			System.out.println(ref_n+" "+alt_n+" "+y+" "+judge);
			if(judge>=4){
				if(bool)
				{
				chr="chr"+chr;
				sql[1]="insert into llrtemp select * from snptemp where chrome='"+chr+ "' and pos=" + ps+"";
				db.result=db.stmt.executeUpdate(sql[1]);
				db.con.commit();
				}
				else{
					sql[1]="insert into llrtemp select * from snptemp where chrome='"+chr+ "' and pos=" + ps+"";
					db.result=db.stmt.executeUpdate(sql[1]);
					db.con.commit();
				}
//				sql[0]="insert into llrtemp  select * from DnaRnatemp where chrome='"+chr+ "' and pos=" + ps+"";
			
			}
			}
			}
			System.out.println("llrtemp end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void post() {
		try {
			System.out.println("post start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
			sql[0] = "create   temporary   table  newtable  select   distinct   *   from  llrtemp";
			sql[1] = "truncate   table  llrtemp";
			sql[2] = "insert   into   llrtemp select   *   from  newtable";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.result = db.stmt.executeUpdate(sql[1]);
			db.result = db.stmt.executeUpdate(sql[2]);
			db.con.commit();
			sql[0] = "drop   table newtable";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.con.commit();
			System.out.println("post end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

	
	
//	while (db.rs.next()) {
//		s1.append(db.rs.getString(1) + "\t");
//		s2.append(db.rs.getInt(2) + "\t");
//	}
//	sql[0] = "select seq from sam where rname='" + s1.toString().split("\t")[0]+ "' and pos='" + s2.toString().split("\t")[0] + "' limit 0,1";
//	db.rs = db.stmt.executeQuery(sql[0]);
//	db.con.commit();
//	while (db.rs.next()) {
//		i = db.rs.getString(1).length();
//	}
//	//Find for alt,ref
//	for(int j=0;j<s1.toString().split("\t").length;j++){
//		chr=s1.toString().split("\t")[j];
//		ps=s2.toString().split("\t")[j];
////		System.out.println(chr+" "+ps);
//		int q=0;
//		int alt = 0;
//		int ref = 0;
//	sql[0] = "select pos,seq,qual from sam where rname='"+ chr + "' and (pos>='" + ps+ "' and pos<('" + ps + "'+'" + i + "'))";
//	//
//	db.rs = db.stmt.executeQuery(sql[0]);
//	db.con.commit();
//	while (db.rs.next()) {
//		int off =0;
////		System.out.println(db.rs.getInt(1));
//		off = db.rs.getInt(1) - Integer.parseInt(ps);
//		if (db.rs.getString(2).charAt(off) == 'A') {
//			ref++;
//			refqual[ref] = db.rs.getString(3).charAt(off)-33;
//			q+=refqual[ref];
//		}
//		if (db.rs.getString(2).charAt(off) == 'G') {
//			alt++;
//			altqual[alt] = db.rs.getString(3).charAt(off)-33;
//			q+=altqual[alt];
//		}
////		for(char n:altqual)	
//	}