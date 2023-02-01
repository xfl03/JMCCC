package jmccc.microsoft.core.request;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class XboxAuthenticateRequest {
    @SerializedName("Properties")
    public JsonObject properties = new JsonObject();
    @SerializedName("RelyingParty")
    public String relyingParty;
    @SerializedName("TokenType")
    public String tokenType = "JWT";
}
