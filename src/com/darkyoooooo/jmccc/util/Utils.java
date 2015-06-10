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
	public static String getSystemName() {
		String name = System.getProperty("os.name").toLowerCase();
		if(name.contains("win")) return "windows";
		else if(name.contains("linux")) return "linux";
		else if(name.contains("osx")) return "osx";
		else return "fuckyou";
	}
	
	public static String resolvePath(String path) {
		return path.replace("/", System.getProperty("file.separator"));
	}
	
	public static String getJavaPath() {
		return resolvePath(new File(System.getProperty("java.home")  + "/bin/java.exe").getAbsolutePath());
	}
	
	public static String genRandomTokenOrUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	public static String readFileToString(File file) throws IOException {
		StringBuffer buffer = new StringBuffer();
		FileReader fileReader = new FileReader(file);
		BufferedReader reader = new BufferedReader(fileReader);
		while(reader.ready()) {
			buffer.append(reader.readLine());
		}
		fileReader.close();
		reader.close();
		return buffer.toString();
	}
	
	public static void uncompressZipFile(File file, String outputPath) throws IOException {
		@Cleanup ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
		@Cleanup BufferedInputStream input = new BufferedInputStream(zipInput);
		@Cleanup BufferedOutputStream output = null;
		File temp = null;
		ZipEntry entry;
		while((entry = zipInput.getNextEntry()) != null) {
			temp = new File(outputPath, entry.getName());
			if(entry.isDirectory()) {
				temp.mkdir();
			} else {
				if(!temp.exists()) {
					new File(temp.getParent()).mkdirs();
				}
				output = new BufferedOutputStream(new FileOutputStream(temp));
				int b;
				while((b = input.read()) != -1){
					output.write(b);
				}
			}
		}
	}
}
