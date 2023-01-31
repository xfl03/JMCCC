package org.to2mbn.jmccc.auth.yggdrasil.test;

import org.to2mbn.jmccc.auth.yggdrasil.CharacterSelector;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;

public class MockCharacterSelector implements CharacterSelector {

    private String toSelect;

    public MockCharacterSelector(String toSelect) {
        this.toSelect = toSelect;
    }

    @Override
    public GameProfile select(GameProfile[] availableProfiles) {
        if (toSelect == null) {
            return null;
        } else {
            for (GameProfile p : availableProfiles) {
                if (toSelect.equals(p.getName())) {
                    return p;
                }
            }
            throw new AssertionError("The profile specified is not existing");
        }
    }

}
