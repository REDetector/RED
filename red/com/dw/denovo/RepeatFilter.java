package com.dw.denovo;

/**
 * we will filter out base in repeated area except for SINE/alu
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class RepeatFilter {
	private DatabaseManager databaseManager;

	private String repeatIn = null;
	private String repeatTable = null;
	private String referenceRepeat = null;
	private String refTable = null;
	FileInputStream inputStream;
	private String line = null;
	private int count = 3;
	private int index = 0;
	private String chr = null;
	private String ps = null;

	// �������ڸ�ʽ
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public RepeatFilter(DatabaseManager databaseManager, String repeatIn,
			String repeatTable, String referenceRepeat, String refTable) {
		this.databaseManager = databaseManager;
		this.repeatIn = repeatIn;
		this.repeatTable = repeatTable;
		this.referenceRepeat = referenceRepeat;
		this.refTable = refTable;
	}
	
	public boolean establishRefRepeat() {
		databaseManager
				.createRefTable(referenceRepeat,
						"(chrome varchar(25),begin int,end int,type varchar(40),index(chrome))");
		ResultSet rs = databaseManager.query(referenceRepeat, "count(*)",
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
	
	public void loadrepeat() {
		try {
			System.out
					.println("loadrepeat start" + " " + df.format(new Date()));// new
			if(establishRefRepeat())
			{																	// Date()Ϊ��ȡ��ǰϵͳʱ��
			int ts_count = 0;
			try {
				inputStream = new FileInputStream(repeatIn);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			databaseManager.setAutoCommit(false);
			BufferedReader rin = new BufferedReader(new InputStreamReader(
					inputStream));
			while ((line = rin.readLine()) != null) {
				// clear head of fa.out
				line = line.replaceAll("['   ']+", "\t");
				if (count > 0) {
					count--;
					continue;
				}
				if (!line.startsWith("\t"))
					index--;
				if (line.split("\t")[index + 5].length() > 6) {
					index = 0;
					continue;
				}
				databaseManager.executeSQL("insert into " + referenceRepeat
						+ "(chrome,begin,end,type) values('"
						+ line.split("\t")[index + 5] + "','"
						+ line.split("\t")[index + 6] + "','"
						+ line.split("\t")[index + 7] + "','"
						+ line.split("\t")[index + 11] + "')");
				ts_count++;
				if (ts_count % 30000 == 0)
					databaseManager.commit();
				index = 0;
			}
			databaseManager.commit();
			databaseManager.setAutoCommit(true);
			}
			System.out.println("loadrepeat end" + " " + df.format(new Date()));// new
																				// Date()Ϊ��ȡ��ǰϵͳʱ��
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void establishrepeat() {
		System.out.println("esrepeat start" + " " + df.format(new Date()));// new
																			// Date()Ϊ��ȡ��ǰϵͳʱ��

		databaseManager.deleteTable(repeatTable);
		databaseManager.createTable(repeatTable, "(chrome varchar(15),"
				+ Utilities.getInstance().getS2() + "," + "index(chrome,pos))");

		databaseManager.deleteTable("alutemp");
		databaseManager.createTable("alutemp", "(chrome varchar(15),"
				+ Utilities.getInstance().getS2() + "," + "index(chrome,pos))");

		System.out.println("esrepeat end" + " " + df.format(new Date()));
	}

	public void repeatFilter() {
		System.out.println("rfliter start" + " " + df.format(new Date()));

		databaseManager.executeSQL("insert into " + repeatTable
				+ " select * from " + refTable
				+ " where not exists (select * FROM " + referenceRepeat
				+ " where (" + refTable + ".chrome=" + referenceRepeat
				+ ".chrome and " + refTable + ".pos>" + referenceRepeat
				+ ".begin and " + refTable + ".pos<" + referenceRepeat
				+ ".end)) ");

		databaseManager.executeSQL("insert into alutemp select * from "
				+ refTable + " where exists (select * FROM " + referenceRepeat
				+ " where (" + refTable + ".chrome=" + referenceRepeat
				+ ".chrome and " + refTable + ".pos>" + referenceRepeat
				+ ".begin and " + refTable + ".pos<" + referenceRepeat
				+ ".end and " + referenceRepeat + ".type='SINE/Alu')) ");

		databaseManager.executeSQL("insert into " + repeatTable
				+ " select * from alutemp");

		System.out.println("rfilter end" + " " + df.format(new Date()));
	}

	public void rfilter() {
		try {
			System.out.println("rfliter start" + " " + df.format(new Date()));// new
																				// Date()Ϊ��ȡ��ǰϵͳʱ��
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
//					System.out.println(chr+" "+ps);
					rs = databaseManager.query(referenceRepeat, " type ",
							"(chrome='" + chr + "' and begin<" + ps
									+ " and end>" + ps + ")");
					if (!rs.next()) {
						databaseManager.executeSQL("insert into " + repeatTable
								+ "  select * from " + refTable
								+ " where chrome='" + chr + "' and pos=" + ps
								+ "");
						count++;
						
						if (count % 10000 == 0) {
							databaseManager.commit();
						}
					}
					// SINEalu is also what we need
					else if (rs.next() && rs.getString(1) == "SINE/Alu") {
						databaseManager.executeSQL("insert into " + repeatTable
								+ "  select * from " + refTable
								+ " where chrome='" + chr + "' and pos=" + ps
								+ "");
						count++;

						if (count % 10000 == 0) {
							databaseManager.commit();
						}
					}
					break;
				}
			}

			databaseManager.commit();
			databaseManager.setAutoCommit(true);

			System.out.println("rfilter end" + " " + df.format(new Date()));// new
																			// Date()Ϊ��ȡ��ǰϵͳʱ��
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void distinctTable() {
		 System.out.println("post start" + " " + df.format(new Date()));
		
		 databaseManager.executeSQL("create temporary table newtable select distinct * from "
		 + repeatTable);
		 databaseManager.executeSQL("truncate table " + repeatTable);
		 databaseManager.executeSQL("insert into " + repeatTable +
		 " select * from  newtable");
		 databaseManager.deleteTable("newTable");
		
		 System.out.println("post end" + " " + df.format(new Date()));
		 }
	
	public Vector<Probe> queryqueryAllEditingSitesEditingSite(){
		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(repeatTable, " chrome, pos,alt "," 1 ");
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
		ResultSet rs=databaseManager.query(repeatTable, " chrome, pos ,alt "," chrome="+chrome+" and pos='"+pos+"' ");
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
		ResultSet rs=databaseManager.query(repeatTable, " chrome, pos ,alt "," chrome="+chrome+" ");
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
