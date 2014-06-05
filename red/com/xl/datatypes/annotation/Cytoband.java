package com.xl.datatypes.annotation;

import java.io.Serializable;

public class Cytoband implements Serializable{
    String chromosome;
    String name;
    String longName;
    int end;
    int start;
    char type; // p, n, or c
    short stain;


    public Cytoband(String chromosome) {
        this.chromosome = chromosome;
        this.name = "";
    }


    public void trim() {

        // @todo -- trim arrays
    }

    public String getChr() {
        return chromosome;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        if(longName == null) {
            longName = chromosome.replace("chr", "") + name;
        }
        return longName;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public void setType(char type) {
        this.type = type;
    }

    public char getType() {
        return type;
    }

    public void setStain(short stain) {
        this.stain = stain;
    }

    public short getStain() {
        return stain;
    }


}