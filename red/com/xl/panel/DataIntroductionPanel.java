/**
 * Copyright 2011-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.xl.panel;

import com.xl.preferences.LocationPreferences;

import javax.swing.*;
import java.awt.*;


public class DataIntroductionPanel extends JPanel {

    public DataIntroductionPanel(String dataType) {
        setPreferredSize(new Dimension(300, 300));
        setLayout(new BorderLayout());
        JLabel label = new JLabel("No file selected", JLabel.CENTER);
        add(label, BorderLayout.CENTER);
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        if (LocationPreferences.R_SCRIPT_PATH.equals(dataType)) {
            sb.append("<p style=\"text-align:center\"><b>" + LocationPreferences.R_SCRIPT_PATH + "</b></p>");
            sb.append("<br>");
            sb.append("We need <b><i>R executable path</i></b> or <b><I>R script path</i></b> to run statistic method.");
            sb.append("<br>");
            sb.append("For <b>Windows</b>,");
            sb.append("<br>");
            sb.append("Default R executable path is  'X:/Program Files/R/R-3.x.x/bin/R.exe'");
            sb.append("<br>");
            sb.append("Default R script path is  'X:/Program Files/R/R-3.x.x/bin/RScript.exe'");
            sb.append("<br>");
            sb.append("For <b>Linux/MAC OSX</b>,");
            sb.append("<br>");
            sb.append("Default R executable path is  '/usr/bin/R'");
            sb.append("<br>");
            sb.append("Default R script path is  '/usr/bin/RScript'");
            sb.append("<br>");
            label.setText(sb.toString());
        } else if (LocationPreferences.RNA_VCF_FILE.equals(dataType)) {
            sb.append("<p style=\"text-align:center\"><b>" + LocationPreferences.RNA_VCF_FILE + "</b></p>");
            sb.append("<br>");
            sb.append("\tRNA vcf file is the main file to detect RNA editing sites.");
            sb.append("<br>");
            sb.append("\t We will use the RNA vcf file to do <I>denovo</I> or <I>non-denovo</I> sequencing.");
            sb.append("<br><br>");
            sb.append("<b>Example</b>: BJ22.RNA.chr8.snvs.vcf");
            sb.append("<br>");
            sb.append("<b>From</b>: GATK");
            label.setText(sb.toString());
        } else if (LocationPreferences.DNA_VCF_FILE.equals(dataType)) {
            sb.append("<p style=\"text-align:center\"><b>" + LocationPreferences.DNA_VCF_FILE + "</b></p>");
            sb.append("<br>");
            sb.append("\tDNA vcf file is the optional file to detect RNA editing sites to make it more precise.");
            sb.append("<br>");
            sb.append("\tWe will use the DNA vcf file to do <I>non-denovo</I> sequencing.");
            sb.append("<br><br>");
            sb.append("<b>Example</b>: BJ22.DNA.chr8.snvs.vcf");
            sb.append("<br>");
            sb.append("<b>From</b>: GATK");
            label.setText(sb.toString());
        } else if (LocationPreferences.REPEAT_FILE.equals(dataType)) {
            sb.append("<p style=\"text-align:center\"><b>" + LocationPreferences.REPEAT_FILE + "</b></p>");
            sb.append("<br>");
            sb.append("\tRepeat file indicates repeat areas.");
            sb.append("<br>");
            sb.append("\tWe will filter out bases located in such areas which are supposed to be unfunctional " +
                    "except for SINE/Alu area.");
            sb.append("<br><br>");
            sb.append("<b>Example</b>: hg19.fa.out");
            sb.append("<br>");
            sb.append("<b>From</b>: http://www.repeatmasker.org/PreMaskedGenomes.html");
            label.setText(sb.toString());
        } else if (LocationPreferences.REF_SEQ_FILE.equals(dataType)) {
            sb.append("<p style=\"text-align:center\"><b>" + LocationPreferences.REF_SEQ_FILE + "</b></p>");
            sb.append("<br>");
            sb.append("\tRefSeq file indicates genes area. ");
            sb.append("<br>");
            sb.append("\tWe will filter out bases located in edge of CDS.");
            sb.append("<br><br>");
            sb.append("<b>Example</b>: genes.gtf/refGene.txt");
            sb.append("<br>");
            sb.append("<b>From</b>: http://genome.ucsc.edu/cgi-bin/hgTables");
            label.setText(sb.toString());
        } else if (LocationPreferences.DBSNP_FILE.equals(dataType)) {
            sb.append("<p style=\"text-align:center\"><b>" + LocationPreferences.DBSNP_FILE + "</b></p>");
            sb.append("<br>");
            sb.append("\tdbSNP file indicates known snp bases.");
            sb.append("<br>");
            sb.append("\tWe will filter out bases which are already snp in DNA level.");
            sb.append("<br><br>");
            sb.append("<b>Example</b>: dbsnp_138.hg19.vcf");
            sb.append("<br>");
            sb.append("<b>From</b>: http://www.ncbi.nlm.nih.gov/SNP/");
            label.setText(sb.toString());
        } else if (LocationPreferences.DARNED_FILE.equals(dataType)) {
            sb.append("<p style=\"text-align:center\"><b>" + LocationPreferences.DARNED_FILE + "</b></p>");
            sb.append("<br>");
            sb.append("\tDarned file indicates known editing sites. ");
            sb.append("<br>");
            sb.append("\tWe will use the reference information to calculate p-value and fdr.");
            sb.append("<br><br>");
            sb.append("<b>Example</b>: hg19.txt");
            sb.append("<br>");
            sb.append("<b>From</b>: http://darned.ucc.ie/static/downloads/hg19.txt");
            label.setText(sb.toString());
        }
    }
}
