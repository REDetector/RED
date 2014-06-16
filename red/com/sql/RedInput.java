package com.sql;

import java.io.*;
import java.sql.SQLException;

//import java.sql.*;


public class RedInput {
    Dbcon db = new Dbcon();
    //data redirectory;
//	File file;
    //String dir;
    File file = new File("D:/TDDOWNLOAD/HCC448N.filtered.snvs.vcf");
    //File file = new File("D:/TDDOWNLOAD/HCC448T.subset.vcf");
    //File file = new File("D:/TDDOWNLOAD/test.txt");
    FileInputStream inputStream;
    private String[] sql = new String[3];
    String line = null;
    String[] col = new String[10];
    String[] temp = new String[10];
    //insertʱʹ�õ����
    StringBuffer s1 = new StringBuffer();
    //create tableʱʹ�õ��ַ�
    StringBuffer s2 = new StringBuffer();
    //insert ʱ��ʹ�õ�����
    StringBuffer s3 = new StringBuffer();
    int count = 1;

    //	public  RedInput(String dir)
//	{
//		this.file=new File(dir);
//	}
//	public String directory()
//	{
//		dir="D:/TDDOWNLOAD/HCC448T.subset.vcf";
//		return dir;
//	}
    public void estable() {
        try {
            //��ʼ��
            db.dbcon();
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            BufferedReader rin = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = rin.readLine()) != null) {
                s1 = new StringBuffer();
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    s2.append(line.split("\\t")[1] + " " + "bigint");
                    s2.append("," + line.split("\\t")[2] + " " + "varchar(30)");
                    s2.append("," + line.split("\\t")[3] + " " + "varchar(3)");
                    s2.append("," + line.split("\\t")[4] + " " + "varchar(5)");
                    s2.append("," + line.split("\\t")[5] + " " + "float(8,2)");
                    s2.append("," + line.split("\\t")[6] + " " + "text");
                    s2.append("," + line.split("\\t")[7] + " " + "text");
                    s3.append("chrome");
                    for (int i = 1; i < 8; i++)
                        s3.append("," + line.split("\\t")[i]);
                    continue;
                }
                for (int i = 0; i < line.split("\\t").length; i++) {
                    col[i] = line.split("\\t")[i];
                }
                if (count > 0) {
                    for (int i = 0; i < col[8].split(":").length; i++) {
                        s2.append("," + col[8].split(":")[i] + " " + "text");
                        s3.append("," + col[8].split(":")[i]);
                    }
                    sql[0] = "drop table if exists vcf";
                    db.result = db.stmt.executeUpdate(sql[0]);
                    sql[1] = "create table vcf(chrome varchar(15)," + s2 + ")";
                    db.result = db.stmt.executeUpdate(sql[1]);
                    db.con.commit();
                    sql[2] = "create index cor on vcf(chrome,POS)";
                    db.result = db.stmt.executeUpdate(sql[2]);
                    if (db.result != -1) {
                        System.out.println("������ݱ�ɹ�");
                    }
                    count--;
                }
                s1.append("'" + col[0] + "'");
                for (int i = 1; i < 8; i++)
                    s1.append("," + "'" + col[i] + "'");
                for (int i = 0; i < col[9].split(":").length; i++) {
                    temp[i] = col[9].split(":")[i].replace(",", ";");
                    //System.out.println(temp[i]);
                    s1.append("," + "'" + temp[i] + "'");
                }
                //��ݿ���ݲ��룬ÿ�в���
                sql[2] = "insert into vcf(" + s3 + ") values(" + s1 + ")";
                db.result = db.stmt.executeUpdate(sql[2]);
//            	sql[1] = "select * from RED";
//              db.rs = db.stmt.executeQuery(sql[1]);
                db.con.commit();
//      	    while (db.rs.next()) {
//                   System.out.println(db.rs.getString(9)+ "\t" + db.rs.getString(11));// �����ص���int���Ϳ�����getInt()
//               }
            }
            System.out.println("good");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}

