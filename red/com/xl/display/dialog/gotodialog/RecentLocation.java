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
package com.xl.display.dialog.gotodialog;

/**
 * The Class RecentLocation stores a recent location the user looks at in the chromosome view.
 */
public class RecentLocation {
    private String c;
    private int start;
    private int end;

    /**
     * Initiate a new RecentLocation instance.
     *
     * @param c     the chromosome name
     * @param start start position
     * @param end   end position
     */
    public RecentLocation(String c, int start, int end) {
        this.c = c;
        this.start = start;
        this.end = end;
    }

    public String chromosome() {
        return c;
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }

    public String toString() {

        return c + ":" + start + "-" + end;
    }

    public int compareTo(RecentLocation l) {
        if (!c.equals(l.c)) {
            return c.compareTo(l.c);
        }
        if (start != l.start()) {
            return (int) (start - l.start());
        }
        return (int) (end - l.end());
    }
}
