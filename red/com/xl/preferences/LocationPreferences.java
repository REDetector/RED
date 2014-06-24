package com.xl.preferences;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by Administrator on 2014/6/23.
 */
public class LocationPreferences {
    private String fastaFile = "";
    private String genomeFile = "";
    private String rnaFile = "";
    private String dnaFile = "";
    private String annotationFile = "";
    /**
     * The recently opened files list
     */
    private LinkedList<String> recentlyOpenedFiles = new LinkedList<String>();

    /**
     * The project root directory
     */
    private File projectRootDirectory = new File("");
}
