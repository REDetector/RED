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

/**
 * ���ܣ�
 * 	1���ļ���ȡ��ɾ��
 */

package com.xl.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class FileUtils provides some I/O operations
 */
public class FileUtils {


    public static boolean createDirectory(String path) {
        File f = new File(path);
        return f.exists() || f.mkdirs();
    }

    public static String getFileNameFromURL(String url) {

        int lastIndexOfSlash = url.lastIndexOf("/");

        String fileName;

        if (lastIndexOfSlash > -1) {
            fileName = url.substring(lastIndexOfSlash + 1, url.length());
        } else {
            fileName = url;
        }

        return fileName;
    }

    /**
     * Delete the file by a given file path.
     *
     * @param path the file to be deleted
     * @return true if delete successfully.
     */
    public static boolean deleteFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            if (file.delete()) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * Delete the file by a given file path with specific suffix.
     *
     * @param path   the file to be deleted
     * @param suffix the suffix
     * @return true if delete successfully.
     */
    public static boolean deleteFileWithSuffix(String path, String suffix) {
        boolean flag = false;
        File file = new File(path);
        if (file.isFile() && file.exists() && file.getName().endsWith(suffix)) {
            if (file.delete()) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * Delete the directory by a given file path.
     *
     * @param path the directory path to be deleted
     * @return true if delete successfully.
     */
    public static boolean deleteDirectory(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    flag = deleteFile(file.getAbsolutePath());
                    if (!flag)
                        break;
                } else {
                    flag = deleteDirectory(file.getAbsolutePath());
                    if (!flag)
                        break;
                }
            }
        }

        return flag && dirFile.delete();
    }

    /**
     * Delete the files by a given file path with specific suffix.
     *
     * @param path   the files to be deleted
     * @param suffix the suffix
     * @return true if delete successfully.
     */
    public static boolean deleteAllFilesWithSuffix(String path, String suffix) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    flag = deleteFileWithSuffix(file.getAbsolutePath(), suffix);
                    if (!flag)
                        break;
                }
            }
        }
        return flag;
    }

    /**
     * Search the files which meets the file name in the given directory.
     *
     * @param fileName  the name of the searching file
     * @param directory the directory to be searched
     * @return a list that contains the required files.
     */
    public static List<File> searchFile(String fileName, File directory) {
        List<File> fileLists = new ArrayList<File>();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        searchFile(fileName, file);
                    } else {
                        if (file.getAbsolutePath().contains(fileName)) {
                            fileLists.add(file);
                        }
                    }
                }
            }
        }
        return fileLists;
    }

    /**
     * Copy the whole content of a folder from one place to another.
     *
     * @param inputPath  The ordinary folder to be copied.
     * @param outputPath The destination folder to copy to.
     */
    public static void copyFolder(String inputPath, String outputPath) {

        try {
            File output = new File(outputPath);
            if (!output.exists()) {
                if (!output.mkdirs()) {
                    throw new IOException("Could not establish the folder '" + output.getAbsolutePath() + "'");
                }
            }
            File input = new File(inputPath);
            String[] files = input.list();
            File tempFile;
            for(String file:files){
                if (inputPath.endsWith(File.separator)) {
                    tempFile = new File(inputPath + file);
                } else {
                    tempFile = new File(inputPath + File.separator + file);
                }
                if (tempFile.isFile()) {
                    FileInputStream fis = new FileInputStream(tempFile);
                    FileOutputStream fos = new FileOutputStream(outputPath + "/" + tempFile.getName());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = fis.read(b)) != -1) {
                        fos.write(b, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    fis.close();
                }
                if (tempFile.isDirectory()) {
                    copyFolder(inputPath + "/" + file, outputPath + "/" + file);
                }
            }
        } catch (Exception e) {
            System.out.println("Wrong when copying the folder.");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        //        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("E:\\Master\\ChongQing\\Data\\BJ22N_DNA_RNA\\BJ22_sites.hard" +
        //                ".filtered.vcf")));
        //        String line;
        //        int count = 0;
        //        while ((line = br.readLine()) != null) {
        //            if (line.startsWith("##")) continue;
        //            if (count++ < 1000) continue;
        //            if (count++ < 2000) System.out.println(line);
        //        }
    }
}
