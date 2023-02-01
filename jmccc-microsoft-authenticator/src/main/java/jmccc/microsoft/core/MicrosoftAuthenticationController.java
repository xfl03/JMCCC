package jmccc.microsoft.core;

import jmccc.microsoft.core.response.MicrosoftDeviceCodeResponse;
import jmccc.microsoft.core.response.MicrosoftTokenResponse;
import jmccc.microsoft.core.response.MinecraftLoginWithXboxResponse;
import jmccc.microsoft.core.response.XboxAuthenticateResponse;
import jmccc.microsoft.entity.MicrosoftSession;
import jmccc.microsoft.entity.MicrosoftVerification;
import jmccc.microsoft.entity.MinecraftProfile;
import org.to2mbn.jmccc.auth.AuthenticationException;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MicrosoftAuthenticationController {
    private final MicrosoftAuthenticationService service;

    public MicrosoftAuthenticationController(String clientId) {
        service = new MicrosoftAuthenticationService(clientId);
    }

    public MicrosoftSession getMicrosoftToken(Consumer<MicrosoftVerification> callback) throws AuthenticationException {
        try {
            //Request authentication
            MicrosoftDeviceCodeResponse codeRes = service.getMicrosoftDeviceCode();
            Objects.requireNonNull(codeRes);
            Objects.requireNonNull(codeRes.deviceCode);
            Objects.requireNonNull(codeRes.userCode);
            MicrosoftVerification verification = new MicrosoftVerification(codeRes.userCode, codeRes.verificationUri, codeRes.message);
            callback.accept(verification);

            //Get authentication result
            long expireTime = TimeUnit.SECONDS.toMillis(codeRes.expiresIn) + System.currentTimeMillis();
            MicrosoftTokenResponse tokenRes = null;
            while (System.currentTimeMillis() < expireTime) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(codeRes.interval));
                tokenRes = service.getMicrosoftToken(codeRes.deviceCode);
                if (tokenRes != null) {
                    break;
                }
            }
            Objects.requireNonNull(tokenRes);
            Objects.requireNonNull(tokenRes.accessToken);
            MicrosoftSession token = new MicrosoftSession();
            token.microsoftAccessToken = tokenRes.accessToken;
            token.microsoftRefreshToken = tokenRes.refreshToken;
            return token;
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    public MicrosoftSession refreshMicrosoftToken(MicrosoftSession session) throws AuthenticationException {
        try {
            Objects.requireNonNull(session);
            Objects.requireNonNull(session.microsoftRefreshToken);
            MicrosoftTokenResponse res = service.refreshMicrosoftToken(session.microsoftRefreshToken);
            MicrosoftSession token = new MicrosoftSession();
            token.microsoftAccessToken = res.accessToken;
            token.microsoftRefreshToken = res.refreshToken;
            return token;
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    public MicrosoftSession getMinecraftToken(MicrosoftSession session) throws AuthenticationException {
        try {
            Objects.requireNonNull(session);
            Objects.requireNonNull(session.microsoftAccessToken);
            XboxAuthenticateResponse xboxUserToken = service.getXboxUserToken(session.microsoftAccessToken);
            XboxAuthenticateResponse xboxXstsMcToken = service.getXboxXstsToken(xboxUserToken.token,"rp://api.minecraftservices.com/");
            XboxAuthenticateResponse xboxXstsToken = service.getXboxXstsToken(xboxUserToken.token, "http://xboxlive.com");
            session.xboxUserId = xboxXstsToken.displayClaims.get("xui").getAsJsonArray().get(0).getAsJsonObject().get("xid").getAsString();

            String userHash = xboxXstsMcToken.displayClaims.get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
            MinecraftLoginWithXboxResponse minecraftToken = service.getMinecraftToken(userHash, xboxXstsMcToken.token);
            session.minecraftAccessToken = minecraftToken.accessToken;
            return session;
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    public MinecraftProfile getMinecraftProfile(MicrosoftSession session) throws AuthenticationException {
        try {
            Objects.requireNonNull(session);
            Objects.requireNonNull(session.minecraftAccessToken);
            return service.getMinecraftProfile(session.minecraftAccessToken);
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }
}
