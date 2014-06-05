package com.xl.utils;

import java.io.File;
import java.io.FileFilter;

public class FileFilterImpl implements FileFilter {

	private String suffix = null;

	public FileFilterImpl(String suffix) {
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

}