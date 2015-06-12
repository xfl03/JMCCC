package com.darkyoooooo.jmccc.version;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import com.darkyoooooo.jmccc.util.OSNames;
import com.darkyoooooo.jmccc.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Version {
	@Getter private final String path;
	@Getter private boolean isValid = false;
	@Getter private String launchArgs, id, assets, mainClass, jarId;
	@Getter private final List<Library> libraries;
	@Getter private final List<Native> natives;
	
	public Version(String path, String name) throws Exception {
		this.path = path;
		this.libraries = new ArrayList<Library>();
		this.natives = new ArrayList<Native>();
		File jsonFile = new File(Utils.resolvePath(String.format("%s/%s.json", path, name)));
		if(jsonFile.exists() && jsonFile.canRead()) {
			this.isValid = true;
			JsonObject obj = new JsonParser().parse(Utils.readFileToString(jsonFile)).getAsJsonObject();
			this.id = obj.get("id").getAsString();
			this.jarId = this.id + ".jar";
			this.assets = obj.get("assets").getAsString();
			this.mainClass = obj.get("mainClass").getAsString();
			this.launchArgs = obj.get("minecraftArguments").getAsString();
			JsonArray libs = obj.get("libraries").getAsJsonArray();
			for(int i = 0; i < libs.size(); i++) {
				obj = libs.get(i).getAsJsonObject();
				String[] info = obj.get("name").getAsString().split(":");
				if(!obj.has("natives")) {
					this.libraries.add(new Library(info[0], info[1], info[2],
					    obj.has("serverreq") ? obj.get("serverreq").getAsBoolean() : true,
						obj.has("clientreq") ? obj.get("clientreq").getAsBoolean() : true
					));
				} else {
					String suffix = obj.get("natives").getAsJsonObject().get(OSNames.CURRENT.toString().toLowerCase()).getAsString();
					this.natives.add(new Native(info[0], info[1], info[2],
						suffix.contains("${arch}") ? suffix.replaceAll("\\Q${arch}", System.getProperty("os.arch").replaceAll("[^0-9]", "")) : suffix,
						obj.has("rules") ? this.checkNativeRules(obj.get("rules").getAsJsonArray()) : true));
				}
			}
		}
	}
	
	private boolean checkNativeRules(JsonArray array) {
		boolean flag = false;
		for(int i = 0; i < array.size(); i++) {
			try {
				JsonObject obj = array.get(i).getAsJsonObject();
				if(!obj.has("os")) {
					flag = obj.get("action").getAsString().equalsIgnoreCase("allow");
				} else {
					String name = obj.get("os").getAsJsonObject().get("name").getAsString();
					flag = name.toLowerCase().contains(OSNames.CURRENT.toString().toLowerCase()) 
						&& obj.get("action").getAsString().equalsIgnoreCase("allow");
				}
			} catch (Exception e) {
			}
		}
		return flag;
	}
}
