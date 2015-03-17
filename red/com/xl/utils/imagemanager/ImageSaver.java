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

package com.xl.utils.imagemanager;

import com.xl.display.dialog.CrashReporter;
import com.xl.display.dialog.JFileChooserExt;
import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.filefilters.EPSFileFilter;
import com.xl.utils.filefilters.PNGFileFilter;
import com.xl.utils.filefilters.SVGFileFilter;
import com.xl.utils.ui.OptionDialogUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A utility class which acts as a wrapper for the EPS, SVG or PNG generating code which can be used to save (almost) any component which uses the standard
 * Graphics interface to draw itself.
 */
public class ImageSaver {

    /**
     * Launches a file selector to select which type of file to create and then create it.
     *
     * @param c The component to save.
     */
    public static void saveImage(Component c, String defaultName) {
        JFileChooser chooser = new JFileChooserExt(LocationPreferences.getInstance().getProjectSaveLocation());

        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(new EPSFileFilter());
        chooser.addChoosableFileFilter(new PNGFileFilter());
        chooser.setFileFilter(new SVGFileFilter());
        File defaultFile = new File(LocationPreferences.getInstance().getProjectSaveLocation() + "/" + defaultName);
        chooser.setSelectedFile(defaultFile);

        int result = chooser.showSaveDialog(c);
        if (result == JFileChooser.CANCEL_OPTION) return;

        File file = chooser.getSelectedFile();
        LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());

        if (file.isDirectory()) return;

        FileFilter filter = chooser.getFileFilter();

        if (filter instanceof PNGFileFilter) {
            if (!file.getPath().toLowerCase().endsWith(".png")) {
                file = new File(file.getPath() + ".png");
            }
        } else if (filter instanceof SVGFileFilter) {
            if (!file.getPath().toLowerCase().endsWith(".svg")) {
                file = new File(file.getPath() + ".svg");
            }
        } else if (filter instanceof EPSFileFilter) {
            if (!file.getPath().toLowerCase().endsWith(".eps")) {
                file = new File(file.getPath() + ".eps");
            }
        } else {
            System.err.println("Unknown file filter type " + filter + " when saving image");
            return;
        }

        if (file.exists()) {
            int answer = OptionDialogUtils.showFileExistDialog(REDApplication.getInstance(), file.getName());
            if (answer > 0) {
                return;
            }
        }

        try {
            if (filter instanceof PNGFileFilter) {
                BufferedImage b = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics g = b.getGraphics();
                c.paint(g);
                ImageIO.write(b, "PNG", file);
            } else if (filter instanceof SVGFileFilter) {
                PrintWriter pr = new PrintWriter(new FileWriter(file));
                SVGGenerator.exportSVGImage(pr, c);
                pr.close();
            } else if (filter instanceof EPSFileFilter) {
                EPSGenerator.exportEPSImage(file, c);
            } else {
                System.err.println("Unknown file filter type " + filter + " when saving image");
            }
        } catch (IOException e) {
            new CrashReporter(e);
        }
    }

}
