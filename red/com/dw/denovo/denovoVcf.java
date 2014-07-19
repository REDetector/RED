package com.dw.denovo;

/**
 * import vcf file
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;

public class denovoVcf {
    private DatabaseManager databaseManager;

    private String rnaFile=null;
    private String rnaVcf=null;
	
	FileInputStream inputStream;
	//SQL to be executed
	//basic unit to process
	private String line = null;
	//data of each column
	private String[] col = new String[40];
	private String[] temp = new String[10];
	private StringBuffer s1 = new StringBuffer();
	// insert时使锟矫碉拷锟斤拷锟�?	StringBuffer s1 = new StringBuffer();
	//count for each function
	private int count_r=1;
	private int depth = -1;
	// 锟斤拷锟斤拷锟斤拷锟节革拷式
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// establish table structure for following tables
	 public denovoVcf(DatabaseManager databaseManager,String rnaFile, String rnaVcf) {
	        this.databaseManager = databaseManager;
	        this.rnaFile=rnaFile;
	        this.rnaVcf=rnaVcf;
	    }

	
//	public int getdepth() {
//		try {
//			inputStream = new FileInputStream(rnaFile);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		BufferedReader rin = new BufferedReader(new InputStreamReader(
//				inputStream));
//		try {
//			while ((line = rin.readLine()) != null) {
//				if (line.startsWith("##"))
//					continue;
//				if (line.startsWith("#"))
//					continue;
//				// value in each line
//				for (int k = 0; k < line.split("\\t")[8].split(":").length; k++) {
//					if (line.split("\\t")[8].split(":")[k].equals("DP")) {
//						depth = k;
//					}
//				}
//				break;
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(depth);
//		return depth;
//	}


	// table for RnaVcf X.length-9=time for circulation
	public void RnaVcf(int num) {
		System.out.println("rnavcf start"+" "+df.format(new Date()));// new Date()为锟斤拷取锟斤拷前系统时锟斤�?
		try {

			databaseManager.setAutoCommit(false);
			//timer for transaction
			int ts_count=0;
			try {
				inputStream = new FileInputStream(rnaFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader rin = new BufferedReader(new InputStreamReader(
					inputStream));
			while ((line = rin.readLine()) != null) {
				if (line.startsWith("##"))
					continue;
				if (line.startsWith("#")) {
					continue;
				}
				for (int i = 0; i < line.split("\\t").length; i++) {
					col[i] = line.split("\\t")[i];
				}
				int length=col[8].split(":").length;
				if(col[num].split(":").length!=length){
					continue;
				}
				if(count_r>0){		
					for (int i = 0; i < length; i++) {
						if (col[8].split(":")[i].equals("DP")) {
							depth = i;
						}
					}
					databaseManager.deleteTable(rnaVcf);
					databaseManager.commit();
					databaseManager.createTable(rnaVcf, "(chrome varchar(15)," +Utilities.getInstance().getS2()+ ",index(chrome,pos))");
					databaseManager.commit();
					count_r--;
				}
				// data for import
				// '.' stands for undetected, so we discard it
				if ((depth>-1)&&col[num].split(":")[depth].equals("."))
					continue;
				s1.append("'" + col[0] + "'");
				for (int i = 1; i < 8; i++)
					s1.append("," + "'" + col[i] + "'");
				for (int i = 0; i < col[num].split(":").length; i++) {
					temp[i] = col[num].split(":")[i].replace(",", ";");
					// System.out.println(temp[i]);
					s1.append("," + "'" + temp[i] + "'");
				}
				// 锟斤拷菘锟斤拷锟捷诧拷锟诫，每锟叫诧拷锟斤拷
				databaseManager.executeSQL("insert into "+rnaVcf+"(" + Utilities.getInstance().getS3() + ") values(" + s1 + ")");
				ts_count++;
				if(ts_count%20000==0)
				databaseManager.commit();
				//clear insert data
				s1.delete(0, s1.length());
			}
			databaseManager.commit();
			databaseManager.setAutoCommit(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		System.out.println("rnavcf end"+" "+df.format(new Date()));// new Date()为锟斤拷取锟斤拷前系统时锟斤�?
	}
}
