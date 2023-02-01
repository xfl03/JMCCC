package jmccc.microsoft.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import jmccc.microsoft.core.request.MinecraftLoginWithXboxRequest;
import jmccc.microsoft.core.request.XboxAuthenticateRequest;
import jmccc.microsoft.core.response.MicrosoftDeviceCodeResponse;
import jmccc.microsoft.core.response.MicrosoftTokenResponse;
import jmccc.microsoft.core.response.MinecraftLoginWithXboxResponse;
import jmccc.microsoft.core.response.XboxAuthenticateResponse;
import jmccc.microsoft.entity.MinecraftProfile;
import org.apache.hc.client5.http.fluent.Form;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;

public class MicrosoftAuthenticationService {
    private static final String JMCCC_CLIENT_ID = "d51b460a-0b8a-4696-af4d-690f7ba7f5b6";
    private final String clientId;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public MicrosoftAuthenticationService(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            clientId = JMCCC_CLIENT_ID;
        }
        this.clientId = clientId;
    }

    public MicrosoftDeviceCodeResponse getMicrosoftDeviceCode() throws IOException {
        return Request.post("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode")
                .bodyForm(Form.form()
                        .add("client_id", clientId)
                        .add("scope", "XboxLive.signin offline_access")
                        .build()).execute().handleResponse(new JsonResponseHander<>(MicrosoftDeviceCodeResponse.class));
    }

    public MicrosoftTokenResponse getMicrosoftToken(String deviceCode) throws IOException {
        MicrosoftTokenResponse obj = Request.post("https://login.microsoftonline.com/consumers/oauth2/v2.0/token")
                .bodyForm(Form.form()
                        .add("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                        .add("client_id", clientId)
                        .add("device_code", deviceCode)
                        .build()).execute().handleResponse(
                        new JsonResponseHander<>(MicrosoftTokenResponse.class, false));
        if (obj.error != null) {
            if ("authorization_pending".equals(obj.error)) {
                return null;
            }
            throw new IllegalStateException("Authentication failed: " + obj.error);
        }
        if (obj.accessToken == null) {

            throw new IllegalStateException("Authentication failed");
        }
        return obj;
    }

    public MicrosoftTokenResponse refreshMicrosoftToken(String microsoftRefreshToken) throws IOException {
        return Request.post("https://login.microsoftonline.com/consumers/oauth2/v2.0/token")
                .bodyForm(Form.form()
                        .add("grant_type", "refresh_token")
                        .add("client_id", clientId)
                        .add("refresh_token", microsoftRefreshToken)
                        .build()).execute().handleResponse(new JsonResponseHander<>(MicrosoftTokenResponse.class));
    }

    public XboxAuthenticateResponse getXboxUserToken(String microsoftAccessToken) throws IOException {
        XboxAuthenticateRequest req = new XboxAuthenticateRequest();
        req.relyingParty = "http://auth.xboxlive.com";
        req.properties.addProperty("AuthMethod", "RPS");
        req.properties.addProperty("SiteName", "user.auth.xboxlive.com");
        req.properties.addProperty("RpsTicket", "d=" + microsoftAccessToken);

        return Request.post("https://user.auth.xboxlive.com/user/authenticate")
                .bodyString(gson.toJson(req), ContentType.APPLICATION_JSON)
                .execute().handleResponse(new JsonResponseHander<>(XboxAuthenticateResponse.class));
    }

    public XboxAuthenticateResponse getXboxXstsToken(String xboxUserToken, String relyingParty) throws IOException {
        XboxAuthenticateRequest req = new XboxAuthenticateRequest();
        req.relyingParty = relyingParty;
        req.properties.addProperty("SandboxId", "RETAIL");
        JsonArray arr = new JsonArray();
        arr.add(xboxUserToken);
        req.properties.add("UserTokens", arr);

        return Request.post("https://xsts.auth.xboxlive.com/xsts/authorize")
                .bodyString(gson.toJson(req), ContentType.APPLICATION_JSON)
                .execute().handleResponse(new JsonResponseHander<>(XboxAuthenticateResponse.class));
    }

    public MinecraftLoginWithXboxResponse getMinecraftToken(String userHash, String xboxXstsToken) throws IOException {
        return Request.post("https://api.minecraftservices.com/authentication/login_with_xbox")
                .bodyString(gson.toJson(new MinecraftLoginWithXboxRequest(userHash, xboxXstsToken)),
                        ContentType.APPLICATION_JSON)
                .execute().handleResponse(new JsonResponseHander<>(MinecraftLoginWithXboxResponse.class));
    }

    public MinecraftProfile getMinecraftProfile(String minecraftToken) throws IOException {
        return Request.get("https://api.minecraftservices.com/minecraft/profile")
                .setHeader("Authorization", String.format("Bearer %s", minecraftToken))
                .execute().handleResponse(new JsonResponseHander<>(MinecraftProfile.class));
    }
}