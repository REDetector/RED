/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dw.denovo;

import net.sf.snver.pileup.util.math.FisherExact;
import rcaller.RCaller;
import rcaller.RCode;

/**
 * Created by Administrator on 2014/11/13.
 */
public class FisherExactTestFilterTest {
    public static void main(String[] args) {
        RCaller caller = new RCaller();
//        Globals.detect_current_rscript();
//        caller.setRExecutable("C:\\R\\R-3.1.1\\bin\\R.exe");
        caller.setRscriptExecutable("C:\\R\\R-3.1.1\\bin\\Rscript.exe");
        RCode code = new RCode();
        double[][] data = new double[][]{{233, 21}, {32, 12}};
        code.addDoubleMatrix("mydata", data);
        code.addRCode("result <- fisher.test(mydata)");
        code.addRCode("mylist <- list(pval = result$p.value)");
        caller.setRCode(code);
        caller.runAndReturnResult("mylist");
        double pValue = caller.getParser().getAsDoubleArray("pval")[0];
        System.out.println(pValue + "\t");

        FisherExact fisherExact = new FisherExact(1000);
        System.out.println(fisherExact.getTwoTailedP(233, 21, 32, 12));

        double[] datas = new double[]{0.02, 0.2343, 0.0005, 0.006, 0.4327, 0.2238, 0.43};

        caller = new RCaller();
        caller.setRscriptExecutable("C:\\R\\R-3.1.1\\bin\\Rscript.exe");
        code.addDoubleArray("parray", datas);
        code.addRCode("result<-p.adjust(parray,method='fdr',length(parray))");
        caller.setRCode(code);
        caller.runAndReturnResult("result");
        double[] result = caller.getParser().getAsDoubleArray("result");
        System.out.println("Calling FDR from R:");
        for (double re : result) {
            System.out.print(re + "\t");
        }
    }

}
