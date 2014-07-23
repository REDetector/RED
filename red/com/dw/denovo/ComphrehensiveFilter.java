package com.dw.denovo;

/**
 * Comphrehensive phase
 * we focus on base in exon
 * we discard base in the rear or front of the sequence
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

public class ComphrehensiveFilter {
	private DatabaseManager databaseManager;

	// FileInputStream inputStream;
	private String comIn = null;
	private String ComphrehensiveTable = null;
	private String referenceComphrehensive = null;
	private String refTable = null;

	private int count = 0;
	private String chr = null;
	private String ps = null;
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public ComphrehensiveFilter(DatabaseManager databaseManager, String comIn,
			String ComphrehensiveTable, String referenceComphrehensive,
			String refTable) {
		this.databaseManager = databaseManager;
		this.comIn = comIn;
		this.ComphrehensiveTable = ComphrehensiveTable;
		this.referenceComphrehensive = referenceComphrehensive;
		this.refTable = refTable;
	}

	public boolean establishCom() {
		System.out.println("escom start" + " " + df.format(new Date()));

		databaseManager.deleteTable(ComphrehensiveTable);
		databaseManager.createTable(ComphrehensiveTable, "(chrome varchar(15),"
				+ Utilities.getInstance().getS2() + "," + "index(chrome,pos))");

		System.out.println("escom end" + " " + df.format(new Date()));
		return true;
	}

	public boolean establishRefCom() {
		databaseManager
				.createTable(
						referenceComphrehensive,
						"(chrome varchar(15),ref varchar(30),type varchar(9),begin int,end int,unuse1 float(8,6),unuse2 varchar(5),unuse3 varchar(5),info varchar(100),index(chrome,type))");
		ResultSet rs = databaseManager.query(referenceComphrehensive,
				"count(*)", "1 limit 0,100");
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
	
	public boolean loadcom() {
		System.out.println("loadcom start" + " " + df.format(new Date()));

		if(establishRefCom()){
		databaseManager.executeSQL("load data local infile '" + comIn
				+ "' into table " + referenceComphrehensive
				+ " fields terminated by '\t' lines terminated by '\n'");
		}

		System.out.println("loadcom end" + " " + df.format(new Date()));
		
		return true;

	}

	public boolean comphrehensiveF(int edge) {
		try {
			System.out.println("comf start" + " " + df.format(new Date()));
																			
			ResultSet rs = databaseManager.query(refTable, "chrome,pos", "1");

			List<String> coordinate = new ArrayList<String>();
			databaseManager.setAutoCommit(false);

			while (rs.next()) {
				coordinate.add(rs.getString(1));
				coordinate.add(rs.getString(2));
			}
			for (int i = 0, len = coordinate.size(); i < len; i++) {
				switch (i % 2) {
				case 0:
					chr = coordinate.get(i);
					break;
				case 1:
					ps = coordinate.get(i);
					rs = databaseManager.query(referenceComphrehensive, "type",
							"( ((begin<" + ps + "+" + edge + " " + "and begin>"
									+ ps + "-" + edge + ") or " + "(end<" + ps
									+ "+" + edge + " and end>" + ps + "-"
									+ edge + ")) "
									+ "and type='CDS' and chrome='" + chr
									+ "')");
					if (!rs.next()) {
						databaseManager.executeSQL("insert into "
								+ ComphrehensiveTable + "  select * from "
								+ refTable + " where chrome='" + chr
								+ "' and pos=" + ps + "");
						count++;
						if (count % 10000 == 0)
							databaseManager.commit();
					}
					break;
				}
			}
			databaseManager.commit();
			databaseManager.setAutoCommit(true);

			System.out.println("comf end" + " " + df.format(new Date()));
																			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}
	
	public void distinctTable() {
		 System.out.println("post start" + " " + df.format(new Date()));
		
		 databaseManager.executeSQL("create temporary table newtable select distinct * from "
		 + ComphrehensiveTable);
		 databaseManager.executeSQL("truncate table " + ComphrehensiveTable);
		 databaseManager.executeSQL("insert into " + ComphrehensiveTable +
		 " select * from  newtable");
		 databaseManager.deleteTable("newTable");
		
		 System.out.println("post end" + " " + df.format(new Date()));
		 }

	public Vector<Probe> queryAllEditingSites(){
		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(ComphrehensiveTable, " chrome, pos,alt "," 1 ");
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
		ResultSet rs=databaseManager.query(ComphrehensiveTable, " chrome, pos ,alt "," chrome="+chrome+" and pos='"+pos+"' ");
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
		ResultSet rs=databaseManager.query(ComphrehensiveTable, " chrome, pos ,alt "," chrome="+chrome+" ");
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
