package jmccc.microsoft.core.response;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class XboxAuthenticateResponse {
    @SerializedName("IssueInstant")
    public String issueInstant;
    @SerializedName("NotAfter")
    public String notAfter;
    @SerializedName("Token")
    public String token;
    @SerializedName("DisplayClaims")
    public JsonObject displayClaims;
    @SerializedName("expires_in")
    public int expiresIn;
}
