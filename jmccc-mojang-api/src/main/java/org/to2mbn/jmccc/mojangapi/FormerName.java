package org.to2mbn.jmccc.mojangapi;

import java.util.Objects;

/**
 * Describes one of a player's former names.
 * 
 * @author yushijinhun
 */
public class FormerName {

	private String name;
	private Long changedToAt;

	public FormerName(String name, Long changedToAt) {
		this.name = Objects.requireNonNull(name);
		this.changedToAt = changedToAt;
	}

	/**
	 * Returns the username.
	 * 
	 * @return the username, cannot be null
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns when the player changed its name to this, or null if the time is
	 * unknown (for example, this is the player's first username).
	 * 
	 * @return when the player changed its name to this, can be null
	 */
	public Long getChangedToAt() {
		return changedToAt;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, changedToAt);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof FormerName) {
			FormerName another = (FormerName) obj;
			return Objects.equals(name, another.name)
					&& Objects.equals(changedToAt, another.changedToAt);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s(changedToAt=%s)", name, changedToAt);
	}

}
