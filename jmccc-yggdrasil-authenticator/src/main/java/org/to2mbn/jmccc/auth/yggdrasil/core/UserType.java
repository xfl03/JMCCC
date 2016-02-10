package org.to2mbn.jmccc.auth.yggdrasil.core;

public enum UserType {
	LEGACY("legacy"),
	MOJANG("mojang");

	private final String name;

	private UserType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static UserType getUserType(String type) {
		switch (type) {
			case "legacy":
				return UserType.LEGACY;

			case "mojang":
				return UserType.MOJANG;

			default:
				return null;
		}
	}
}
