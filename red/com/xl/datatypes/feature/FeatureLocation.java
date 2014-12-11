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

package com.xl.datatypes.feature;

import com.xl.datatypes.sequence.Location;

/**
 * Created by Xing Li on 2014/10/26.
 * <p/>
 * The FeatureLocation class is a wrap class for feature to parse location by a given string. It is only used in REDParser.
 */
public class FeatureLocation extends Location {

    public FeatureLocation(String location) {
        super(Integer.parseInt(location.split("-")[0]), Integer.parseInt(location.split("-")[1]));
    }

}
