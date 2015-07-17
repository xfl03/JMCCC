package com.darkyoooooo.jmccc.version;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.darkyoooooo.jmccc.util.OsTypes;
import com.darkyoooooo.jmccc.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Version {
	private String path, parentInheritsPath, launchArgs, id, assets, mainClass, jarId, parentInheritsFormName;
	private boolean isValid = false, isInheritsForm = false;
	private List<String> inheritsFormNames;
	private List<Library> libraries;
	private List<Native> natives;
	private JsonParser jsonParser = new JsonParser();
	
	public Version(File currentDirectory, String name) throws Exception {
		this.path = currentDirectory.getAbsolutePath();
		this.libraries = new ArrayList<Library>();
		this.natives = new ArrayList<Native>();
		this.inheritsFormNames = new ArrayList<String>();
		File jsonFile = new File(String.format("%s/%s.json", this.path, name));
		if(jsonFile.exists() && jsonFile.canRead()) {
			this.isValid = true;
			JsonObject obj = jsonParser.parse(Utils.readFileToString(jsonFile)).getAsJsonObject();
			this.id = obj.get("id").getAsString();
			this.jarId = this.id + ".jar";
			this.assets = obj.get("assets").getAsString();
			this.mainClass = obj.get("mainClass").getAsString();
			this.launchArgs = obj.get("minecraftArguments").getAsString();
			this.loadLibrariesAndNatives(obj);
			
			if(obj.get("inheritsFrom") != null) {
				this.isInheritsForm = true;
				while(obj.has("inheritsFrom")) {
					this.parentInheritsFormName = obj.get("inheritsFrom").getAsString();
					this.inheritsFormNames.add(this.parentInheritsFormName);
					this.parentInheritsPath = new File(currentDirectory.getParent(), this.parentInheritsFormName).getAbsolutePath();
					obj = jsonParser.parse(Utils.readFileToString(new File(Utils.resolvePath(
							    String.format("%s/%s/%s.json", currentDirectory.getParent(), this.parentInheritsFormName, this.parentInheritsFormName)
					      )))).getAsJsonObject();
					this.loadLibrariesAndNatives(obj);
				}
			}
		}
	}
	
	private void loadLibrariesAndNatives(JsonObject obj) {
		JsonArray libs = obj.get("libraries").getAsJsonArray();
		for(int i = 0; i < libs.size(); i++) {
			obj = libs.get(i).getAsJsonObject();
			String[] info = obj.get("name").getAsString().split(":");
			if(!obj.has("natives")) {
				if(obj.has("rules")) {
					if(!this.checkRules(obj.get("rules").getAsJsonArray())) {
						continue;
					}
				}
				this.libraries.add(new Library(info[0], info[1], info[2],
					    obj.has("serverreq") ? obj.get("serverreq").getAsBoolean() : true,
						obj.has("clientreq") ? obj.get("clientreq").getAsBoolean() : true
				));
			} else {
				String suffix = obj.get("natives").getAsJsonObject().get(OsTypes.CURRENT.toString().toLowerCase()).getAsString();
				this.natives.add(new Native(info[0], info[1], info[2],
						suffix.contains("${arch}") ? suffix.replaceAll("\\Q${arch}",
					    System.getProperty("java.vm.name").contains("64") ? "64" : "32") : suffix,
					    obj.has("rules") ? this.checkRules(obj.get("rules").getAsJsonArray()) : true
				));
			}
		}
	}
	
	private boolean checkRules(JsonArray array) {
		boolean flag = false;
		for(int i = 0; i < array.size(); i++) {
			try {
				JsonObject obj = array.get(i).getAsJsonObject();
				if (obj.has("action")) {
					if (obj.get("action").getAsString().contentEquals("allow")) {
						if (!obj.has("os")) {
							flag = true;
						} else {
							flag = obj.get("os").getAsJsonObject().get("name").getAsString().contains(OsTypes.CURRENT.toString().toLowerCase());
						}
					}
					if (obj.get("action").getAsString().contentEquals("disallow")) {
						if (obj.has("os")) {
							return !obj.get("os").getAsJsonObject().get("name").getAsString().contains(OsTypes.CURRENT.toString().toLowerCase());
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return flag;
	}

	public String getPath() {
		return this.path;
	}

	public String getParentInheritsPath() {
		return this.parentInheritsPath;
	}

	public boolean isValid() {
		return this.isValid;
	}

	public boolean isInheritsForm() {
		return this.isInheritsForm;
	}

	public String getLaunchArgs() {
		return this.launchArgs;
	}

	public String getId() {
		return this.id;
	}

	public String getAssets() {
		return this.assets;
	}

	public String getMainClass() {
		return this.mainClass;
	}

	public String getJarId() {
		return this.jarId;
	}
	
	public String getParentInheritsFormName() {
		return this.parentInheritsFormName;
	}

	public List<String> getInheritsFormNames() {
		return this.inheritsFormNames;
	}

	public List<Library> getLibraries() {
		return this.libraries;
	}

	public List<Native> getNatives() {
		return this.natives;
	}
}
