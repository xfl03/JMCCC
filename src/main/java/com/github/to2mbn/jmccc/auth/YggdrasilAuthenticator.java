package com.github.to2mbn.jmccc.auth;

/**
 * @deprecated this class will be removed in future versions, please use YggdrasilPasswordAuthenticator
 */
@Deprecated
public class YggdrasilAuthenticator extends YggdrasilPasswordAuthenticator {

	/**
	 * Creates a YggdrasilPasswordAuthenticator.
	 * 
	 * @param email the email
	 * @param password the password
	 * @param characterSelector call when selecting character, null if use the default character
	 * @throws NullPointerException if <code>email==null||password==null</code>
	 */
	public YggdrasilAuthenticator(String email, String password, YggdrasilCharacterSelector characterSelector) {
		super(email, password, characterSelector);
	}

	/**
	 * Creates a YggdrasilPasswordAuthenticator.
	 * 
	 * @param email the email
	 * @param password the password
	 * @throws NullPointerException if <code>email==null||password==null</code>
	 */
	public YggdrasilAuthenticator(String email, String password) {
		super(email, password);
	}

}
