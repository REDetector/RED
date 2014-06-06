package com.xl.utils.filefilters;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileFilterExt extends FileFilter {

    private String suffix = null;

    public FileFilterExt(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean accept(File f) {
        // TODO Auto-generated method stub
        if (f.isDirectory() || f.getName().toLowerCase().endsWith("." + suffix)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return suffix + " files";
    }

}