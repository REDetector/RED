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

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;


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
            Class colorModeClass = Class.forName("net.sf.epsgraphics.ColorMode");
            Class graphicsClass = Class.forName("net.sf.epsgraphics.EpsGraphics");
            Constructor constructor = graphicsClass.getConstructor(String.class, OutputStream.class,
                    int.class, int.class, int.class, int.class, colorModeClass);
            Object colorModeValue = Enum.valueOf(colorModeClass, "COLOR_RGB");
            // EpsGraphics stores directly in a file
            fos = new FileOutputStream(file);
            g = (Graphics2D) constructor.newInstance("eps", fos, 0, 0, c.getWidth(), c.getHeight(), colorModeValue);

            c.paintAll(g);

            graphicsClass.getMethod("close").invoke(g);
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
