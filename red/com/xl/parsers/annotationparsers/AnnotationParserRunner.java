package com.xl.parsers.annotationparsers;


import com.xl.dialog.ProgressDialog;
import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;

import javax.swing.*;
import java.io.File;

/**
 * The Class AnnotationParserRunner provides an asynchonous way to
 * actually set up and run the import of external annotation features.
 */
public class AnnotationParserRunner {

    /**
     * Run annotation parser.
     *
     * @param application the application
     * @param parser      the parser
     */
    public static void RunAnnotationParser(REDApplication application, AnnotationParser parser) {
        if (parser.requiresFile()) {
            File file;
            JFileChooser chooser = new JFileChooser(LocationPreferences.getInstance().getProjectSaveLocation());
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileFilter(parser.fileFilter());

            int result = chooser.showOpenDialog(application);
            if (result == JFileChooser.CANCEL_OPTION) return;

            file = chooser.getSelectedFile();
            LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());
            parser.addProgressListener(new ProgressDialog(application, parser.name(), parser));
            parser.parseFiles(file);
        } else {
            System.err.println("Nothing to parse");
        }

    }

}
