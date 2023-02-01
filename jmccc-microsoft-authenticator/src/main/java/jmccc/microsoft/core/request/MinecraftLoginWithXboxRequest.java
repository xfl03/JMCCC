package jmccc.microsoft.core.request;

public class MinecraftLoginWithXboxRequest {
    public String identityToken;

    public MinecraftLoginWithXboxRequest(String userHash, String xboxXstsToken) {
        this.identityToken = String.format("XBL3.0 x=%s;%s", userHash, xboxXstsToken);
    }
}
