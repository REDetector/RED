/**
 * Copyright 2009-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.xl.utils.imagemanager;

import net.sf.epsgraphics.ColorMode;
import net.sf.epsgraphics.EpsGraphics;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EPSGenerator {

    /**
     * Instantiates a new generator.  Not used externally - all external calls
     * to this class should go via the static convert to SVG method.
     *
     * @param c The component to convert
     */
    public static boolean exportEpsImage(File file, Component c) throws IOException {
        Graphics2D g;
        FileOutputStream fos = null;
        boolean exportSuccess = false;
        try {
            ColorMode colorModeClass = ColorMode.COLOR_RGB;
            fos = new FileOutputStream(file);
            g = new EpsGraphics("eps", fos, 0, 0, c.getWidth(), c.getHeight(), colorModeClass);
            // EpsGraphics stores directly in a file
            c.paintAll(g);
            exportSuccess = true;

        } catch (Exception e) {
            e.printStackTrace();
            exportSuccess = false;
        } finally {
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }
        return exportSuccess;
    }


}