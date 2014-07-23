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
import java.util.Date;
import java.util.Vector;

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;
import com.xl.datatypes.probes.Probe;

public class DbsnpFilter {
	private DatabaseManager databaseManager;
	private String line = null;

	private int count = 0;
	private String snpIn = null;
	private String dbSnpTable = null;
	private String referencedbSnp = null;
	private String refTable = null;
	// File file = new File("D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf");
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public DbsnpFilter(DatabaseManager databaseManager, String snpIn,
			String dbSnpTable, String referencedbSnp, String refTable) {
		this.databaseManager = databaseManager;
		this.snpIn = snpIn;
		this.dbSnpTable = dbSnpTable;
		this.referencedbSnp = referencedbSnp;
		this.refTable = refTable;
	}

	public boolean establishsnp() {
		System.out.println("establishsnp start" + " " + df.format(new Date()));

		databaseManager.deleteTable(dbSnpTable);
		databaseManager.createTable(dbSnpTable, "(chrome varchar(15),"
				+ Utilities.getInstance().getS2() + "," + "index(chrome,pos))");

		System.out.println("establishsnp end" + " " + df.format(new Date()));
		return true;

	}

	public boolean establishRefdbSnp() {
		databaseManager.createTable(referencedbSnp,
				"(chrome varchar(15),pos int,index(chrome,pos))");
		ResultSet rs = databaseManager.query(referencedbSnp, "count(*)",
				"1 limit 0,100");
		int number = 0;
		try {
			if (rs.next()) {
				number = rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (number > 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean loadRefdbSnp() {
		try {
			System.out.println("loaddbsnp start" + " " + df.format(new Date()));
																			
			if(establishRefdbSnp()){
			FileInputStream inputStream = new FileInputStream(snpIn);
			BufferedReader rin = new BufferedReader(new InputStreamReader(
					inputStream));
			while ((line = rin.readLine()) != null) {
				if (line.startsWith("#")) {
					count++;
					continue;
				} else
					break;
			}
			rin.close();
			databaseManager
					.executeSQL("load data local infile '"
							+ snpIn
							+ "' into table "
							+ referencedbSnp
							+ " fields terminated by '\t' lines terminated by '\n' IGNORE "
							+ count + " LINES");
			}

			System.out.println("loaddbsnp end" + " " + df.format(new Date()));
																			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void snpFilter() {
		System.out.println("dbsnpf start" + " " + df.format(new Date()));

		databaseManager.executeSQL("insert into " + dbSnpTable
				+ " select * from " + refTable
				+ " where not exists (select chrome from " + referencedbSnp
				+ " where (" + referencedbSnp+ ".chrome=" + refTable + ".chrome and " + referencedbSnp+ ".pos=" + refTable + ".pos))");

		System.out.println("dbsnpf end" + " " + df.format(new Date()));
	}
	// public void snpF(){
	// System.out.println("snpf start"+" "+df.format(new Date()));// new
	// Date()涓洪敓鏂ゆ嫹鍙栭敓鏂ゆ嫹鍓嶇郴缁熸椂閿熸枻锟�
	// try {
	//
	// System.out.println("snpf end"+" "+df.format(new Date()));// new
	// Date()涓洪敓鏂ゆ嫹鍙栭敓鏂ゆ嫹鍓嶇郴缁熸椂閿熸枻锟�
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	// public void post() throws SQLException {
	// sql[0]="insert into snptemp select *from Comphrehensivetemp where not exists (select *FROM dbSnpVcf where (Comphrehensivetemp.chrome=dbSnpVcf.chrome and Comphrehensivetemp.pos=dbSnpVcf.pos))";
	// db.result = db.stmt.executeUpdate(sql[0]);
	// db.con.commit();
	// while (rs.next()) {
	// coordinate.add(rs.getString(1));
	// coordinate.add(rs.getString(2));
	// }
	// for (int i = 0, len = coordinate.size(); i < len; i++) {
	// switch (i % 2) {
	// case 0:
	// chr = coordinate.get(i);
	// break;
	// case 1:
	// ps = coordinate.get(i);
	// rs=databaseManager.query(referencedbSnp, "chrome", "(pos="+ ps+
	// " and chrome='"+ chr + "')");
	// if(!rs.next())
	// {
	// databaseManager.executeSQL("insert into "+dbSnpTable+"  select * from "+comphrehensiveTable+" where chrome='"+chr+"' and pos="+ps+"");
	// count++;
	// if(count%10000==0)
	// databaseManager.commit();
	// }
	// break;
	// }
	// }
	// databaseManager.commit();
	// databaseManager.setAutoCommit(true);
	// }
	public void distinctTable() {
		 System.out.println("post start" + " " + df.format(new Date()));
		
		 databaseManager.executeSQL("create temporary table newtable select distinct * from "
		 + dbSnpTable);
		 databaseManager.executeSQL("truncate table " + dbSnpTable);
		 databaseManager.executeSQL("insert into " + dbSnpTable +
		 " select * from  newtable");
		 databaseManager.deleteTable("newTable");
		
		 System.out.println("post end" + " " + df.format(new Date()));
		 }
	
	public Vector<Probe> queryAllEditingSites(){
		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(dbSnpTable, " chrome, pos,alt "," 1 ");
		try {
			while(rs.next()){
				Probe p=new Probe(rs.getString(1),rs.getInt(2),rs.getString(3).toCharArray()[0]);
				probeVector.add(p);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return probeVector;
	}
 
 public Probe queryEditingSite(String chrome,int pos){
		ResultSet rs=databaseManager.query(dbSnpTable, " chrome, pos ,alt "," chrome="+chrome+" and pos='"+pos+"' ");
		try {
			while(rs.next()){
				return new Probe(rs.getString(1),rs.getInt(2),rs.getString(3).toCharArray()[0]);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
	}
 
 public Vector<Probe> queryEditingSitesForChr(String chrome){
		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(dbSnpTable, " chrome, pos ,alt "," chrome="+chrome+" ");
		try {
			while(rs.next()){
				Probe p=new Probe(rs.getString(1),rs.getInt(2),rs.getString(3).toCharArray()[0]);
				probeVector.add(p);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return probeVector;
	}
}
