package com.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Dbcon {
	//数据库驱动
	 String url = "jdbc:mysql://10.108.85.251:3306/input" ;    
     String username = "root" ;   
     String password = "root" ;
     Connection con=null;
     Statement stmt = null;
     int result=0;
     ResultSet rs=null;
	public void dbcon() throws SQLException{
		try{
		    //加载MySql的驱动类   
		    Class.forName("com.mysql.jdbc.Driver") ;   				
			con =DriverManager.getConnection(url,username,password) ;
			con.setAutoCommit(false);
			stmt = con.createStatement();			
		} catch(SQLException se){   
		      System.out.println("数据库连接失败！");   
		      se.printStackTrace() ;   
		      }catch(ClassNotFoundException e){   
			    System.out.println("找不到驱动程序类 ，加载驱动失败！");   
			    e.printStackTrace() ;   
			    } 
		}
	public void dbclose(){
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
