package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.yggdrasil.core.Agent;
import org.to2mbn.jmccc.auth.yggdrasil.core.AuthenticationService;

public class YggdrasilAuthenticationServiceBuilder extends AbstractYggdrasilServiceBuilder<AuthenticationService> {

    protected Agent agent;

    public static YggdrasilAuthenticationServiceBuilder create() {
        return new YggdrasilAuthenticationServiceBuilder();
    }

    public static AuthenticationService buildDefault() {
        return create().build();
    }

    public YggdrasilAuthenticationServiceBuilder agent(Agent agent) {
        this.agent = agent;
        return this;
    }

    @Override
    public AuthenticationService build() {
        return new YggdrasilAuthenticationService(buildHttpRequester(), buildPropertiesDeserializer(), buildAPIProvider(), buildAgent());
    }

    protected Agent buildAgent() {
        return agent == null ? Agent.MINECRAFT : agent;
    }

}
