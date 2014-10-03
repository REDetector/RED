package com.xl.datatypes.probes;

/**
 * Created by Administrator on 2014/10/1.
 */
public class ProbeBean {


    private String chr;
    private int pos;
    private String id;
    private char ref;
    private char alt;
    private float qual;
    private String filter;
    private String info;
    private String gt;
    private String ad;
    private String dp;
    private String gq;
    private String pl;

    public ProbeBean(String chr, int pos) {
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

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getGq() {
        return gq;
    }

    public void setGq(String gq) {
        this.gq = gq;
    }

    public String getPl() {
        return pl;
    }

    public void setPl(String pl) {
        this.pl = pl;
    }


}
