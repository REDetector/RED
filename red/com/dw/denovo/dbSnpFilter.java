package com.dw.denovo;

/**
 * we will filter out base which already be recognized
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;

public class dbSnpFilter {
	private DatabaseManager databaseManager;
	private String line = null;

	private int count = 0;
	private String snpIn=null;
	private String dbSnpTable=null;
	private String referencedbSnp=null;
	private String refTable=null;
//	File file = new File("D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf");
	// 锟斤拷锟斤拷锟斤拷锟节革拷式
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
	public dbSnpFilter(DatabaseManager databaseManager,String snpIn,String dbSnpTable,String referencedbSnp,String refTable) {
	        this.databaseManager = databaseManager;
	        this.snpIn = snpIn;
	        this.dbSnpTable=dbSnpTable;
	        this.referencedbSnp=referencedbSnp;
	        this.refTable=refTable;
	    }
	
	public boolean establishsnp() {
			System.out.println("establishsnp start"+" "+df.format(new Date()));
			
			databaseManager.deleteTable(dbSnpTable);
			databaseManager.createTable(dbSnpTable, "(chrome varchar(30)," +Utilities.getInstance().getS2() + ")");

			System.out.println("establishsnp end"+" "+df.format(new Date()));
			return true;
		
	}

	public boolean dbSnpinput() {
		try {
			System.out.println("dbsnp start"+" "+df.format(new Date()));// new Date()为锟斤拷取锟斤拷前系统时锟斤�?
			
			databaseManager.deleteTable(referencedbSnp);
			databaseManager.createTable(referencedbSnp, "(chrome varchar(15),pos varchar(30),index(chrome,pos))");
			FileInputStream inputStream = new FileInputStream(snpIn);
			BufferedReader rin = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = rin.readLine()) != null) {
				if (line.startsWith("#")) {
					count++;
					continue;
				} else
					break;
			}
			databaseManager.executeSQL("load data local infile '"+snpIn+"' into table "+referencedbSnp+" fields terminated by '\t' lines terminated by '\n' IGNORE "+ count + " LINES");
			
			System.out.println("dbsnp end"+" "+df.format(new Date()));// new Date()为锟斤拷取锟斤拷前系统时锟斤�?
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		return true;
	}

	public void snpFilter() {
			System.out.println("dbsnpf start"+" "+df.format(new Date()));// new Date()为锟斤拷取锟斤拷前系统时锟斤�?
			
			databaseManager.executeSQL("insert into "+dbSnpTable+" select *from "+refTable+" where not exists (select *FROM "+referencedbSnp+" where ("+refTable+".chrome="+referencedbSnp+".chrome and "+refTable+".pos="+referencedbSnp+".pos))");
	                        
			System.out.println("dbsnpf end"+" "+df.format(new Date()));// new Date()为锟斤拷取锟斤拷前系统时锟斤�?
	}
//	public void snpF(){
//		System.out.println("snpf start"+" "+df.format(new Date()));// new Date()为锟斤拷取锟斤拷前系统时锟斤�?
//		try {
//			
//			System.out.println("snpf end"+" "+df.format(new Date()));// new Date()为锟斤拷取锟斤拷前系统时锟斤�?
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	public void post() throws SQLException {
//		sql[0]="insert into snptemp select *from Comphrehensivetemp where not exists (select *FROM dbSnpVcf where (Comphrehensivetemp.chrome=dbSnpVcf.chrome and Comphrehensivetemp.pos=dbSnpVcf.pos))";
//		db.result = db.stmt.executeUpdate(sql[0]);
//		db.con.commit();
//		while (rs.next()) {
//                coordinate.add(rs.getString(1));
//                coordinate.add(rs.getString(2));
//            }
//            for (int i = 0, len = coordinate.size(); i < len; i++) {
//                switch (i % 2) {
//                    case 0:
//                        chr = coordinate.get(i);
//                        break;
//                    case 1:
//                        ps = coordinate.get(i);
//                        rs=databaseManager.query(referencedbSnp, "chrome", "(pos="+ ps+ " and chrome='"+ chr + "')");
//                        if(!rs.next())
//                		{
//                			databaseManager.executeSQL("insert into "+dbSnpTable+"  select * from "+comphrehensiveTable+" where chrome='"+chr+"' and pos="+ps+"");
//                			count++;
//                			if(count%10000==0)
//                				databaseManager.commit();
//                		}
//                        break;
//                }
//            }	                        
//            databaseManager.commit();
//            databaseManager.setAutoCommit(true);
//	}
}
