package jmccc.microsoft.entity;

public class MicrosoftVerification {
    /**
     * A short string shown to the user that's used to identify the session on a secondary device.
     */
    public String userCode;
    /**
     * The URI the user should go to with the userCode in order to sign in.
     */
    public String verificationUri;
    /**
     * A human-readable string with instructions for the user.
     */
    public String message;

    public MicrosoftVerification(String userCode, String verificationUri, String message) {
        this.userCode = userCode;
        this.verificationUri = verificationUri;
        this.message = message;
    }
}
