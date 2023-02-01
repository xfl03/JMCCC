package jmccc.microsoft.core.response;

import com.google.gson.annotations.SerializedName;

public class MicrosoftDeviceCodeResponse {
    @SerializedName("user_code")
    public String userCode;
    @SerializedName("device_code")
    public String deviceCode;
    @SerializedName("verification_uri")
    public String verificationUri;
    @SerializedName("expires_in")
    public int expiresIn;
    public int interval;
    public String message;
}
