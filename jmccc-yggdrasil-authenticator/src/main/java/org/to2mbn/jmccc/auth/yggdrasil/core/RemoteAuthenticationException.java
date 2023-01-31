package org.to2mbn.jmccc.auth.yggdrasil.core;

import org.to2mbn.jmccc.auth.AuthenticationException;

public class RemoteAuthenticationException extends AuthenticationException {

    private static final long serialVersionUID = 1L;
    private String remoteName;
    private String remoteMessage;
    private String remoteCause;
    public RemoteAuthenticationException(String remoteName, String remoteMessage, String remoteCause) {
        super(getExceptionMessage(remoteName, remoteMessage, remoteCause));
        this.remoteName = remoteName;
        this.remoteMessage = remoteMessage;
        this.remoteCause = remoteCause;
    }

    private static String getExceptionMessage(String remoteName, String remoteMessage, String remoteCause) {
        StringBuilder sb = new StringBuilder(remoteName);
        if (remoteMessage != null) {
            sb.append(": ");
            sb.append(remoteMessage);
        }
        if (remoteCause != null) {
            sb.append(": ");
            sb.append(remoteCause);
        }
        return sb.toString();
    }

    public String getRemoteExceptionName() {
        return remoteName;
    }

    public String getRemoteMessage() {
        return remoteMessage;
    }

    public String getRemoteCause() {
        return remoteCause;
    }

}
