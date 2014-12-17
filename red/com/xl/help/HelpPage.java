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
package com.xl.help;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class HelpPage describes a single help page in the help system.
 */
public class HelpPage extends DefaultMutableTreeNode {
    /**
     * The file.
     */
    private File file;
    /**
     * The name.
     */
    private String name;

    /**
     * Instantiates a new help page.
     *
     * @param file the file
     */
    public HelpPage(File file) {
        this.file = file;
        name = file.getName().replaceFirst("\\.[hH][tT][mM][lL]?$", "");

        String[] nameSections = name.split(" ");
        if (nameSections.length > 1) {
            // We have two sections so check if the first is just integers separated by dots.  If it is then we can lose it.
            String[] numbers = nameSections[0].split("\\.");
            for (String number : numbers) {
                if (!number.matches("\\d+")) {
                    return;
                }
            }

            // If we get here then we want to chop the first part of the name off
            StringBuilder sb = new StringBuilder(nameSections[1]);
            for (int s = 2; s < nameSections.length; s++) {
                sb.append(" ");
                sb.append(nameSections[s]);
            }
            name = sb.toString();
        }

    }

    /**
     * Contains string.
     *
     * @param searchTerm the search term
     * @param hits       the hits
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void containsString(String searchTerm, Vector<HelpPage> hits) throws IOException {

        // We don't want to be trying to open directories
        if (isLeaf()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                searchTerm = searchTerm.toLowerCase();
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.toLowerCase().contains(searchTerm)) {
                        hits.add(this);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    br.close();
                }
            }
        }
        // Extend the search to our children
        Enumeration kids = children();
        while (kids.hasMoreElements()) {
            Object node = kids.nextElement();
            if (node instanceof HelpPage) {
                ((HelpPage) node).containsString(searchTerm, hits);
            }
        }
    }

    @Override
    public boolean isLeaf() {
        return !file.isDirectory();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

}
