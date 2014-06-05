package com.xl.parsers.annotationparsers;


import java.io.File;

import javax.swing.JFileChooser;

import com.xl.dialog.ProgressDialog;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;

/**
 * The Class AnnotationParserRunner provides an asynchonous way to
 * actually set up and run the import of external annotation features.
 */
public class AnnotationParserRunner {
	
	/**
	 * Run annotation parser.
	 * 
	 * @param application the application
	 * @param parser the parser
	 */
	public static void RunAnnotationParser(REDApplication application, AnnotationParser parser) {
		
		File [] files = null;
		if (parser.requiresFile()) {
			JFileChooser chooser = new JFileChooser(REDPreferences.getInstance().getDataLocation());
			chooser.setMultiSelectionEnabled(true);
			chooser.setFileFilter(parser.fileFilter());
		
			int result = chooser.showOpenDialog(application);
			if (result == JFileChooser.CANCEL_OPTION) return;

			files = chooser.getSelectedFiles();
			REDPreferences.getInstance().setLastUsedDataLocation(files[0]);
		}
		
		parser.addProgressListener(new ProgressDialog(application, parser.name(), parser));

		parser.parseFiles(files);
	}
	
}
