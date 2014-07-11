package com.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class SamInput {
    Dbcon db = new Dbcon();
    private String[] sql = new String[3];
    private File file;
    private FileInputStream inputStream;
    private String line;
    private String[] temp = new String[21];
    private StringBuffer s1 = new StringBuffer();

    public SamInput(File file) {
        this.file = file;
    }

    public boolean samtable() {
        BufferedReader rin = null;
        try {
            db.dbcon();
            sql[0] = "drop table if exists sam";
            db.result = db.stmt.executeUpdate(sql[0]);
            sql[1] = "create table sam(qname varchar(30),flag int,rname varchar(11),pos int,mapq int,cigar varchar(30),rnext varchar(15),pnext bigint,tlen bigint,seq varchar(80),qual varchar(80))";
            db.result = db.stmt.executeUpdate(sql[1]);
            db.con.commit();
            if (db.result != -1) {
                System.out.println("������ݱ�ɹ�");
            }
            inputStream = new FileInputStream(file);
            rin = new BufferedReader(new InputStreamReader(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            while ((line = rin.readLine()) != null) {
                if (line.startsWith("@"))
                    continue;
                line = line.replace("'", "\\'");
                s1.append("'" + line.split("\\t")[0] + "'");
                for (int i = 1; i < 11; i++) {
                    temp[i] = line.split("\\t")[i];
                    s1.append("," + "'" + temp[i] + "'");
                }
                // System.out.println(s1);
                sql[2] = "insert into sam(qname,flag,rname,pos,mapq,cigar,rnext,pnext,tlen,seq,qual) values("
                        + s1 + ")";
                db.result = db.stmt.executeUpdate(sql[2]);
                db.con.commit();
                s1.delete(0, s1.length());
            }
            System.out.println(s1);
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } finally {
        }
    }
}
