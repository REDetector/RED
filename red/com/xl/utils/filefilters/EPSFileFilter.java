package com.xl.utils.filefilters;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Created by Administrator on 2014/6/23.
 */
public class EPSFileFilter extends FileFilter {
    /**
     * A file filter representing EPS files
     */
    @Override
    public boolean accept(File f) {
        if (f.isDirectory() || f.getName().toLowerCase().endsWith(".eps")) {
            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    @Override
    public String getDescription() {
        return "EPS Files";
    }


}
