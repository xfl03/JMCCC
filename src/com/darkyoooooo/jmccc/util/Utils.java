package com.darkyoooooo.jmccc.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.Cleanup;

public class Utils {
	
	public static String resolvePath(String path) {
		return path.replace("/", OSNames.CURRENT.getFileSpearator() + "");
	}
	
	public static String getJavaPath() {
		return resolvePath(new File(System.getProperty("java.home")  + "/bin/java.exe").getAbsolutePath());
	}
	
	public static String genRandomTokenOrUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	public static String readFileToString(File file) throws IOException {
		StringBuffer buffer = new StringBuffer();
		@Cleanup FileReader fileReader = new FileReader(file);
		@Cleanup BufferedReader reader = new BufferedReader(fileReader);
		while(reader.ready()) {
			buffer.append(reader.readLine());
		}
		return buffer.toString();
	}
	
	public static void uncompressZipFile(File file, String outputPath) throws IOException {
		@Cleanup ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
		@Cleanup BufferedInputStream input = new BufferedInputStream(zipInput);
		@Cleanup BufferedOutputStream output = null;
		File temp = null;
		ZipEntry entry = null;
		while((entry = zipInput.getNextEntry()) != null) {
			temp = new File(outputPath, entry.getName());
			if(entry.isDirectory()) {
				temp.mkdir();
			} else {
				if(!temp.exists()) {
					new File(temp.getParent()).mkdirs();
				}
				output = new BufferedOutputStream(new FileOutputStream(temp));
				byte[] b = new byte[1024 * 4];
				while(input.read(b) != -1) {
					output.write(b);
				}
			}
		}
	}
}
