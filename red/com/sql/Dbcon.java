package com.sql;

import java.sql.*;

public class Dbcon {
    //��ݿ���
    String url = "jdbc:mysql://10.108.85.251:3306/input";
    String username = "root";
    String password = "root";
    Connection con = null;
    Statement stmt = null;
    int result = 0;
    ResultSet rs = null;

    public void dbcon() throws SQLException {
        try {
            //����MySql������
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

    public void dbclose() {
        try {
            con.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
