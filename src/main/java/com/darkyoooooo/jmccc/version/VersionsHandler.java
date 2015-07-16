package com.darkyoooooo.jmccc.version;

import java.io.File;
import java.util.HashSet;

public class VersionsHandler {
	private final HashSet<Version> versions;
	private final HashSet<String> unvalidVersions;
	
	public VersionsHandler(String gameRoot) {
		this.versions = new HashSet<Version>();
		this.unvalidVersions = new HashSet<String>();
		
		File versions = new File(gameRoot, "versions");
		if(!versions.exists()) {
			return;
		}
		for(File version : versions.listFiles()) {
			if(version.isDirectory()) {
				try {
					if(!(version.getName().split(" ").length > 1)) {
						Version ver = new Version(version, version.getName());
						if(ver.isValid()) {
							this.versions.add(ver);
						}
					}
				} catch (Exception e) {
				}
				this.unvalidVersions.add(version.getAbsolutePath());
			}
		}
	}
	
	public Version getVersionById(String id) {
		Version version = null;
		for(Version ver : this.versions) {
			if (ver.getId().contentEquals(id)) {
				version = ver;
				break;
			}
		}
		return version;
	}

	public HashSet<Version> getVersions() {
		return this.versions;
	}

	public HashSet<String> getUnvalidVersions() {
		return this.unvalidVersions;
	}
}
