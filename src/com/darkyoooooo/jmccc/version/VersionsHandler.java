package com.darkyoooooo.jmccc.version;

import java.io.File;
import java.util.HashSet;

import lombok.Getter;

public class VersionsHandler {
	@Getter private final HashSet<IVersion> versions;
	
	public VersionsHandler(String gameRoot) {
		this.versions = new HashSet<IVersion>();
		for(File verDir : new File(gameRoot + "/versions/").listFiles()) {
			if(verDir.isDirectory()) {
				try {
					Version ver = new Version(verDir.getAbsolutePath(), verDir.getName());
					if(ver.isValid()) {
						this.versions.add(ver);
					}
				} catch (Exception e) {
				}
			}
		}
	}
}
