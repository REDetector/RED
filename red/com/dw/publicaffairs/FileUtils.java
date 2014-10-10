package com.dw.publicaffairs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileUtils {
    public final static Map<String, String> alter = new HashMap<String, String>();

    static {
        for (int i = 1; i < 23; i++) {
            alter.put(i + "", "chr" + i);
            alter.put("ch" + i, "chr" + i);
            alter.put("Chr" + i, "chr" + i);
        }
        alter.put("M", "chrM");
        alter.put("Y", "chrY");
        alter.put("X", "chrX");
    }

    public static File openFile(String path) {
        return new File(path);

    }
}
