package org.to2mbn.jmccc.mojangapi;

import java.io.Serializable;
import java.util.Objects;

/**
 * The information of a Mojang account.
 * <p>
 * Each property of {@code AccountInfo} can be null (if the property is not
 * specified in Mojang server's response).
 * 
 * @author yushijinhun
 */
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

	/**
	 * @return Account Identifier?
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Email attached to account
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return Username of account, with migrated accounts this is the same as
	 *         email
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return IP used to register account
	 */
	public String getRegisterIp() {
		return registerIp;
	}

	/**
	 * @return Epoch timestamp in ms of date the Mojang account was registered
	 */
	public Long getRegisteredAt() {
		return registeredAt;
	}

	/**
	 * @return Epoch timestamp of time password was last changed
	 */
	public Long getPasswordChangedAt() {
		return passwordChangedAt;
	}

	/**
	 * @return Epoch timestamp of date of birth for this Mojang Account
	 */
	public Long getDateOfBirth() {
		return dateOfBirth;
	}

	/**
	 * @return Unknown, probably whether account has been deleted or not
	 */
	public Boolean getDeleted() {
		return deleted;
	}

	/**
	 * @return Unknown, probably whether account has been blocked or not
	 */
	public Boolean getBlocked() {
		return blocked;
	}

	/**
	 * @return Whether security questions are enabled on this Mojang Account
	 */
	public Boolean getSecured() {
		return secured;
	}

	/**
	 * @return Whether the account has been migrated, if the account was made
	 *         after Mojang Accounts were mandatory for new accounts this is set
	 *         to false
	 */
	public Boolean getMigrated() {
		return migrated;
	}

	/**
	 * @return Whether the email attached to the account is verified
	 */
	public Boolean getEmailVerified() {
		return emailVerified;
	}

	/**
	 * @return Whether the account is a legacy user?
	 */
	public Boolean getLegacyUser() {
		return legacyUser;
	}

	/**
	 * @return Whether the account has been verified by parent, is set to false
	 *         if no parent verification was needed
	 */
	public Boolean getVerifiedByParent() {
		return verifiedByParent;
	}

	/**
	 * @return Full name attached to Mojang account, can be an empty string
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @return Not sure, probably related to migrated?
	 */
	public Boolean getFromMigratedUser() {
		return fromMigratedUser;
	}

	/**
	 * @return Unsure, seems to be set to false?
	 */
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
