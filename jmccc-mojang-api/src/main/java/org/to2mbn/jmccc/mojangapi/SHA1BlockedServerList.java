package org.to2mbn.jmccc.mojangapi;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.to2mbn.jmccc.util.HexUtils;

class SHA1BlockedServerList implements BlockedServerList {

	private static final long serialVersionUID = 1L;

	private static final Pattern IP_REGEX = Pattern.compile("^(2[0-4]\\d|25[0-5]|1\\d\\d|[1-9]?\\d)(\\.(2[0-4]\\d|25[0-5]|1\\d\\d|[1-9]?\\d)){3}$");

	private Set<String> entries;

	public SHA1BlockedServerList(Set<String> entries) throws NoSuchAlgorithmException {
		this.entries = Objects.requireNonNull(entries);
	}

	@Override
	public boolean isBlocked(String host) {
		if (isBlockedServer(host)) {
			return true;
		}

		InetAddress address;
		try {
			address = InetAddress.getByName(null);
		} catch (UnknownHostException e) {
			return false;
		}
		return isBlockedServer(address.getHostAddress())
				|| isBlockedServer(address.getHostName());
	}

	@Override
	public String[] getEntries() {
		return entries.toArray(new String[entries.size()]);
	}

	private boolean isInBlockedList(String str) {
		try {
			return entries.contains(HexUtils.bytesToHex(MessageDigest.getInstance("SHA-1").digest(str.toLowerCase().getBytes("ISO-8859-1"))));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	private boolean isBlockedServer(String server) {
		if (server == null || server.isEmpty())
			return false;

		int lastNonDotIdx = server.length() - 1;
		while (lastNonDotIdx >= 0 && server.charAt(lastNonDotIdx) == '.')
			lastNonDotIdx--;
		if (lastNonDotIdx < 0)
			return false;
		server = server.substring(0, lastNonDotIdx + 1);

		if (isInBlockedList(server))
			return true;

		boolean isIp = IP_REGEX.matcher(server).matches();
		if (isIp) {
			int currentIdx = server.length();
			for (int i = 0; i < 3; i++) {
				currentIdx = server.lastIndexOf('.', currentIdx - 1);
				if (isInBlockedList(server.substring(0, currentIdx + 1) + "*"))
					return true;
			}
		} else {
			int currentIdx = -1;
			while ((currentIdx = server.indexOf('.', currentIdx + 1)) != -1)
				if (isInBlockedList("*" + server.substring(currentIdx)))
					return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof SHA1BlockedServerList) {
			SHA1BlockedServerList another = (SHA1BlockedServerList) obj;
			return Objects.equals(entries, another.entries);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return entries.hashCode();
	}

	@Override
	public String toString() {
		return String.format("SHA1BlockedServerList [entries=%s]", entries);
	}

}
