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

package com.xl.datatypes.sequence;

import com.xl.utils.ChromosomeNameComparator;
import com.xl.utils.Strand;

/**
 * Created by Administrator on 2014/10/26.
 */
public class Alignment extends Location {
    String chr;
    Strand strand;

    public Alignment(String chr, int start, int end, boolean negative) {
        super(start, end);
        this.chr = chr;
        if (negative) {
            strand = Strand.NEGATIVE;
        } else {
            strand = Strand.POSITIVE;
        }
    }

    public Alignment(String chr, int start, int end, Strand strand) {
        super(start, end);
        this.chr = chr;
        this.strand = strand;
    }

    public String getChr() {
        return chr;
    }

    public Strand getStrand() {
        return strand;
    }

    @Override
    public String toString() {
        return start + "\t" + end + "\t" + Strand.parseStrand(strand);
    }

    @Override
    public int compareTo(Location o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Alignment) {
            int result = ChromosomeNameComparator.getInstance().compare(getChr(), ((Alignment) o).getChr());
            if (result != 0) {
                return result;
            }
        }
        if (getStart() > o.getStart()) {
            return 1;
        } else if (getStart() < o.getStart()) {
            return -1;
        } else {
            return 0;
        }
    }
}
