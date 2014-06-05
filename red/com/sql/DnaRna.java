package com.sql;

public class DnaRna {
	private String[] sql=new String[3];
	BasicFilter bf=new BasicFilter();
	Dbcon db=new Dbcon();
	public void drfilter(){
		sql[0]="select * from sam where (select * from specifictemp where ";
	}
}
