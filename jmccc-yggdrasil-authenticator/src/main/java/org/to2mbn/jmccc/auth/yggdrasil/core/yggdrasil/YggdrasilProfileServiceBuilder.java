package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.yggdrasil.core.ProfileService;

public class YggdrasilProfileServiceBuilder extends AbstractYggdrasilServiceBuilder<ProfileService> {

    public static YggdrasilProfileServiceBuilder create() {
        return new YggdrasilProfileServiceBuilder();
    }

    public static ProfileService buildDefault() {
        return create().build();
    }

    @Override
    public ProfileService build() {
        return new YggdrasilProfileService(buildHttpRequester(), buildPropertiesDeserializer(), buildAPIProvider());
    }

}
