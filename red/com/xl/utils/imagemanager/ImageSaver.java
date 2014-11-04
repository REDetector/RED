package com.xl.utils.imagemanager;

import com.xl.dialog.CrashReporter;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.filefilters.EPSFileFilter;
import com.xl.utils.filefilters.PNGFileFilter;
import com.xl.utils.filefilters.SVGFileFilter;

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
 * A utility class which acts as a wrapper for the SVG or PNG generating
 * code which can be used to save (almost) any component which uses the
 * standard Graphics interface to draw itself.
 */
public class ImageSaver {

    /**
     * Launches a file selector to select which type of file to create and
     * then create it.
     *
     * @param c The component to save.
     */
    public static void saveImage(Component c, String defaultName) {
        JFileChooser chooser = new JFileChooser(LocationPreferences.getInstance().getProjectSaveLocation());

        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(new SVGFileFilter());
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
            int answer = JOptionPane.showOptionDialog(c, file.getName() + " exists.  Do you want to overwrite the existing file?", "Overwrite file?",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Overwrite and Save", "Cancel"}, "Overwrite and Save");

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
                SVGGenerator.writeSVG(pr, c);
                pr.close();
            } else if (filter instanceof EPSFileFilter) {
                EPSGenerator.exportEpsImage(file, c);
            } else {
                System.err.println("Unknown file filter type " + filter + " when saving image");
            }
        } catch (IOException e) {
            new CrashReporter(e);
        }
    }

}
