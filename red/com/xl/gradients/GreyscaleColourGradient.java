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

import java.awt.*;

public class GreyscaleColourGradient extends ColourGradient {


    protected Color[] makeColors() {
        /*
         * We pre-generate a list of 100 colours we're going to use for this display.
		 *
		 * Because a linear gradient ends up leaving too much green in the spectrum we put this on a log scale to emphasise low and high values so the display
		 * is clearer.
		 */

        Color[] colors = new Color[100];


        for (int i = 0; i < 100; i++) {
            int value = (255 * (i + 1)) / 100;
            colors[i] = new Color(value, value, value);
        }

        return colors;
    }

    public String name() {
        return "Greyscale Colour Gradient";
    }
}
