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

        File[] files = null;
        if (parser.requiresFile()) {
            JFileChooser chooser = new JFileChooser(LocationPreferences.getInstance().getProjectSaveLocation());
            chooser.setMultiSelectionEnabled(true);
            chooser.setFileFilter(parser.fileFilter());

            int result = chooser.showOpenDialog(application);
            if (result == JFileChooser.CANCEL_OPTION) return;

            files = chooser.getSelectedFiles();
            LocationPreferences.getInstance().setProjectSaveLocation(files[0].getAbsolutePath());
        }

        parser.addProgressListener(new ProgressDialog(application, parser.name(), parser));

        parser.parseFiles(files);
    }

}
