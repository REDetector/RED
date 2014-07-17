package com.test;

/**
 * import vcf file
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VcfInput {
	//establish database connection;
	Dbcon db = new Dbcon();
	// data redirectory;
	// File file;
	// String dir;
//	File RNAfile = new File(
//			"D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf");
//	File DNAfile = new File(
//			"D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.DNA.chr8.snvs.vcf");
	String dnaFile=null;
	String rnaFile=null;
	
	FileInputStream inputStream;
	//SQL to be executed
	private String[] sql = new String[3];
	//basic unit to process
	String line = null;
	//data of each column
	String[] col = new String[40];
	String[] temp = new String[10];
	// insert时使用的数据
	StringBuffer s1 = new StringBuffer();
	// create table时使用的字符串
	StringBuffer s2 = new StringBuffer();
	// insert 时候使用的列名
	StringBuffer s3 = new StringBuffer();
	//count for each function
	int count_t=1;
	int count_r=1;
	int count_d=1;
	int depth = 0;
	int gtype=0;
	private int ref=0;
	private int alt=0;
	// 设置日期格式
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// public RedInput()
	// {
	// this.file=new File(dir);
	// }
	// public String directory()
	// {
	// dir="D:/TDDOWNLOAD/HCC448T.subset.vcf";
	// return dir;
	// }
	// establish table structure for following tables
	
	public VcfInput(String dnaDir,String rnaDir){
		this.dnaFile=dnaDir;
		this.rnaFile=rnaDir;
	}
	
	public int getdepth() {
		try {
			inputStream = new FileInputStream(rnaFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader rin = new BufferedReader(new InputStreamReader(
				inputStream));
		try {
			while ((line = rin.readLine()) != null) {
				s1 = new StringBuffer();
				if (line.startsWith("##"))
					continue;
				if (line.startsWith("#"))
					continue;
				// value in each line
				for (int k = 0; k < line.split("\\t")[8].split(":").length; k++) {
					if (line.split("\\t")[8].split(":")[k].equals("DP")) {
						depth = k;
					}
				}
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(depth);
		return depth;
	}

	// table for RnaVcf X.length-9=time for circulation
	public void RnaVcf(int num) {
		System.out.println("rnavcf start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		try {
			// 初始化
			db.usedb();

			//timer for transaction
			int ts_count=0;
			try {
				inputStream = new FileInputStream(rnaFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader rin = new BufferedReader(new InputStreamReader(
					inputStream));
			while ((line = rin.readLine()) != null) {
				s1 = new StringBuffer();
				if (line.startsWith("##"))
					continue;
				if (line.startsWith("#")) {
					continue;
				}
				for (int i = 0; i < line.split("\\t").length; i++) {
					col[i] = line.split("\\t")[i];
				}
				if(count_r>0){
					for (int i = 0; i < col[8].split(":").length; i++) {
						if (line.split("\\t")[8].split(":")[i].equals("DP")) {
							depth = i;
						}
					}
					sql[0] = "drop table if exists RnaVcf";
					db.result = db.stmt.executeUpdate(sql[0]);
					sql[1] = "create table RnaVcf(chrome varchar(15)," +Utilities.getInstance().getS2()+ ",index(chrome,pos))";
					db.result = db.stmt.executeUpdate(sql[1]);
					db.con.commit();
					if (db.result != -1) {
//						System.out.println("创建RNA数据表成功");
					}
					count_r--;
				}
				// data for import
				// '.' stands for undetected, so we discard it
				if (col[num].split(":")[depth].equals("."))
					continue;
				s1.append("'" + col[0] + "'");
				for (int i = 1; i < 8; i++)
					s1.append("," + "'" + col[i] + "'");
				for (int i = 0; i < col[num].split(":").length; i++) {
					temp[i] = col[num].split(":")[i].replace(",", ";");
					// System.out.println(temp[i]);
					s1.append("," + "'" + temp[i] + "'");
				}
				// 数据库数据插入，每行插入
				sql[2] = "insert into RnaVcf(" + Utilities.getInstance().getS3() + ") values(" + s1 + ")";
				db.result = db.stmt.executeUpdate(sql[2]);
				ts_count++;
				if(ts_count%20000==0)
				db.con.commit();
			}
			db.con.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//clear insert data
		s1.delete(0, s1.length());
		System.out.println("rnavcf end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
	}

	// table for DnaVcf
	public void dnaVcf(int num) {
		System.out.println("dnavcf start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		try {
			// 初始化
			db.usedb();
			int ts_count=0;
			try {
				inputStream = new FileInputStream(dnaFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader rin = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = rin.readLine()) != null) {
				s1 = new StringBuffer();
				if (line.startsWith("##"))
					continue;
				if (line.startsWith("#")) {
					continue;
				}
				for (int i = 0; i < line.split("\\t").length; i++) {
					col[i] = line.split("\\t")[i];
				}
				if (count_d > 0) {
					sql[0] = "drop table if exists DnaVcf";
					db.result = db.stmt.executeUpdate(sql[0]);
//					System.out.println(s2 + " " + s3);
					sql[1] = "create table DnaVcf(chrome varchar(15)," + Utilities.getInstance().getS2()+ ",index(chrome,pos))";
					db.result = db.stmt.executeUpdate(sql[1]);
					db.con.commit();
					db.con.commit();
					for (int i = 0; i < col[8].split(":").length; i++) {
						if (col[8].split(":")[i].equals("DP")) {
							depth = i;
						}
						if(col[8].split(":")[i].equals("GT")){
							gtype=i;
						}
					}
					if (db.result != -1) {
//						System.out.println("创建DNA数据表成功");
					}
					count_d--;
				}
				// data for import
				//we don't need unnecessary base ||(Float.parseFloat(col[5])<20)
				if(!(col[3].equals("A"))||!(col[6].equals("PASS"))){
					continue;
				}
				// '.' stands for undetected, so we discard it
				if ((col[num].split(":")[depth].equals("."))||(col[num].split(":")[gtype].split("/")[0].equals("."))||(col[num].split(":")[gtype].split("/")[1].equals(".")))
				{
					continue;
				}
				ref=Integer.parseInt(col[num].split(":")[gtype].split("/")[0]);
				alt=Integer.parseInt(col[num].split(":")[gtype].split("/")[1]);
				if((ref!=0)||(alt!=0)){
					continue;
				}
				
				
				
				s1.append("'" + col[0] + "'");
				for (int i = 1; i < 8; i++)
					s1.append("," + "'" + col[i] + "'");
				for (int i = 0; i < col[num].split(":").length; i++) {
					temp[i] = col[num].split(":")[i].replace(",", ";");
					// System.out.println(temp[i]);
					s1.append("," + "'" + temp[i] + "'");
				}
				// 数据库数据插入，每行插入
				sql[2] = "insert into DnaVcf(" + Utilities.getInstance().getS3() + ") values(" + s1 + ")";
				db.result = db.stmt.executeUpdate(sql[2]);
				ts_count++;
				if(ts_count%20000==0)
				db.con.commit();
			}
			db.con.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//clear insert data
		s1.delete(0, s1.length());
		System.out.println("dnavcf end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
	}
	
	
}
