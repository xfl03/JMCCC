package com.darkyoooooo.jmccc.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.darkyoooooo.jmccc.Jmccc;
import com.darkyoooooo.jmccc.version.Library;
import com.darkyoooooo.jmccc.version.Native;

public class Utils {
	public static List<String> resolveRealLibPaths(Jmccc jmccc, List<Library> list) {
		List<String> realPaths = new ArrayList<String>();
		for(Library lib : list) {
			realPaths.add(Utils.resolvePath(String.format("%s/libraries/%s/%s/%s/%s-%s.jar", jmccc.getBaseOptions().getGameRoot(),
				lib.getDomain().replace(".", "/"), lib.getName(), lib.getVersion(), lib.getName(), lib.getVersion())));
		}
		return realPaths;
	}
	
	public static List<String> resolveRealNativePaths(Jmccc jmccc, List<Native> list) {
		List<String> realPaths = new ArrayList<String>();
		for(Native nat : list) {
			if(!nat.isAllowed()) {
				continue;
			}
			realPaths.add(Utils.resolvePath(String.format("%s/libraries/%s/%s/%s/%s-%s-%s.jar", jmccc.getBaseOptions().getGameRoot(),
				nat.getDomain().replace(".", "/"), nat.getName(), nat.getVersion(), nat.getName(), nat.getVersion(),
				nat.getSuffix())));
		}
		return realPaths;
	}
	
	public static String resolvePath(String path) {
		return new File(path).getAbsolutePath();
	}
	
	public static String getJavaPath() {
		return new File(new File(System.getProperty("java.home"), "bin"), "java.exe").getAbsolutePath();
	}
	
	public static String genRandomTokenOrUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	public static String readFileToString(File file) throws IOException {
		StringBuffer buffer = new StringBuffer();
		FileReader fileReader = new FileReader(file);
		BufferedReader reader = new BufferedReader(fileReader);
		while(reader.ready()) {
			buffer.append(reader.readLine() + "\n");
		}
		fileReader.close();
		reader.close();
		return buffer.toString();
	}
	
	public static void uncompressZipFile(File file, String outputPath) throws IOException {
		ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
		BufferedInputStream input = new BufferedInputStream(zipInput);
		BufferedOutputStream output = null;
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
				int i;
				byte[] b = new byte[1024];
				while((i = input.read(b)) != -1) {
					output.write(b, 0, i);
				}
			}
		}
		zipInput.close();
		input.close();
		output.close();
	}
}
