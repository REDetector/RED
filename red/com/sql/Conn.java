package com.sql;

import java.sql.SQLException;

public class Conn {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Dbcon db=new Dbcon();
		try {
			db.dbcon();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new RedInput();	
//		RedInput ri=new RedInput("D:/TDDOWNLOAD/HCC448T.subset.vcf");
//		Basicf bf=new Basicf();
//		Saminput si=new Saminput("D:/TDDOWNLOAD/HCC448N.subset.sam");
//		Saminput si=new Saminput("D:/TDDOWNLOAD/HCC448N.recal.chr8.sam");
//		si.samtable();
//		bf.specific();
//		bf.bfilter();
		Repeatedmask rm=new Repeatedmask();
//		rm.loadrepeat();
		rm.rfilter();
		db.dbclose();
	}
	

}
