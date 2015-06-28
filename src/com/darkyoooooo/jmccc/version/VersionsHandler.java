package com.darkyoooooo.jmccc.version;

import java.io.File;
import java.util.HashSet;

import lombok.Getter;

public class VersionsHandler {
	@Getter private final HashSet<Version> versions;
	@Getter private final HashSet<String> unvalidVersions;
	
	public VersionsHandler(String gameRoot) {
		this.versions = new HashSet<Version>();
		this.unvalidVersions = new HashSet<String>();
		
		File versions = new File(gameRoot, "versions");
		if (!versions.exists()) {
			versions.mkdirs();
			return;
		}
		for(File verDir : versions.listFiles()) {
			if(verDir.isDirectory()) {
				try {
					Version ver = new Version(verDir, verDir.getName());
					if(ver.isValid()) {
						this.versions.add(ver);
					} else {
						this.unvalidVersions.add(verDir.getAbsolutePath());
					}
				} catch (Exception e) {
					this.unvalidVersions.add(verDir.getAbsolutePath());
				}
			}
		}
	}
}
