package net.kronos.mclaunch_util_lib.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Util
{
	public static String readFile(File file) throws FileNotFoundException, IOException
	{
		String fileContents = "";
		
		try(BufferedReader br = new BufferedReader(new FileReader(file)))
		{
	        String line;
	        
	        while ((line = br.readLine()) != null)
	        	fileContents += (line + System.lineSeparator());
	    }
		
		return fileContents;
	}
}
