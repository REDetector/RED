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

package com.xl.datatypes.sites;

import com.xl.datatypes.sequence.Location;
import com.xl.utils.ChromosomeNameComparator;

/**
 * The Class Site contains the basic information of a RNA editing site.
 */
public class Site extends Location {

    /**
     * The chr.
     */
    private String chr;
    /**
     * The reference base.
     */
    private char refBase;
    /**
     * The alternative base.
     */
    private char altBase;
    /**
     * The depth of coverage.
     */
    private int depth = 0;

    /**
     * Instantiates a new site.
     *
     * @param chr      The chr
     * @param position The position
     * @param refBase  The reference base
     * @param altBase  The alternative base
     */
    public Site(String chr, int position, char refBase, char altBase) {
        super(position, position);
        this.chr = chr;
        this.refBase = refBase;
        this.altBase = altBase;
    }

    /**
     * Instantiates a new site.
     *
     * @param chr      The chr
     * @param position The position
     * @param depth    The depth of coverage.
     */
    public Site(String chr, int position, int depth) {
        super(position, position);
        this.chr = chr;
        this.depth = depth;
    }

    public char getRefBase() {
        return refBase;
    }

    public char getAltBase() {
        return altBase;
    }

    public String getChr() {
        return chr;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return chr + "\t" + getStart() + "\t" + refBase + "\t" + altBase;
    }

    @Override
    public int compareTo(Location o) {
        int result = 0;
        if (o instanceof Site) {
            result = ChromosomeNameComparator.getInstance().compare(getChr(), ((Site) o).getChr());
            if (result == 0) {
                result = super.compareTo(o);
            }
        }
        return result;
    }
}
