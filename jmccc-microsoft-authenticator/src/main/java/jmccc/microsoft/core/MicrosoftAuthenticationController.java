package jmccc.microsoft.core;

import jmccc.microsoft.core.response.MicrosoftDeviceCodeResponse;
import jmccc.microsoft.core.response.MicrosoftTokenResponse;
import jmccc.microsoft.core.response.MinecraftLoginWithXboxResponse;
import jmccc.microsoft.core.response.XboxAuthenticateResponse;
import jmccc.microsoft.entity.AuthenticationToken;
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

    public AuthenticationToken getMicrosoftToken(Consumer<MicrosoftVerification> callback) throws AuthenticationException {
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
            AuthenticationToken token = new AuthenticationToken();
            token.microsoftAccessToken = tokenRes.accessToken;
            token.microsoftRefreshToken = tokenRes.refreshToken;
            return token;
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    public AuthenticationToken refreshMicrosoftToken(AuthenticationToken microsoftToken) throws AuthenticationException {
        try {
            Objects.requireNonNull(microsoftToken);
            Objects.requireNonNull(microsoftToken.microsoftRefreshToken);
            MicrosoftTokenResponse res = service.refreshMicrosoftToken(microsoftToken.microsoftRefreshToken);
            AuthenticationToken token = new AuthenticationToken();
            token.microsoftAccessToken = res.accessToken;
            token.microsoftRefreshToken = res.refreshToken;
            return token;
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    public AuthenticationToken getMinecraftToken(AuthenticationToken microsoftToken) throws AuthenticationException {
        try {
            Objects.requireNonNull(microsoftToken);
            Objects.requireNonNull(microsoftToken.microsoftAccessToken);
            XboxAuthenticateResponse xboxUserToken = service.getXboxUserToken(microsoftToken.microsoftAccessToken);
            XboxAuthenticateResponse xboxXstsToken = service.getXboxXstsToken(xboxUserToken.token);

            String userHash = xboxXstsToken.displayClaims.get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
            MinecraftLoginWithXboxResponse minecraftToken = service.getMinecraftToken(userHash, xboxXstsToken.token);
            microsoftToken.minecraftAccessToken = minecraftToken.accessToken;
            return microsoftToken;
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    public MinecraftProfile getMinecraftProfile(AuthenticationToken minecraftToken) throws AuthenticationException {
        try {
            Objects.requireNonNull(minecraftToken);
            Objects.requireNonNull(minecraftToken.minecraftAccessToken);
            return service.getMinecraftProfile(minecraftToken.minecraftAccessToken);
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }
}
