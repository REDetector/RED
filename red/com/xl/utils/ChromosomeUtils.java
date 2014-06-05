package com.xl.utils;

public class ChromosomeUtils {
	public static String getStandardChromosomeName(String chr){
		if(chr.length()==4){
			return chr;
		}
		String chrName = chr.substring(0, 5);
		Character c = chrName.charAt(4);
		if(Character.isDigit(c)){
			return chrName;
		}else {
			return null;
		}
	}
}
