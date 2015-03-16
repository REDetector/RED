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

import java.text.DecimalFormat;

/**
 * The Class AxisScale calculates the scale interval by the given minimum and maximum values.
 */
public class AxisScale {

    private double min;
    private double max;
    private double starting;
    private double interval;
    private DecimalFormat df = null;

    public AxisScale(double min, double max) {

        this.min = min;
        this.max = max;

        if (max <= min) {
            starting = min;
            interval = 1;
            return;
        }

        double base = 1;

        while (base > (max - min)) {
            base /= 10;
        }

        double[] divisions = new double[]{1, 2, 2.5, 5};

        OUTER:
        while (true) {
            for (double division : divisions) {
                double tester = base * division;
                if (((max - min) / tester) <= 10) {
                    interval = tester;
                    break OUTER;
                }
            }
            base *= 10;

        }

        // Now we work out the first value to be plotted
        int basicDivision = (int) (min / interval);

        double testStart = basicDivision * interval;

        if (testStart < min) {
            testStart += interval;
        }

        starting = testStart;

    }

    public static void main(String[] args) {
        AxisScale as = new AxisScale(-4.75, 5.52);
        System.out.println("Scale is " + as.getMin() + "-" + as.getMax() + " starts at " + as.getStartingValue() + " with interval " + as.getInterval());
    }

    public String format(double number) {
        if (df == null) {
            if (interval == (int) interval) {
                df = new DecimalFormat("#");
            } else {
                String stringInterval = "" + interval;
                // Find the number of decimal places
                int dp = stringInterval.length() - (stringInterval.indexOf(".") + 1);
                StringBuilder formatBuffer = new StringBuilder();
                formatBuffer.append("#.");
                for (int i = 0; i < dp; i++) {
                    formatBuffer.append("#");
                }
                df = new DecimalFormat(formatBuffer.toString());
            }
        }

        return df.format(number);
    }

    public double getStartingValue() {
        return starting;
    }

    public double getInterval() {
        return interval;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
