
package com.test;

/**
 * Basic process for RNA-editing
 * specific filter means we only focus on A-G
 * basic filter means we set threshold on quality and depth
 */

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Basicf {
	Dbcon db = new Dbcon();
	String refDir =null;
	private String[] sql = new String[3];
	StringBuffer s2 = new StringBuffer();
	//insert 时候使用的列名
	StringBuffer s3 = new StringBuffer();
	
	StringBuffer ref = new StringBuffer();
	StringBuffer alt = new StringBuffer();
	String chrom=null;
	String chr = null;
	String ps=null;
	// 设置日期格式
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public int ref_n=0;
	public int alt_n=0;
	
	public Basicf(String refDir){
		this.refDir=refDir;
	}
	
	public void esSpecific(){
		try {
		db.usedb();
//		Utilities.getInstance().createCalTable(refDir);
		sql[0] = "drop table if exists specifictemp";
		sql[1] = "create table specifictemp(chrome varchar(30)," +Utilities.getInstance().getS2()+ ",index(chrome,pos))";
		db.result = db.stmt.executeUpdate(sql[0]);
		db.result = db.stmt.executeUpdate(sql[1]);
		db.con.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void specificf() {
		try {
			System.out.println("specific start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
//			Utilities.getInstance().createCalTable(refDir);
			sql[2] = "insert into specifictemp(" +Utilities.getInstance().getS3()+ ")  select * from RnaVcf where REF='A' AND ALT='G'";
			db.result = db.stmt.executeUpdate(sql[2]);
			db.con.commit();

			System.out.println("specific end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void spePost() {
		try {
			System.out.println("post start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
			sql[0] = "create   temporary   table  newtable  select   distinct   *   from  specifictemp";
			sql[1] = "truncate   table  specifictemp";
			sql[2] = "insert   into   specifictemp select   *   from  newtable";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.result = db.stmt.executeUpdate(sql[1]);
			db.result = db.stmt.executeUpdate(sql[2]);
			sql[0] = "drop   table   newtable";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.con.commit();
			System.out.println("post end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void basicf(int qua,int num) {
		try {
			System.out.println("bfilter start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
		
			//create table and insert data into it
			sql[0] = "drop table if exists basictemp";
			sql[1] = "create table basictemp(chrome varchar(30)," + Utilities.getInstance().getS2() + ",index(chrome,pos))";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.result = db.stmt.executeUpdate(sql[1]);
			sql[0]="select chrome,pos,AD from specifictemp";
			db.rs=db.stmt.executeQuery(sql[0]);
			db.con.commit();
			while(db.rs.next()){
				s2.append(db.rs.getString(1)+"\t");
				s3.append(db.rs.getString(2)+"\t");
				String[] col=db.rs.getString(3).split(";");
				ref.append(col[0]+"\t");
				alt.append(col[1]+"\t");
			}
			int j=s2.toString().split("\\t").length;
			for(int i=0;i<j;i++)
			{
				chr=s2.toString().split("\\t")[i];
				ps=s3.toString().split("\\t")[i];
				ref_n=Integer.parseInt(ref.toString().split("\\t")[i]);
				alt_n=Integer.parseInt(alt.toString().split("\\t")[i]);
				if((ref_n+alt_n)>num)
				{
					sql[2] = "insert into basictemp(" + Utilities.getInstance().getS3()+ ")  (select * from specifictemp where filter='PASS' and pos="+ps+" and qual>"+qua+" and chrome='"+chr+"')";
					db.result = db.stmt.executeUpdate(sql[2]);
					db.con.commit();
				}
			}
			//clear insert data
			s2.delete(0, s2.length());
			s3.delete(0, s3.length());
			System.out.println("bfilter end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
