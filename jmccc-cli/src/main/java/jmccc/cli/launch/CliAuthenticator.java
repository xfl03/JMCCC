package jmccc.cli.launch;

import jmccc.microsoft.MicrosoftAuthenticator;
import jmccc.microsoft.entity.MicrosoftSession;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.Authenticator;

import java.io.IOException;

public class CliAuthenticator {
    public static Authenticator getMicrosoftAuthenticator() throws AuthenticationException, IOException {
        CliConfig config = CliConfig.getConfig();
        MicrosoftSession token = config.token;
        MicrosoftAuthenticator ma;
        if (token == null) {
            System.out.println("Minecraft and Microsoft token not found.");
            ma = MicrosoftAuthenticator.login(it -> System.out.println(it.message));
        } else {
            System.out.println("Existing Minecraft and Microsoft token found.");
            ma = MicrosoftAuthenticator.session(token, it -> System.out.println(it.message));
        }
        config.token = ma.getSession();
        config.writeToFile();
        return ma;
    }
}
