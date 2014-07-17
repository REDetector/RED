package com.test;

/**
 *P_value based on alt and ref 
*/

	import java.io.BufferedReader;
	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileNotFoundException;
	import java.io.IOException;
	import java.io.InputStreamReader;
	import java.sql.SQLException;
	import java.text.DecimalFormat;
	import java.text.SimpleDateFormat;
	import java.util.ArrayList;
	import java.util.Date;

	import rcaller.Globals;
	import rcaller.RCaller;
import rcaller.RCode;

	public class PValue {
		Dbcon db = new Dbcon();
//		File file = new File("D:/TDDOWNLOAD/data/hg19.txt");
		String pIn=null;
		FileInputStream inputStream;
		private String[] sql = new String[3];
		String line = null;
		String[] col = new String[40];
		String[] temp = new String[10];
		// insert时使用的数据
		StringBuffer s1 = new StringBuffer();
		StringBuffer s2 = new StringBuffer();
		// create table时使用的字符串
		// insert 时候使用的列名
		StringBuffer s3 = new StringBuffer();
		int count=1;
		int num=0;
		private String chr;
		private String ps;
		int off =0;
		double known_alt=0;
		double known_ref=0;
		ArrayList<Double> fd_ref=new ArrayList<Double>();
		ArrayList<Double> fd_alt=new ArrayList<Double>();
//		private StringBuffer ref = new StringBuffer();
//		private StringBuffer alt = new StringBuffer();
//		float[] P_V;
		double fdr=0;
		double ref_n=0;
		double alt_n=0;
		double pvalue=0;
		// 设置日期格式
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		public PValue(String pIn){
			this.pIn=pIn;
		}
		public void loadHg19(){
		try {
			System.out.println("loadhg19 start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			db.usedb();
			// 初始化
			int count_ts=0;
			inputStream = new FileInputStream(pIn);
			BufferedReader rin = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = rin.readLine()) != null) {
				StringBuffer s1 = new StringBuffer();
				if (count>0) {
					s2.append(line.split("\\t")[0] + " " + "varchar(9)");
					s2.append("," + line.split("\\t")[1] + " " + "bigint");
					s2.append("," + line.split("\\t")[2] + " " + "varchar(5)");
					s2.append("," + line.split("\\t")[3] + " " + "varchar(5)");
					s2.append("," + line.split("\\t")[4] + " " + "varchar(5)");
					count--;
					s3.append(line.split("\\t")[0]);
					for (int i = 1; i < 5; i++)
						s3.append("," + line.split("\\t")[i]);
					sql[0] = "drop table if exists Hg19";
					db.result = db.stmt.executeUpdate(sql[0]);
				sql[1] = "create table Hg19("+s2+",index(chrom,coordinate))";
				db.result = db.stmt.executeUpdate(sql[1]);
				db.con.commit();
					continue;
				}
				for (int i = 0; i < 5; i++) {
					col[i] = line.split("\\t")[i];
					if(i==0&&col[i].length()<3)
						col[i]="chr"+col[i];
				}
				//A-I or G is what we focus on
				if(col[3].toCharArray()[0]=='A'&&(col[4].toCharArray()[0]=='G'||col[4].toCharArray()[0]=='I'))
				{
				s1.append("'" + col[0] + "'");
				for (int i = 1; i < 5; i++)
					s1.append("," + "'" + col[i] + "'");
				// 数据库数据插入，每行插入
				sql[2] = "insert into Hg19(" + s3 + ") values(" + s1 + ")";
				db.result = db.stmt.executeUpdate(sql[2]);
				count_ts++;
				if(count_ts%20000==0)
				db.con.commit();
				}
				db.con.commit();
		} 
			//clear insert data
			s1.delete(0, s1.length());
			s2.delete(0, s2.length());
			s3.delete(0, s3.length());
			System.out.println("loadhg19 end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
		
		public void level(String chr,String ps){
			try {
				db.usedb();
				ref_n=alt_n=0;
				sql[0] = "select AD from llrtemp where chrome='"+chr+"' and pos="+ps+"";
				db.rs = db.stmt.executeQuery(sql[0]);
				db.con.commit();
				
				while(db.rs.next()){
					String[] col=db.rs.getString(1).split(";");
					ref_n=Double.parseDouble(col[0]);
					alt_n=Double.parseDouble(col[1]);
				}
//				System.out.println(alt_n+" "+ref_n);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void Exp_num(){
			try {
				db.usedb();
				sql[0] = "select chrome,pos from llrtemp";
				db.rs = db.stmt.executeQuery(sql[0]);
				db.con.commit();
				while (db.rs.next()) {			
					s2.append(db.rs.getString(1) + "\t");
					s3.append(db.rs.getInt(2) + "\t");
				}
				
				//Find for alt,ref
				for(int j=0;j<s2.toString().split("\t").length;j++){
					chr=s2.toString().split("\t")[j];
					ps=s3.toString().split("\t")[j];
					sql[0] = "select strand from Hg19 where chrom='" + chr+ "' and coordinate='" + ps+ "' ";
					db.rs = db.stmt.executeQuery(sql[0]);
					db.con.commit();
					level(chr,ps);
//					System.out.println(chr+" "+ps);
					
//					int temp1=(int) (alt*100/(alt+ref));
//					if(alt*100/(alt+ref)-0.5>temp1)
//						temp1++;
//					found_alt[j]=temp1;
//					int temp2=(int) (ref*100/(alt+ref));
//					if(ref*100/(alt+ref)-0.5>temp2)
//						temp2++;
//					found_ref[j]=temp2;
					fd_alt.add(alt_n);
					fd_ref.add(ref_n);
				if(db.rs.next()) {
					known_alt+=alt_n;
					known_ref+=ref_n;
				}
				else{
					known_alt+=0;
					known_ref+=(alt_n+ref_n);	
				}	
				}
				known_alt/=s3.toString().split("\t").length;
				known_ref/=s3.toString().split("\t").length;
				int temp1=(int)known_alt;
				int temp2=(int)known_ref;
				
				if(known_alt-0.5>temp1)
					known_alt=temp1++;
				else
					known_alt=temp1;
				if(known_ref-0.5>temp2)
					known_ref=temp2++;
				else
					known_ref=temp2;
//				System.out.println(known_ref+" "+temp2+" "+known_alt);
//				//clear insert data
//				s2.delete(0, s2.length());
//				s3.delete(0, s3.length());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		public double calculate(double found_ref,double found_alt,double known_ref,double known_alt,String commandD){
			try {
				RCaller caller = new RCaller();
				RCode code = new RCode();
				Globals.detect_current_rscript();
				caller.setRscriptExecutable(commandD);
//				caller.setRscriptExecutable("D:/software/R-3.0.1/bin/Rscript.exe");
				

				double[][] data=new double[][]{{found_ref,found_alt},{known_ref,known_alt}};
//				double[][] data=new double[][]{{51.2,49.3},{100.,0}};
				code.addDoubleMatrix("mydata", data);
				code.addRCode("result <- fisher.test(mydata)");
				code.addRCode("mylist <- list(pval = result$p.value)");
				
				caller.setRCode(code);
				caller.runAndReturnResult("mylist");
				pvalue = caller.getParser().getAsDoubleArray("pval")[0];
				return pvalue;
	      } catch (Exception e) {
	          System.out.println(e.toString());
	      }
			return 0;
		}
		
		public void pvTable(){
			try {
				db.usedb();
				sql[0] = "drop table if exists pvtemp";
				sql[1] = "create table pvtemp(chrome varchar(15),pos int,ref smallint,alt smallint,level varchar(10),p_value double,fdr double)";
				db.result = db.stmt.executeUpdate(sql[0]);
				db.result = db.stmt.executeUpdate(sql[1]);
				db.con.commit();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		public void P_V(String commandD){
			System.out.println("P_V start"+" "+df.format(new Date()));// new Date()为获取当前系统时间
			pvTable();
			Exp_num();
			DecimalFormat dF = new DecimalFormat( "0.000 "); 
			try {
				int j=s3.toString().split("\t").length;
			for(int i=0;i<j;i++){
				chr=s2.toString().split("\t")[i];
				ps=s3.toString().split("\t")[i];
				ref_n=fd_ref.get(i);
				alt_n=fd_alt.get(i);
				double lev=alt_n/(alt_n+ref_n);
//				double lev=found_alt[i]/(found_alt[i]+found_ref[i]);
				if(((int)ref_n+(int)alt_n)<6)
					{
					continue;
					}
				calculate(ref_n,alt_n,known_ref,known_alt,commandD);
				sql[1] = "insert into pvtemp(chrome,pos,ref,alt,level,p_value) values('"+chr+"',"+ps+","+(int)ref_n+","+(int)alt_n+",'"+dF.format(lev)+"',"+pvalue+")";
				db.result = db.stmt.executeUpdate(sql[1]);
				db.con.commit();
//				System.out.println(chr+" "+ps+" "+(int)found_ref[i]+" "+(int)found_alt[i]+" "+dF.format(lev)+" "+pvalue);
				s1.append(pvalue+"\t");
			}
			 }catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			System.out.println("P_V end"+" "+df.format(new Date()));// new Date()为获取当前系统时间
		}
		
		public void fdr(String commandD){
			P_V(commandD);
			try {
				RCaller caller = new RCaller();
				RCode code = new RCode();
				Globals.detect_current_rscript();
				caller.setRscriptExecutable(commandD);
				ArrayList<Float> P_V=new ArrayList<Float>();
				for(int i=0;i<s1.toString().split("\t").length;i++)
				{
//					System.out.println(s1.toString().split("\t")[i]);
//				P_V[i]=Double.parseDouble(s1.toString().split("\t")[i]);
					P_V.add(Float.valueOf(s1.toString().split("\t")[i]));
				}
				Object[] objs=P_V.toArray();
				float[] floats = new float[objs.length];
				for(int i=0;i<objs.length;i++){
					floats[i] = (float) objs[i];
					}
				code.addFloatArray("parray", floats);
				code.addRCode("result<-p.adjust(parray,method='fdr',length(parray))");
//				code.addRCode("mylist <- list(qval = result$q.value)");
				caller.setRCode(code);
				caller.runAndReturnResult("result");

				for(int i=0;i<caller.getParser().getAsDoubleArray("result").length;i++){
				chr=s2.toString().split("\t")[i];
				ps=s3.toString().split("\t")[i];
				fdr = caller.getParser().getAsDoubleArray("result")[i];
				sql[1] = "update pvtemp set fdr="+fdr+" where chrome='"+chr+"' and pos="+ps+" ";
				db.result = db.stmt.executeUpdate(sql[1]);
				db.con.commit();
				}
	      } catch (Exception e) {
	          System.out.println(e.toString());
	      }
//			p.adjust(p,method="fdr",length(p));
		}
		
	}


	
	
	
