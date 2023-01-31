package org.to2mbn.jmccc.auth.yggdrasil;

import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;

/**
 * Provides an available yggdrasil session as a credential.
 *
 * @author yushijinhun
 */
public interface SessionCredential {

    /**
     * Returns an available yggdrasil session.
     *
     * @return an available yggdrasil session
     * @throws AuthenticationException if an exception occurs during
     *                                 authentication
     */
    Session session() throws AuthenticationException;

}
