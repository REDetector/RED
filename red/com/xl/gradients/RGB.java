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
package com.xl.gradients;

/**
 * A simple class to represent separate values for Red Green and Blue components of a colour.
 */
public class RGB {
    /**
     * Red.
     */
    public int r;
    /**
     * Green.
     */
    public int g;
    /**
     * Blue.
     */
    public int b;

    /**
     * Instantiates a new RGB colour.
     *
     * @param r RED
     * @param g GREEN
     * @param b BLUE
     */
    public RGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
