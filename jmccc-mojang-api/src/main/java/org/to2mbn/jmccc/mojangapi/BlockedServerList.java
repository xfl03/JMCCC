package org.to2mbn.jmccc.mojangapi;

import java.io.Serializable;

public interface BlockedServerList extends Serializable {

	boolean isBlocked(String host);

	String[] getEntries();

}
