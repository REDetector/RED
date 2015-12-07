/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 *
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.datatypes.sites;

/**
 * Created by Xing Li on 2014/10/1.
 * <p>
 * SiteBean is a class to parse a strip of all information from a VCF file. It also uses in querying data from database.
 */
public class SiteBean {
    private String chr;
    private int pos;
    private String id;
    private char ref;
    private char alt;
    private float qual;
    private String filter;
    private String info;
    private String gt;
    private int refCount;
    private int altCount;

    /**
     * The RNA editing level, level = ALT / ( REF + ALT ).
     */
    private double level = -1;
    /**
     * P-value from Fisher's Exact Test.
     */
    private double pvalue = -1;
    /**
     * False Discovery Ratio.
     */
    private double fdr = -1;
    /**
     * Since we need to divide the coding region and Alu region, we set a flag for this.
     */
    private char alu = 'F';

    private char strand = '+';

    /**
     * "create table if not exists " + tableName +
     * "(CHROM varchar(30), POS int, ID varchar(30),REF varchar(5),ALT varchar(5),QUAL float(10,2),FILTER text," +
     * "INFO text,GT varchar(10),REF_COUNT int,ALT_COUNT int,ALU varchar(1) default 'F'," +
     * "STRAND varchar(1) default '+',P_VALUE float(10,8),FDR float(10,8),LEVEL float(10,8),index(chrom,pos))";
     * 
     * @param chr
     * @param pos
     * @param id
     * @param ref
     * @param alt
     * @param qual
     * @param filter
     * @param info
     * @param gt
     * @param refCount
     * @param altCount
     * @param alu
     * @param strand
     */
    public SiteBean(String chr, int pos, String id, char ref, char alt, float qual, String filter, String info,
        String gt, int refCount, int altCount, char alu, char strand, float pValue, float fdr, float level) {
        this.chr = chr;
        this.pos = pos;
        this.id = id;
        this.ref = ref;
        this.alt = alt;
        this.qual = qual;
        this.filter = filter;
        this.info = info;
        this.gt = gt;
        this.refCount = refCount;
        this.altCount = altCount;
        this.alu = alu;
        this.strand = strand;
        this.pvalue = pValue;
        this.fdr = fdr;
        this.level = level;
    }

    public SiteBean(String chr, int pos) {
        this.chr = chr;
        this.pos = pos;
    }

    public String getChr() {
        return chr;
    }

    public int getPos() {
        return pos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public char getRef() {
        return ref;
    }

    public void setRef(char ref) {
        this.ref = ref;
    }

    public char getAlt() {
        return alt;
    }

    public void setAlt(char alt) {
        this.alt = alt;
    }

    public float getQual() {
        return qual;
    }

    public void setQual(float qual) {
        this.qual = qual;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getGt() {
        return gt;
    }

    public void setGt(String gt) {
        this.gt = gt;
    }

    public double getPvalue() {
        return pvalue;
    }

    public void setPValue(double pvalue) {
        this.pvalue = pvalue;
    }

    public double getFdr() {
        return fdr;
    }

    public void setFdr(double fdr) {
        this.fdr = fdr;
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public int getRefCount() {
        return refCount;
    }

    public void setRefCount(int refCount) {
        this.refCount = refCount;
    }

    public int getAltCount() {
        return altCount;
    }

    public void setAltCount(int altCount) {
        this.altCount = altCount;
    }

    public char getAlu() {
        return alu;
    }

    public void setAlu(char alu) {
        this.alu = alu;
    }

    public char getStrand() {
        return strand;
    }

    public void setStrand(char strand) {
        this.strand = strand;
    }

    @Override
    public String toString() {
        return "'" + chr + "'," + pos + ",'" + id + "','" + ref + "','" + alt + "'," + qual + ",'" + filter + "'," + "'"
            + info + "','" + gt + "'," + refCount + "," + altCount + ",'" + alu + "','" + strand + "'," + pvalue + ","
            + fdr + "," + level;
    }
}
