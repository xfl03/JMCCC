package jmccc.cli.launch;

import jmccc.microsoft.MicrosoftAuthenticator;
import jmccc.microsoft.entity.AuthenticationToken;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.Authenticator;

import java.io.IOException;

public class CliAuthenticator {
    public static Authenticator getMicrosoftAuthenticator() throws AuthenticationException, IOException {
        CliConfig config = CliConfig.getConfig();
        AuthenticationToken token = config.token;
        MicrosoftAuthenticator ma;
        if (token == null) {
            System.out.println("Minecraft and Microsoft token not found.");
            ma = MicrosoftAuthenticator.login(it -> System.out.println(it.message));
        } else {
            System.out.println("Existing Minecraft and Microsoft token found.");
            ma = MicrosoftAuthenticator.token(token, it -> System.out.println(it.message));
        }
        config.token = ma.getAuthenticationToken();
        config.writeToFile();
        return ma;
    }
}
