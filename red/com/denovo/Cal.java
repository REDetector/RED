package com.denovo;

import rcaller.Globals;
import rcaller.RCaller;
import rcaller.RCode;

public class Cal {
	public void what() {
		try {
			// double[][] data = new double[][]{
			// {197.136, 124.32, 63.492, 59.052},
			// {124.32, 78.4, 40.04, 37.24},
			// {63.492, 40.04, 20.449, 19.019},
			// {59.052, 37.24, 19.019, 17.689}
			// };
			double[][] data = new double[][] { { 51, 49 }, { 100, 0 } };
			RCaller caller = new RCaller();
			Globals.detect_current_rscript();
			caller.setRscriptExecutable("D:/software/R-3.0.1/bin/Rscript.exe");
			RCode code = new RCode();

			code.addDoubleMatrix("mydata", data);
			// code.addRCode("result <- chisq.test(mydata)");
			code.addRCode("result <- fisher.test(mydata)");
			// code.addRCode("mylist <- list(pval = result$p.value, df=result$parameter)");
			code.addRCode("mylist <- list(pval = result$p.value)");

			caller.setRCode(code);
			caller.runAndReturnResult("mylist");

			double pvalue = caller.getParser().getAsDoubleArray("pval")[0];
			// double df = caller.getParser().getAsDoubleArray("df")[0];
			System.out.println("Pvalue is : " + pvalue);
			// System.out.println("Df is : "+df);
			
//			Utilities.getInstance().createCalTable();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

}