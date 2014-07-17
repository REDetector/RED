package com.test;

/**
 * we will filter out base which already be recognized
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Snpf {
	private String[] sql = new String[3];
	VcfInput vi = new VcfInput("D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.DNA.chr8.snvs.vcf","D:/TDDOWNLOAD/data/BJ22N_DNA_RNA/BJ22.RNA.chr8.snvs.vcf");
	Dbcon db = new Dbcon();
	String line = null;
	String[] col = new String[40];
	String[] temp = new String[10];
	// insertʱʹ�õ�����
	StringBuffer s1 = new StringBuffer();
	// create tableʱʹ�õ��ַ���
	StringBuffer s2 = new StringBuffer();
	// insert ʱ��ʹ�õ�����
	StringBuffer s3 = new StringBuffer();
	String chr = null;
	String ps =null;
	int count = 0;
	String snpIn=null;
//	File file = new File("D:/TDDOWNLOAD/data/dbsnp_138.hg19.vcf");
	// �������ڸ�ʽ
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
	public Snpf(String snpIn){
		this.snpIn=snpIn;
	}
	public boolean essnp() {
		try {
			db.usedb();

			sql[0] = "drop table if exists Snptemp";
			sql[1] = "create table Snptemp(chrome varchar(30)," +Utilities.getInstance().getS2() + ")";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.result = db.stmt.executeUpdate(sql[1]);
			db.con.commit();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean dbSnpinput() {
		try {
			System.out.println("dbsnp start"+" "+df.format(new Date()));// new Date()Ϊ��ȡ��ǰϵͳʱ��
			db.usedb();
			sql[0] = "drop table if exists dbSnpVcf";
			db.result = db.stmt.executeUpdate(sql[0]);
			sql[1] = "create table dbSnpVcf(chrome varchar(15),pos varchar(30),index(chrome,pos))";
			db.result = db.stmt.executeUpdate(sql[1]);
			db.con.commit();
			FileInputStream inputStream = new FileInputStream(snpIn);
			BufferedReader rin = new BufferedReader(new InputStreamReader(
					inputStream));
			while ((line = rin.readLine()) != null) {
				if (line.startsWith("#")) {
					count++;
					continue;
				} else
					break;
			}
//			System.out.println(count);
			sql[2] = "load data local infile '"+snpIn+"' into table dbSnpVcf fields terminated by '\t' lines terminated by '\n' IGNORE "+ count + " LINES";
			// sql[2]="LOAD DATA INFILE '/tmp/test.txt'  INTO TABLE test IGNORE count LINES;/";
			db.result = db.stmt.executeUpdate(sql[2]);
			db.con.commit();
			System.out.println("dbsnp end"+" "+df.format(new Date()));// new Date()Ϊ��ȡ��ǰϵͳʱ��
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean dbSnpf() {
		try {
			db.usedb();
			System.out.println("dbsnpf start"+" "+df.format(new Date()));// new Date()Ϊ��ȡ��ǰϵͳʱ��
//			sql[0] = "select chrome,pos from dnarnatemp";
			sql[0] = "select chrome,pos from DnaRnatemp";
			db.rs = db.stmt.executeQuery(sql[0]);
			db.con.commit();
			// sql[2]="insert into repeattemp("+s3+") select * from basictemp where chrome=(select chrome from loadrepeat) && pos>(select begin from loadrepeat) &&pos<(select end from loadrepeat)";
			while (db.rs.next()) {
				s2.append(db.rs.getString(1) + "\t");
				s3.append(db.rs.getString(2) + "\t");
			}
//			System.out.println(s2.toString().split("\t").length);
			int j=s2.toString().split("\t").length;
			for (int i = 0; i < j; i++) {
				chr=s2.toString().split("\t")[i];
				ps=s3.toString().split("\t")[i];
//				System.out.println(df.format(new Date()) + " " + "start" + " "+ i);// new Date()Ϊ��ȡ��ǰϵͳʱ��
				// It needs much to improve, multiple index may be a good idea.
				sql[0] = "select chrome from dbSnpVcf where (pos="+ ps+ " and chrome='"+ chr + "')";
				db.rs = db.stmt.executeQuery(sql[0]);
				db.con.commit();
				// System.out.println(s2.toString().split("\t")[i]);
//				System.out.println(df.format(new Date()) + " " + "middle" + " "+ i);// new Date()Ϊ��ȡ��ǰϵͳʱ��
				if (!db.rs.next()) {
					// System.out.println(s2.toString().split("\t")[i]);
					sql[0] = "insert into snptemp select * from dnarnatemp where chrome='"+ chr+ "' and pos="+ ps + "";
//					sql[0] = "insert into snptemp select * from Comphrehensivetemp where chrome='"+ chr+ "' and pos="+ ps + "";
					db.result = db.stmt.executeUpdate(sql[0]);
					db.con.commit();
//					System.out.println(df.format(new Date()) + " " + "end"+ " " + i);// new Date()Ϊ��ȡ��ǰϵͳʱ��
				}
			}
			//clear insert data
			s2.delete(0, s2.length());
			s3.delete(0, s3.length());
			System.out.println("dbsnpf end"+" "+df.format(new Date()));// new Date()Ϊ��ȡ��ǰϵͳʱ��
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}
	
	public void snpF(){
		System.out.println("snpf start"+" "+df.format(new Date()));// new Date()Ϊ��ȡ��ǰϵͳʱ��
		try {
			sql[0]="insert into snptemp select *from dnarnatemp where not exists (select *FROM dbSnpVcf where (dnarnatemp.chrome=dbSnpVcf.chrome and dnarnatemp.pos=dbSnpVcf.pos))";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.con.commit();
			System.out.println("snpf end"+" "+df.format(new Date()));// new Date()Ϊ��ȡ��ǰϵͳʱ��
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void post() throws SQLException {
		try {
			db.usedb();
			sql[0] = "create   temporary   table  newtable  select   distinct   *   from  snptemp";
			sql[1] = "truncate   table  snptemp";
			sql[2] = "insert   into   snptemp select   *   from  newtable";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.result = db.stmt.executeUpdate(sql[1]);
			db.result = db.stmt.executeUpdate(sql[2]);
			db.con.commit();
			sql[0] = "drop   table   newtable";
			db.result = db.stmt.executeUpdate(sql[0]);
			db.con.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
