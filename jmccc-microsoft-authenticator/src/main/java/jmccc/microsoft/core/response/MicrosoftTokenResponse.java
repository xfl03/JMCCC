package jmccc.microsoft.core.response;

import com.google.gson.annotations.SerializedName;

public class MicrosoftTokenResponse {
    @SerializedName("token_type")
    public String tokenType;
    public String scope;
    @SerializedName("expires_in")
    public int expiresIn;
    @SerializedName("ext_expires_in")
    public int extExpiresIn;
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("refresh_token")
    public String refreshToken;

    public String error;
}
