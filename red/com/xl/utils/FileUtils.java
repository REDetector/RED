/**
 * ���ܣ�
 * 	1���ļ���ȡ��ɾ��
 */

package com.xl.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class FileUtils {
	private static boolean flag = false;

	public static void writeData(String path, String content) {
		File file = new File(path); // ����Ҫ�������ļ�
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs(); // �������ļ���·��
		}
		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
			out.println(content);
		} catch (Exception e) {
			e.printStackTrace();
		} finally { // һ��Ҫ�ر���
			if (out != null) {
				out.close();
			}
		}
	}

	public static boolean isFileExist(String path) {
		return (new File(path)).exists();
	}
	
	/**
	 * ׷���ļ���ʹ��FileWriter
	 * 
	 * @param path
	 * @param content
	 */
	public static void appendData(String path, String content) {
		FileWriter writer = null;
		try {
			// ��һ��д�ļ��������캯���еĵڶ�������true��ʾ��׷����ʽд�ļ�
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
	 * ɾ�������ļ�
	 * 
	 * @param path
	 *            ��ɾ���ļ����ļ���
	 * @return �����ļ�ɾ���ɹ�����true�����򷵻�false
	 */
	public static boolean deleteFile(String path) {
		flag = false;
		File file = new File(path);
		// ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * ɾ��Ŀ¼���ļ��У��Լ�Ŀ¼�µ��ļ�
	 * 
	 * @param path
	 *            ��ɾ��Ŀ¼���ļ�·��
	 * @return Ŀ¼ɾ���ɹ�����true�����򷵻�false
	 */
	public static boolean deleteDirectory(String path) {
		// ���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		File dirFile = new File(path);
		// ���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		// ɾ���ļ����µ������ļ�(������Ŀ¼)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// ɾ�����ļ�
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // ɾ����Ŀ¼
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// ɾ����ǰĿ¼
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}
}
