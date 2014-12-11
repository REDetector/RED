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

package com.xl.datatypes.genome;

import com.xl.datatypes.annotation.Cytoband;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * The Class Chromosome represents a single chromosome in a genome
 */
public class Chromosome implements Comparable<Chromosome>, Serializable {

    /**
     * The name.
     */
    private String name;

    /**
     * The length.
     */
    private int length = 0;

    /**
     * The list of cytobands with this chromosome.
     */
    private List<Cytoband> cytobands;

    /**
     * Instantiates a new chromosome.
     *
     * @param name the name
     */
    public Chromosome(String name) {
        this.name = name;
    }

    /**
     * Instantiates a new chromosome with the chromosome length. It is used in annotation parser.
     *
     * @param name   The chromosome name
     * @param length The chromosome length
     */
    public Chromosome(String name, int length) {
        this.name = name;
        this.length = length;
        final Cytoband cytoband = new Cytoband(name);
        cytoband.setStart(0);
        cytoband.setEnd(length);
        cytobands = Arrays.asList(cytoband);
    }

    /**
     * Name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Length.
     *
     * @return the length in bp
     */
    public int getLength() {
        return length;
    }

    /**
     * @return List of cytobands for this chromosome, if any. Can be null.
     */
    public List<Cytoband> getCytobands() {
        return cytobands;
    }

    /**
     * Set cytoband.
     *
     * @param cytobands The cytobands
     */
    public void setCytobands(List<Cytoband> cytobands) {
        this.cytobands = cytobands;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Chromosome o) {
        try {
            return (new Integer(Integer.parseInt(name)).compareTo(Integer.parseInt(o.getName())));
        } catch (Exception e) {
            return name.compareTo(o.getName());
        }
    }

}
