package org.to2mbn.jmccc.mojangapi;

import java.io.Serializable;
import java.util.Objects;

public class AccountInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String email;
	private String username;
	private String registerIp;
	private Long registeredAt;
	private Long passwordChangedAt;
	private Long dateOfBirth;
	private Boolean deleted;
	private Boolean blocked;
	private Boolean secured;
	private Boolean migrated;
	private Boolean emailVerified;
	private Boolean legacyUser;
	private Boolean verifiedByParent;
	private String fullName;
	private Boolean fromMigratedUser;
	private Boolean hashed;

	public AccountInfo(String id, String email, String username, String registerIp, Long registeredAt, Long passwordChangedAt, Long dateOfBirth, Boolean deleted, Boolean blocked, Boolean secured, Boolean migrated, Boolean emailVerified, Boolean legacyUser, Boolean verifiedByParent, String fullName, Boolean fromMigratedUser, Boolean hashed) {
		this.id = id;
		this.email = email;
		this.username = username;
		this.registerIp = registerIp;
		this.registeredAt = registeredAt;
		this.passwordChangedAt = passwordChangedAt;
		this.dateOfBirth = dateOfBirth;
		this.deleted = deleted;
		this.blocked = blocked;
		this.secured = secured;
		this.migrated = migrated;
		this.emailVerified = emailVerified;
		this.legacyUser = legacyUser;
		this.verifiedByParent = verifiedByParent;
		this.fullName = fullName;
		this.fromMigratedUser = fromMigratedUser;
		this.hashed = hashed;
	}

	public String getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}

	public String getRegisterIp() {
		return registerIp;
	}

	public Long getRegisteredAt() {
		return registeredAt;
	}

	public Long getPasswordChangedAt() {
		return passwordChangedAt;
	}

	public Long getDateOfBirth() {
		return dateOfBirth;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public Boolean getBlocked() {
		return blocked;
	}

	public Boolean getSecured() {
		return secured;
	}

	public Boolean getMigrated() {
		return migrated;
	}

	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public Boolean getLegacyUser() {
		return legacyUser;
	}

	public Boolean getVerifiedByParent() {
		return verifiedByParent;
	}

	public String getFullName() {
		return fullName;
	}

	public Boolean getFromMigratedUser() {
		return fromMigratedUser;
	}

	public Boolean getHashed() {
		return hashed;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, email, username, registerIp, registeredAt, passwordChangedAt, dateOfBirth, deleted, blocked, secured, migrated, emailVerified, legacyUser, verifiedByParent, fullName, fromMigratedUser, hashed);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof AccountInfo) {
			AccountInfo another = (AccountInfo) obj;
			return Objects.equals(id, another.id)
					&& Objects.equals(email, another.email)
					&& Objects.equals(username, another.username)
					&& Objects.equals(registerIp, another.registerIp)
					&& Objects.equals(registeredAt, another.registeredAt)
					&& Objects.equals(passwordChangedAt, another.passwordChangedAt)
					&& Objects.equals(dateOfBirth, another.dateOfBirth)
					&& Objects.equals(deleted, another.deleted)
					&& Objects.equals(blocked, another.blocked)
					&& Objects.equals(secured, another.secured)
					&& Objects.equals(migrated, another.migrated)
					&& Objects.equals(emailVerified, another.emailVerified)
					&& Objects.equals(legacyUser, another.legacyUser)
					&& Objects.equals(verifiedByParent, another.verifiedByParent)
					&& Objects.equals(fullName, another.fullName)
					&& Objects.equals(fromMigratedUser, another.fromMigratedUser)
					&& Objects.equals(hashed, another.hashed);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("AccountInfo [id=%s, email=%s, username=%s, registerIp=%s, registeredAt=%s, passwordChangedAt=%s, dateOfBirth=%s, deleted=%s, blocked=%s, secured=%s, migrated=%s, emailVerified=%s, legacyUser=%s, verifiedByParent=%s, fullName=%s, fromMigratedUser=%s, hashed=%s]", id, email, username, registerIp, registeredAt, passwordChangedAt, dateOfBirth, deleted, blocked, secured, migrated, emailVerified, legacyUser, verifiedByParent, fullName, fromMigratedUser, hashed);
	}

}
