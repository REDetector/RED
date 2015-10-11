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

package com.xl.utils;

import java.util.List;

/**
 * Created by xing.li on 2015/7/9.
 */
public class EmptyChecker {
    public static <T> boolean isEmptyList(List<T> list) {
        return list == null || list.isEmpty();
    }

    public static <T> boolean isEmptyArray(T[] arrays) {
        return arrays == null || arrays.length == 0;
    }

}
