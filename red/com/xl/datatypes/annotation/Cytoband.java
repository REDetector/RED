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

package com.xl.datatypes.annotation;

import java.io.Serializable;

/**
 * The Cytoband file format is used to specify the cytobands for an imported genome. We uses this file to draw the chromosome ideograms for the genome, with the
 * idea put forward by IGV.
 */
public class Cytoband implements Serializable {
    String chromosome;
    String name;
    int end;
    int start;
    char type; // p, n, or c
    short stain;

    public Cytoband(String chromosome) {
        this.chromosome = chromosome;
        this.name = "";
    }

    public String getChr() {
        return chromosome;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public short getStain() {
        return stain;
    }

    public void setStain(short stain) {
        this.stain = stain;
    }


}