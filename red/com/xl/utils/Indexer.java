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

package com.xl.utils;

/**
 * Created by Xing Li on 2014/11/13.
 * <p/>
 * This class intends to collect all indexes used in creating table for database.
 */
public class Indexer {
    public static final String CHROM_POSITION = "index(chrom,pos)";
    public static final String CHROM_COORDINATE = "index(chrom,coordinate)";
    public static final String CHROM_BEGIN_END = "index(chrom,begin,end)";
    public static final String CHROM_TYPE = "index(chrom,type)";
    public static final String CHROM_START_END = "index(chrom,txStart,txEnd)";
}
