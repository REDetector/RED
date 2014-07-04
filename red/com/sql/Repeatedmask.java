package com.sql;

import java.io.*;
import java.sql.SQLException;

public class Repeatedmask {
    Dbcon db = new Dbcon();
    // File file;
    File file = new File("H:/hg19.fa.out");
    FileInputStream inputStream;
    String line = null;
    StringBuffer s1 = new StringBuffer();
    int count = 3;
    int index = 0;
    BasicFilter bf = new BasicFilter();
    StringBuffer s2 = new StringBuffer();
    // insert ʱ��ʹ�õ�����
    StringBuffer s3 = new StringBuffer();
    int timer = 0;
    int minute = 0;
    private String[] sql = new String[3];

    public void loadrepeat() {
        try {
            db.dbcon();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            sql[0] = "drop table if exists loadrepeat";
            sql[1] = "create table loadrepeat(chrome varchar(25),begin int,end int,type varchar(40))";
            // sql[2]="load data local infile 'H:/hg19.fa.out' into table loadrepeat fields terminated by '\t' lines terminated by '\n' ignore 3 lines ";
            db.result = db.stmt.executeUpdate(sql[0]);
            db.result = db.stmt.executeUpdate(sql[1]);
            db.con.commit();
            BufferedReader rin = new BufferedReader(new InputStreamReader(
                    inputStream));
            while ((line = rin.readLine()) != null) {
                line = line.replaceAll("['   ']+", "\t");
                // ��fa.out��ͷע��ȥ��
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
                // System.out.println(line.split("\t")[index+5]);
                sql[0] = "insert into loadrepeat(chrome,begin,end,type) values('"
                        + line.split("\t")[index + 5]
                        + "','"
                        + line.split("\t")[index + 6]
                        + "','"
                        + line.split("\t")[index + 7]
                        + "','"
                        + line.split("\t")[index + 11] + "')";
                db.result = db.stmt.executeUpdate(sql[0]);
                db.con.commit();
                timer++;
                if (timer == 20000) {
                    minute++;
                    System.out.println(minute * 2 + "W");
                    timer = 0;
                }
                index = 0;
            }
            System.out.println("loadrepeat good");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void rfilter() {
        try {
            db.dbcon();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        bf.specific();
        bf.bfilter();
        try {
            s2 = bf.s2;
            s3 = bf.s3;
            System.out.println(bf.s2);
            sql[0] = "drop table if exists repeattemp";
            sql[1] = "create table repeattemp(chrome text," + s2 + ")";
            sql[2] = "insert into repeattemp("
                    + s3
                    + ") select * from basictemp where chrome=(select chrome from loadrepeat) && pos>(select begin from loadrepeat) &&pos<(select end from loadrepeat)";
            db.result = db.stmt.executeUpdate(sql[0]);
            db.result = db.stmt.executeUpdate(sql[1]);
            db.result = db.stmt.executeUpdate(sql[2]);
            // db.con.commit();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
