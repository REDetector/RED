package com.test;

/**
 * we will filter out base in repeated area except for SINE/alu
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

public class Repeatedmask {
	private String[] sql=new String[3];
	Dbcon db=new Dbcon();
	String repeatIn=null;
	FileInputStream inputStream;
	String line=null;
	int count=3;
	int index=0;
	
	private StringBuffer s2 = new StringBuffer();
	//insert 时候使用的列名
	private StringBuffer s3 = new StringBuffer();
	String chr =null;
	String ps  =null;
	int timer=0;
	int minute=0;
	// 设置日期格式
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public Repeatedmask(String repeatIn){
		this.repeatIn=repeatIn;
	}
	
	public void loadrepeat(){
		try {
			System.out.println("loadrepeat start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
			int ts_count = 0;
			try {
				inputStream = new FileInputStream(repeatIn);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sql[0]="drop table if exists loadrepeat";
			//column index
			sql[1]="create table loadrepeat(chrome varchar(25),begin int,end int,type varchar(40),index(chrome,begin))";
			db.result=db.stmt.executeUpdate(sql[0]);
			db.result=db.stmt.executeUpdate(sql[1]);
			db.con.commit();
			BufferedReader rin = new BufferedReader(new InputStreamReader(inputStream));		
            while((line=rin.readLine())!=null){
            	//clear head of fa.out
            	line=line.replaceAll("['   ']+","\t");
            	if(count>0)
            		{
            		count--;
            		continue;
            		}
            	if(!line.startsWith("\t"))
            		index--;
            	if(line.split("\t")[index+5].length()>6)
            	{
            		index=0;
            		continue;
            	}            		
            	sql[0]="insert into loadrepeat(chrome,begin,end,type) values('"+line.split("\t")[index+5]+"','"+line.split("\t")[index+6]+"','"+line.split("\t")[index+7]+"','"+line.split("\t")[index+11]+"')";
            	db.result=db.stmt.executeUpdate(sql[0]);
            	ts_count++;
            	if(ts_count%30000==0)
    			db.con.commit();
//    			timer++;
//    			if(timer==50000)
//    			{
//    				minute++;
//    				System.out.println(minute*5+"W");
//    				timer=0;
//    			}
    			index=0;
            }
            db.con.commit();
            System.out.println("loadrepeat end"+" "+df.format(new Date()));// new Date()为获取当前系统时间	
		} catch ( IOException e) {
			// TODO Auto-generated catch block 
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void esrepeat(){
		try {
			System.out.println("esrepeat start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();

		sql[0]="drop table if exists repeattemp";
		db.result=db.stmt.executeUpdate(sql[0]);
		sql[1]="create table repeattemp(chrome text,"+Utilities.getInstance().getS2()+")";
		db.result=db.stmt.executeUpdate(sql[1]);
		db.con.commit();

		System.out.println("esrepeat end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void rfilter(){
			try {
				System.out.println("rfliter start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
				db.usedb();
			sql[0]="select chrome,pos from basictemp";
			db.rs=db.stmt.executeQuery(sql[0]);
			db.con.commit();	
			while(db.rs.next())
			{
				s2.append(db.rs.getString(1)+"\t");
				s3.append(db.rs.getString(2)+"\t");
			}
			int j=s2.toString().split("\\t").length;
		for(int i=0;i<j;i++)
		{
		chr=s2.toString().split("\t")[i];
		ps=s3.toString().split("\t")[i];
		sql[0]="select type from loadrepeat where (begin<"+ps+" and end>"+ps+" and chrome='"+chr+"')";
		db.rs=db.stmt.executeQuery(sql[0]);
		db.con.commit();
		//only base not belong to repeat area is ok
		if(!db.rs.next())
		{
			sql[0]="insert into repeattemp  select * from basictemp where chrome='"+chr+"' and pos="+ps+"";
			db.result=db.stmt.executeUpdate(sql[0]);
			db.con.commit();
		}
		//SINEalu is also what we need
		else if(db.rs.next()&&db.rs.getString(1)=="SINE/Alu")
		{
			sql[0]="insert into repeattemp  select * from basictemp where chrome='"+chr+"' and pos="+ps+"";
			db.result=db.stmt.executeUpdate(sql[0]);
			db.con.commit();
		}
		}
		//clear data
		s2.delete(0,s2.length());
		s3.delete(0,s3.length());
		System.out.println("rfilter end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
