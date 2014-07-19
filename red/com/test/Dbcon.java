package com.test;

/**
 * Linked to target database
 */

import java.sql.*;

public class Dbcon {
    // ��ݿ���
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
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, username, password);
            con.setAutoCommit(false);
            stmt = con.createStatement();
        } catch (SQLException se) {
            System.out.println("��ݿ�����ʧ�ܣ�");
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("�Ҳ���������� ��������ʧ�ܣ�");
            e.printStackTrace();
        }
    }

    public void usebase() {
        try {
            sql[0] = "create database DnaRna";
            result = stmt.executeUpdate(sql[0]);
            con.commit();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean usedb() {
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
