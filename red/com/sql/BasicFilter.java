package com.sql;

import java.sql.SQLException;

public class BasicFilter {
    Dbcon db = new Dbcon();
    RedInput ri = new RedInput();
    StringBuffer s2 = new StringBuffer();
    //insert ʱ��ʹ�õ�����
    StringBuffer s3 = new StringBuffer();
    //RedInput ri=new RedInput("D:/TDDOWNLOAD/HCC448T.subset.vcf");
    private String[] sql = new String[3];

    public void specific() {
        try {
            db.dbcon();
            ri.estable();
            sql[0] = "drop table if exists specifictemp";
            //System.out.println(ri.s3);
            sql[1] = "create table specifictemp(chrome text," + ri.s2 + ")";
            sql[2] = "insert into specifictemp(" + ri.s3 + ")  select * from vcf where REF='A' AND ALT='G'";
            db.result = db.stmt.executeUpdate(sql[0]);
            db.result = db.stmt.executeUpdate(sql[1]);
            db.result = db.stmt.executeUpdate(sql[2]);
            db.con.commit();
            System.out.println("better");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void bfilter() {
        try {
            //System.out.println(ri.s2);
            sql[0] = "drop table if exists basictemp";
            //System.out.println(ri.s3);
            s2 = ri.s2;
            s3 = ri.s3;
            sql[1] = "create table basictemp(chrome text," + s2 + ")";
            sql[2] = "insert into basictemp(" + s3 + ")  select * from specifictemp where QUAL>30 AND DP>100";
            db.result = db.stmt.executeUpdate(sql[0]);
            db.result = db.stmt.executeUpdate(sql[1]);
            db.result = db.stmt.executeUpdate(sql[2]);
            db.con.commit();
            System.out.println("good");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
