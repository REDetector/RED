/**
 * 功能：
 * 	1、文件读取与删除
 */

package com.xl.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class FileUtils {
	private static boolean flag = false;

	public static void writeData(String path, String content) {
		File file = new File(path); // 定义要操作的文件
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs(); // 创建父文件夹路径
		}
		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
			out.println(content);
		} catch (Exception e) {
			e.printStackTrace();
		} finally { // 一定要关闭流
			if (out != null) {
				out.close();
			}
		}
	}

	public static boolean isFileExist(String path) {
		return (new File(path)).exists();
	}
	
	/**
	 * 追加文件：使用FileWriter
	 * 
	 * @param path
	 * @param content
	 */
	public static void appendData(String path, String content) {
		FileWriter writer = null;
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			writer = new FileWriter(path, true);
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param path
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String path) {
		flag = false;
		File file = new File(path);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param path
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String path) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		File dirFile = new File(path);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}
}
