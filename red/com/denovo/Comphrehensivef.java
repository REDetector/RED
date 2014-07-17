package com.denovo;

/**
 * Comphrehensive phase
 * we focus on base in exon
 * we discard base in the rear or front of the sequence
 */

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Comphrehensivef {
	private String[] sql=new String[3];
	Dbcon db=new Dbcon();

//	FileInputStream inputStream;
	String comIn=null;
	String line=null;
	StringBuffer s1 = new StringBuffer();
	int count=3;
	int index=0;
	StringBuffer s2 = new StringBuffer();
	//insert 时候使用的列名
	StringBuffer s3 = new StringBuffer();
	String chr = null;
	String ps=null;
	// 设置日期格式
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
	public Comphrehensivef(String comIn){
		this.comIn=comIn;
	}

	public boolean esCom(){
		try {
			System.out.println("escom start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		db.usedb();
		
		sql[0]="drop table if exists comphrehensivetemp";
		sql[1]="create table comphrehensivetemp(chrome text,"+Utilities.getInstance().getS2()+")";
		db.result=db.stmt.executeUpdate(sql[0]);	
		db.result=db.stmt.executeUpdate(sql[1]);
		db.con.commit();
		
		System.out.println("escom end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		 return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean loadcom(){
		try {
			System.out.println("loadcom start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
		sql[0]="drop table if exists loadcom";
		sql[1]="create table loadcom(chrome varchar(30),ref varchar(30),type varchar(9),begin int,end int,unuse1 float(8,6),unuse2 varchar(5),unuse3 varchar(5),info varchar(100),index(chrome,type))";
		sql[2]="load data local infile '"+comIn+"' into table loadcom fields terminated by '\t' lines terminated by '\n'";
		db.result=db.stmt.executeUpdate(sql[0]);
		db.result=db.stmt.executeUpdate(sql[1]);
		db.result=db.stmt.executeUpdate(sql[2]);		
		db.con.commit();
		System.out.println("loadcom end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean comphrehensive(int edge){
		try {
			db.usedb();
			System.out.println("comf start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			sql[0]="select chrome,pos from repeattemp";
			db.rs=db.stmt.executeQuery(sql[0]);
			db.con.commit();	
			while(db.rs.next())
			{
				s2.append(db.rs.getString(1)+"\t");
				s3.append(db.rs.getString(2)+"\t");	
			}
		for(int i=0;i<s2.toString().split("\t").length;i++)
		{
		chr=s2.toString().split("\t")[i];
		ps=s3.toString().split("\t")[i];
		//It needs much to improve, multiple index may be a good idea. 
		sql[0]="select type from loadcom where ( ((begin<"+ps+"+"+edge+" and begin>"+ps+"-"+edge+") or (end<"+ps+"+"+edge+" and end>"+ps+"-"+edge+")) and type='CDS' and chrome='"+chr+"')";
//		System.out.println(ps+edge);
		db.rs=db.stmt.executeQuery(sql[0]);
		db.con.commit();
		if(!db.rs.next())
		{
			sql[0]="insert into comphrehensivetemp  select * from repeattemp where chrome='"+chr+"' and pos="+ps+"";
			db.result=db.stmt.executeUpdate(sql[0]);
			db.con.commit();
		}
		}
		
		//clear insert data
		s2.delete(0, s2.length());
		s3.delete(0, s3.length());
		
		System.out.println("comf end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		return true;
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void comPost() {
		try {
			System.out.println("post start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
			sql[0] = "create   temporary   table  newtable  select   distinct   *   from  comphrehensivetemp";
			sql[1] = "truncate   table  comphrehensivetemp";
			sql[2] = "insert   into   comphrehensivetemp select   *   from  newtable";
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
}
