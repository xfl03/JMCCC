package jmccc.microsoft.entity;

import java.io.Serializable;

/**
 * Token used in authenticator, which can be serialized and save to file
 */
public class MicrosoftSession implements Serializable {
    /**
     * Microsoft access token
     */
    public String microsoftAccessToken;
    /**
     * Microsoft refresh token
     */
    public String microsoftRefreshToken;
    /**
     * Minecraft access token
     */
    public String minecraftAccessToken;
    /**
     * Xbox User ID
     */
    public String xboxUserId;
}
