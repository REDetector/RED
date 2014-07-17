package com.test;

/**
 * Detect SNP in DNA level
 */
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DnaRna {
	private String[] sql = new String[3];
	// public String[] getSql() {
	// return sql;
	// }
	// public void setSql(String[] sql) {
	// this.sql = sql;
	// }
	Dbcon db = new Dbcon();
	String chr = null;
	String ps = null;
	StringBuffer s1 = new StringBuffer();
	StringBuffer s2 = new StringBuffer();
	StringBuffer s3 = new StringBuffer();
	String chrom = null;
	private int ref_n=0;
	private int alt_n=0;
	int temp = 0;
	// 设置日期格式
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public DnaRna(){
		
	}
	public boolean esdr() {
		try {
			System.out.println("esdr start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();

			sql[0] = "drop table if exists DnaRnatemp";
			sql[1] = "create table DnaRnatemp(chrome varchar(30)," + Utilities.getInstance().getS2() + ")";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.result = db.stmt.executeUpdate(sql[1]);
			db.con.commit();
			
			System.out.println("esdr end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	
	public void dnaF(){
		try {
			System.out.println("df start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
			
			//whether it is 
			boolean bool=false;
			sql[0]="select chrome from dnavcf limit 0,1";
			db.rs=db.stmt.executeQuery(sql[0]);
			db.con.commit();
			if(db.rs.next()&&db.rs.getString(1).length()<3)
			{
			bool=true;
			}
			
			sql[0]="select chrome,pos from snptemp";
			db.rs=db.stmt.executeQuery(sql[0]);
			db.con.commit();
			while(db.rs.next())
			{
				if(bool)
				{
				chrom=db.rs.getString(1).replace("chr", "");
				s2.append(chrom+"\t");
				s3.append(db.rs.getString(2)+"\t");
				}
				else
				{
				s2.append(db.rs.getString(1)+"\t");
				s3.append(db.rs.getString(2)+"\t");
				}
			}

			int m=s2.toString().split("\\t").length;
			for(int i=0;i<m;i++)
			{
				chr=s2.toString().split("\\t")[i];
				ps=s3.toString().split("\\t")[i];
				
					//The first six base will be filtered out
					sql[0] = "select GT from Dnavcf where chrome='"+chr+"' and pos="+ps+"";
					db.rs = db.stmt.executeQuery(sql[0]);
					db.con.commit();
//					System.out.println("df middleB"+i+" "+df.format(new Date()));// new Date()为获取当前系统时间
					while (db.rs.next()) 
					{
						ref_n=Integer.parseInt(db.rs.getString(1).split("/")[0]);
						alt_n=Integer.parseInt(db.rs.getString(1).split("/")[1]);
						if ((ref_n==0)&&(alt_n==0)) 
						{
							if(bool)
							{
							chr="chr"+chr;
							sql[1] = "insert into DnaRnatemp select * from snptemp where chrome='"+ chr+ "' and pos="+ ps+ "";
//							System.out.println(chr+" "+ps);
							db.result = db.stmt.executeUpdate(sql[1]);
							db.con.commit();
							}
							else
							{
								sql[1] = "insert into DnaRnatemp select * from snptemp where chrome='"+ chr+ "' and pos="+ ps+ "";
								db.result = db.stmt.executeUpdate(sql[1]);
								db.con.commit();
							}
						
						}
					}
			}
			//clear insert data
			s2.delete(0, s2.length());
			s3.delete(0, s3.length());
			System.out.println("df end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
