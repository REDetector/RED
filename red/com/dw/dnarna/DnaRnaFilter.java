package com.dw.dnarna;

/**
 * Detect SNP in DNA level
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;
import com.xl.datatypes.probes.Probe;

public class DnaRnaFilter {
	private DatabaseManager databaseManager;

	private String DnaRnaTable = null;
	private String refTable = null;
	private String dnaVcf = null;
	private String chr = null;
	private String ps = null;
	private String chrom = null;
	private int count = 0;
	// 设置日期格式
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public DnaRnaFilter(DatabaseManager databaseManager, String dnaVcf,
			String DnaRnaTable, String refTable) {
		this.databaseManager = databaseManager;
		this.dnaVcf = dnaVcf;
		this.DnaRnaTable = DnaRnaTable;
		this.refTable = refTable;
	}

	public boolean createDnaRnaTable() {
		System.out.println("esdr start" + " " + df.format(new Date()));// new
																		// Date()为获取当前系统时间

		databaseManager.deleteTable(DnaRnaTable);
		databaseManager.createTable(DnaRnaTable, "(chrome varchar(15),"
				+ Utilities.getInstance().getS2() + ")");

		System.out.println("esdr end" + " " + df.format(new Date()));// new
																		// Date()为获取当前系统时间
		return true;
	}

	public void dnarnaFilter() {
		try {
			System.out.println("df start" + " " + df.format(new Date()));// new
																			// Date()为获取当前系统时间

			ResultSet rs = databaseManager.query(dnaVcf, "chrome",
					"1 limit 0,1");
			List<String> coordinate = new ArrayList<String>();
			databaseManager.setAutoCommit(false);

			// whether it is
			boolean bool = false;
			if (rs.next() && rs.getString(1).length() < 3) {
				bool = true;
			}

			rs = databaseManager.query(refTable, "chrome,pos", "1");
			while (rs.next()) {
				if (bool) {
					chrom = rs.getString(1).replace("chr", "");
					coordinate.add(chrom);
					coordinate.add(rs.getString(2));
				} else {
					coordinate.add(rs.getString(1));
					coordinate.add(rs.getString(2));
				}
			}

			for (int i = 0, len = coordinate.size(); i < len; i++) {
				if (i % 2 == 0) {
					chr = coordinate.get(i);
				} else {
					ps = coordinate.get(i);
					// The first six base will be filtered out
					rs = databaseManager.query(dnaVcf, "GT", "chrome='" + chr
							+ "' and pos=" + ps + "");
					while (rs.next()) {
						if (bool) {
							chr = "chr" + chr;
							databaseManager.executeSQL("insert into "
									+ DnaRnaTable + " select * from "
									+ refTable + " where chrome='" + chr
									+ "' and pos=" + ps + "");
							count++;
							if (count % 10000 == 0)
								databaseManager.commit();
						} else {
							databaseManager.executeSQL("insert into "
									+ DnaRnaTable + " select * from "
									+ refTable + " where chrome='" + chr
									+ "' and pos=" + ps + "");
							count++;
							if (count % 10000 == 0)
								databaseManager.commit();
						}
					}
					databaseManager.commit();
				}
			}
			databaseManager.setAutoCommit(true);

			System.out.println("df end" + " " + df.format(new Date()));// new
																		// Date()为获取当前系统时间
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void distinctTable() {
		 System.out.println("post start" + " " + df.format(new Date()));
		
		 databaseManager.executeSQL("create temporary table newtable select distinct * from "
		 + DnaRnaTable);
		 databaseManager.executeSQL("truncate table " + DnaRnaTable);
		 databaseManager.executeSQL("insert into " + DnaRnaTable +
		 " select * from  newtable");
		 databaseManager.deleteTable("newTable");
		
		 System.out.println("post end" + " " + df.format(new Date()));
		 }
	
	public Vector<Probe> queryAllEditingSites(){
		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(DnaRnaTable, " chrome, pos,alt "," 1 ");
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
		ResultSet rs=databaseManager.query(DnaRnaTable, " chrome, pos ,alt "," chrome="+chrome+" and pos='"+pos+"' ");
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
		ResultSet rs=databaseManager.query(DnaRnaTable, " chrome, pos ,alt "," chrome="+chrome+" ");
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
