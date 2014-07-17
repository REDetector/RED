package com.test;

/**
 * Linked to target database
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Dbcon {
	// 数据库驱动
//	String url = "jdbc:mysql://10.108.87.147:3306/";
//	String username = "wudi";
//	String password = "123456";
	String url = "jdbc:mysql://localhost/";
//	String username = "seq";
//	String password = "sequencing";
	String username = "root";
	String password = "root";
	Connection con = null;
	Statement stmt = null;
	int result = 0;
	ResultSet rs = null;
	private String[] sql = new String[3];

	public void dbcon() throws SQLException {
		try {
			// 加载MySql的驱动类
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, username, password);
			con.setAutoCommit(false);
			stmt = con.createStatement();
//			System.out.println("数据库连接成功！");
		} catch (SQLException se) {
			System.out.println("数据库连接失败！");
			se.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("找不到驱动程序类 ，加载驱动失败！");
			e.printStackTrace();
		}
	}
	public void usebase(){
		try {
			sql[0] = "create database DnaRna";
			result = stmt.executeUpdate(sql[0]);
			con.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean usedb(){
		try {
			dbcon();
			sql[0] = "use DnaRna";
			result = stmt.executeUpdate(sql[0]);
			con.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	public void dbclose() {
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
