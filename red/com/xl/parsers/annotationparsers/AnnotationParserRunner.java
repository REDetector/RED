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

package com.xl.parsers.annotationparsers;


import com.xl.display.dialog.JFileChooserExt;
import com.xl.display.dialog.ProgressDialog;
import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;

import javax.swing.*;
import java.io.File;

/**
 * The Class AnnotationParserRunner provides an asynchronous way to actually set up and run the import of external annotation features.
 */
public class AnnotationParserRunner {

    /**
     * Run annotation parser.
     *
     * @param application the application
     * @param parser      the parser
     */
    public static void RunAnnotationParser(REDApplication application, AnnotationParser parser) {
        File file;
        JFileChooser chooser = new JFileChooserExt(LocationPreferences.getInstance().getGenomeDirectory());
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(parser.fileFilter());

        int result = chooser.showOpenDialog(application);
        if (result == JFileChooser.CANCEL_OPTION) return;

        file = chooser.getSelectedFile();
        LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());
        parser.addProgressListener(new ProgressDialog(application, parser.name(), parser));
        parser.parseFile(file);

    }

}
