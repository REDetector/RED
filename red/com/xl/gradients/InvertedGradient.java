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

public class InvertedGradient extends ColourGradient {
    /**
     * The colour gradient.
     */
    private ColourGradient gradient;

    public InvertedGradient(ColourGradient gradient) {
        if (gradient == null) {
            throw new NullPointerException("Gradient can't be null");
        }
        this.gradient = gradient;
    }

    protected Color[] makeColors() {

        Color[] colours = gradient.makeColors();

        Color[] invertedColors = new Color[colours.length];

        for (int i = 0; i < colours.length; i++) {
            invertedColors[invertedColors.length - (i + 1)] = colours[i];
        }

        return invertedColors;

    }

    public String name() {
        return "Inverted " + gradient.name();
    }


}
