package com.darkyoooooo.jmccc.auth;

import java.util.Objects;

public class AuthResult {

    private String username;
    private String token;

    /**
     * Creates an AuthResult with the given username and null access token(offline authentication).
     * 
     * @param username the username
     * @throws NullPointerException if <code>username==null</code>
     */
    public AuthResult(String username) {
        this(username, null);
    }

    /**
     * Creates an AuthResult with the given username and the given access token.
     * 
     * @param username the username
     * @param token the access token, null if no token(offline authentication)
     * @throws NullPointerException if <code>username==null</code>
     */
    public AuthResult(String username, String token) {
        Objects.requireNonNull(username);
        this.username = username;
        this.token = token;
    }

    /**
     * Gets the username.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the access token, null if no token(offline authentication).
     * 
     * @return the access token, null if no token(offline authentication)
     */
    public String getToken() {
        return token;
    }

}
