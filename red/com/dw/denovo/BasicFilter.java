package com.dw.denovo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;

/**
 * Basic process for RNA-editing specific filter means we only focus on A-G
 * basic filter means we set threshold on quality and depth
 */
public class BasicFilter {
	private DatabaseManager databaseManager;
	private String chr = null;
	private String ps = null;
	private String rnaVcf = null;
	private String basicTable = null;
	private String specificTable = null;

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private int ref_n = 0;
	private int alt_n = 0;
	private int count = 0;

	public BasicFilter(DatabaseManager databaseManager, String rnaVcf,
			String specificTable, String basicTable) {
		this.databaseManager = databaseManager;
		this.rnaVcf = rnaVcf;
		this.basicTable = basicTable;
		this.specificTable = specificTable;
	}

	public void createSpecificTable() {
		databaseManager.deleteTable(specificTable);
		databaseManager.createTable(specificTable, "(chrome varchar(30),"
				+ Utilities.getInstance().getS2() + "," + "index(chrome,pos))");
	}

	public void specificf() {
		System.out.println("specific start" + " " + df.format(new Date()));

		databaseManager.insertClause("insert into " + specificTable + "("
				+ Utilities.getInstance().getS3() + ")  select * from " + ""
				+ rnaVcf + " where " + "REF='A' AND ALT='G'");

		System.out.println("specific end" + " " + df.format(new Date()));
	}

	// public void spePost() {
	// System.out.println("post start" + " " + df.format(new Date()));
	//
	// databaseManager.executeSQL("create temporary table newtable select distinct * from "
	// + specificTable);
	// databaseManager.executeSQL("truncate table " + specificTable);
	// databaseManager.executeSQL("insert into " + specificTable +
	// " select * from  newtable");
	// databaseManager.deleteTable("newTable");
	//
	// System.out.println("post end" + " " + df.format(new Date()));
	// }

	public void basicf(double quality, int depth) {
		try {
			System.out.println("bfilter start" + " " + df.format(new Date()));// new
																				// Date()Ϊ��ȡ��ǰϵͳʱ��

			// create table and insert data into it
			databaseManager.deleteTable(basicTable);
			databaseManager.createTable(basicTable, "(chrome varchar(30),"
					+ Utilities.getInstance().getS2() + ",index(chrome,pos))");
			ResultSet rs = databaseManager.query(specificTable,
					"chrome,pos,AD", "1");
			List<String> coordinate = new ArrayList<String>();
			databaseManager.setAutoCommit(false);

			while (rs.next()) {
				coordinate.add(rs.getString(1));
				coordinate.add(rs.getString(2));
				coordinate.add(rs.getString(3));
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
					if (ref_n + alt_n > depth) {
						databaseManager.executeSQL("insert into " + basicTable
								+ "( " + Utilities.getInstance().getS3()
								+ ") (select * from " + specificTable
								+ " where filter='PASS' and pos=" + ps
								+ " and qual >" + quality + " and chrome='"
								+ chr + "')");
						count++;
						if (count % 10000 == 0)
							databaseManager.commit();
					}
					break;
				}
				databaseManager.commit();
			}
			databaseManager.setAutoCommit(true);
			System.out.println("bfilter end" + " " + df.format(new Date()));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
