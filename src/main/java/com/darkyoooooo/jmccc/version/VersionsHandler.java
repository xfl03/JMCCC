package com.darkyoooooo.jmccc.version;

import java.io.File;
import java.util.HashSet;

public class VersionsHandler {
	private final HashSet<Version> versions;
	private final HashSet<String> unvalidVersions;
	
	public VersionsHandler(String gameRoot) {
		this.versions = new HashSet<Version>();
		this.unvalidVersions = new HashSet<String>();
		
		File versionsDir = new File(gameRoot, "versions");
		if(!versionsDir.exists()) {
			return;
		}
		for(File versionDir : versionsDir.listFiles()) {
			if(versionDir.isDirectory()) {
				try {
					if(!(versionDir.getName().split(" ").length > 1)) {
						Version ver = new Version(versionDir, versionDir.getName());
						if(ver.isValid()) {
							this.versions.add(ver);
							continue;
						}
					}
				} catch (Exception e) {
				}
				this.unvalidVersions.add(versionDir.getAbsolutePath());
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
