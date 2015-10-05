/*
 * REFilters: RNA Editing Filters Copyright (C) <2014> <Xing Li>
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

package com.xl.utils;

/**
 * Created by Administrator on 2014/12/31.
 */
public class NegativeType {
    public static String getNegativeStrandEditingType(String editingType) {
        if (editingType == null || editingType.length() != 2) {
            return null;
        }
        char[] types = editingType.toCharArray();
        return getNegativeStrandBase(types[0]) + getNegativeStrandBase(types[1]);
    }

    private static String getNegativeStrandBase(char type) {
        switch (type) {
            case 'A':
                return "T";
            case 'G':
                return "C";
            case 'T':
                return "A";
            case 'C':
                return "G";
            default:
                return "T";
        }
    }

}
