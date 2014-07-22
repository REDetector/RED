package com.dw.dnarna;

/**
 * LLR used for detecting editing sites
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

public class LlrFilter {
	private DatabaseManager databaseManager;

	private String dnaVcf = null;
	private String llrTable = null;
	private String refTable = null;
	private String chr;
	private String ps;
	private String chrom = null;
	private int count = 0;
	private int ref_n = 0;
	private int alt_n = 0;
	// 设置日期格式
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public LlrFilter(DatabaseManager databaseManager, String dnaVcf,
			String llrTable, String refTable) {
		this.databaseManager = databaseManager;
		this.dnaVcf = dnaVcf;
		this.llrTable = llrTable;
		this.refTable = refTable;
	}

	public boolean createLlrTable() {
		System.out.println("esllr start" + " " + df.format(new Date()));// new
																		// Date()为获取当前系统时间

		databaseManager.deleteTable(llrTable);
		databaseManager.createTable(llrTable, "(chrome text,"
				+ Utilities.getInstance().getS2() + ")");

		System.out.println("esllr end" + " " + df.format(new Date()));// new
																		// Date()为获取当前系统时间
		return true;
	}

	public void llrtemp() {
		try {
			System.out.println("llrtemp start" + " " + df.format(new Date()));// new
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

			rs = databaseManager.query(refTable, "chrome,pos,AD", "1");
			while (rs.next()) {
				if (bool) {
					chrom = rs.getString(1).replace("chr", "");
					coordinate.add(chrom);
					coordinate.add(rs.getString(2));
					coordinate.add(rs.getString(3));
				} else {
					coordinate.add(rs.getString(1));
					coordinate.add(rs.getString(2));
					coordinate.add(rs.getString(3));
				}
			}

			for (int i = 0, len = coordinate.size(); i < len; i++) {
				switch (i % 3) {
				case 0:
					chr = coordinate.get(i);
					break;
				case 1:
					ps = coordinate.get(i);
					break;
				case 2:
					String[] section = coordinate.get(i).split(";");
					ref_n = Integer.parseInt(section[0]);
					alt_n = Integer.parseInt(section[1]);

					double q = 0;
					rs = databaseManager.query(dnaVcf, "qual", "chrome='" + chr
							+ "' and pos=" + ps + "");
					while (rs.next()) {
						q = rs.getDouble(1);
					}
					if (alt_n + ref_n > 0) {

						double f_ml = 1.0 * ref_n / (ref_n + alt_n);
						double y = Math.pow(f_ml, ref_n)
								* Math.pow(1 - f_ml, alt_n);
						y = Math.log(y) / Math.log(10.0);
						double judge = 0.0;
						judge = y + q / 10.0;
						// System.out.println(judge);
						// System.out.println(ref_n + " " + alt_n + " " + y +
						// " "
						// + judge);
						if (judge >= 4) {
							if (bool) {
								chr = "chr" + chr;
								databaseManager.executeSQL("insert into "
										+ llrTable + " select * from "
										+ refTable + " where chrome='" + chr
										+ "' and pos=" + ps + "");
								count++;
								if (count % 10000 == 0) {
									databaseManager.commit();
								}
							} else {
								databaseManager.executeSQL("insert into "
										+ llrTable + " select * from "
										+ refTable + " where chrome='" + chr
										+ "' and pos=" + ps + "");
								count++;
								if (count % 10000 == 0) {
									databaseManager.commit();
								}
							}
						}
					}
					break;
				}
			}
			databaseManager.commit();
			databaseManager.setAutoCommit(true);
			System.out.println("llrtemp end" + " " + df.format(new Date()));// new
																			// Date()为获取当前系统时间
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	// public void post() {
	// try {
	// System.out.println("post start"+" "+df.format(new Date()));// new
	// Date()为获取当前系统时间
	// db.usedb();
	// sql[0] =
	// "create   temporary   table  newtable  select   distinct   *   from  llrtemp";
	// sql[1] = "truncate   table  llrtemp";
	// sql[2] = "insert   into   llrtemp select   *   from  newtable";
	// db.result = db.stmt.executeUpdate(sql[0]);
	// db.result = db.stmt.executeUpdate(sql[1]);
	// db.result = db.stmt.executeUpdate(sql[2]);
	// db.con.commit();
	// sql[0] = "drop   table newtable";
	// db.result = db.stmt.executeUpdate(sql[0]);
	// db.con.commit();
	// System.out.println("post end"+" "+df.format(new Date()));// new
	// Date()为获取当前系统时间
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	public void distinctTable() {
		 System.out.println("post start" + " " + df.format(new Date()));
		
		 databaseManager.executeSQL("create temporary table newtable select distinct * from "
		 + llrTable);
		 databaseManager.executeSQL("truncate table " + llrTable);
		 databaseManager.executeSQL("insert into " + llrTable +
		 " select * from  newtable");
		 databaseManager.deleteTable("newTable");
		
		 System.out.println("post end" + " " + df.format(new Date()));
		 }
	
	public Vector<Probe> queryAllEditingSites(){
		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(llrTable, " chrome, pos,alt "," 1 ");
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
		ResultSet rs=databaseManager.query(llrTable, " chrome, pos ,alt "," chrome="+chrome+" and pos='"+pos+"' ");
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
		ResultSet rs=databaseManager.query(llrTable, " chrome, pos ,alt "," chrome="+chrome+" ");
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

// while (db.rs.next()) {
// s1.append(db.rs.getString(1) + "\t");
// s2.append(db.rs.getInt(2) + "\t");
// }
// sql[0] = "select seq from sam where rname='" + s1.toString().split("\t")[0]+
// "' and pos='" + s2.toString().split("\t")[0] + "' limit 0,1";
// db.rs = db.stmt.executeQuery(sql[0]);
// db.con.commit();
// while (db.rs.next()) {
// i = db.rs.getString(1).length();
// }
// //Find for alt,ref
// for(int j=0;j<s1.toString().split("\t").length;j++){
// chr=s1.toString().split("\t")[j];
// ps=s2.toString().split("\t")[j];
// // System.out.println(chr+" "+ps);
// int q=0;
// int alt = 0;
// int ref = 0;
// sql[0] = "select pos,seq,qual from sam where rname='"+ chr + "' and (pos>='"
// + ps+ "' and pos<('" + ps + "'+'" + i + "'))";
// //
// db.rs = db.stmt.executeQuery(sql[0]);
// db.con.commit();
// while (db.rs.next()) {
// int off =0;
// // System.out.println(db.rs.getInt(1));
// off = db.rs.getInt(1) - Integer.parseInt(ps);
// if (db.rs.getString(2).charAt(off) == 'A') {
// ref++;
// refqual[ref] = db.rs.getString(3).charAt(off)-33;
// q+=refqual[ref];
// }
// if (db.rs.getString(2).charAt(off) == 'G') {
// alt++;
// altqual[alt] = db.rs.getString(3).charAt(off)-33;
// q+=altqual[alt];
// }
// // for(char n:altqual)
// }