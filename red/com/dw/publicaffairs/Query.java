package com.dw.publicaffairs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.xl.datatypes.probes.Probe;

public class Query {
	public Vector<Probe> queryAllEditingSites( String tableName){
		DatabaseManager databaseManager=DatabaseManager.getInstance();
		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(tableName, " chrome, pos,alt "," 1 ");
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
	
	public Probe queryEditingSite(String tableName,String chrome,int pos){
		DatabaseManager databaseManager=DatabaseManager.getInstance();

		ResultSet rs=databaseManager.query(tableName, " chrome, pos ,alt "," chrome="+chrome+" and pos='"+pos+"' ");
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
 
 public Vector<Probe> queryEditingSitesForChr(String tableName,String chrome){
		DatabaseManager databaseManager=DatabaseManager.getInstance();

		Vector<Probe> probeVector= new Vector<>();
		ResultSet rs=databaseManager.query(tableName, " chrome, pos ,alt "," chrome="+chrome+" ");
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
