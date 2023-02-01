package jmccc.microsoft.core.response;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class MinecraftLoginWithXboxResponse {
    public String username;
    public JsonArray roles;
    public JsonObject metadata;
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("token_type")
    public String tokenType;
}
