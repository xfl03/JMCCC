package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.yggdrasil.core.io.DebugHttpRequester;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.HttpRequester;
import org.to2mbn.jmccc.util.Builder;

import java.io.*;
import java.net.Proxy;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

abstract public class AbstractYggdrasilServiceBuilder<T> implements Builder<T> {

    protected YggdrasilAPIProvider apiProvider;
    protected PublicKey sessionPublicKey;
    protected boolean useDefaultSessionPublicKey = true;
    protected Proxy proxy;
    protected boolean debug;

    protected AbstractYggdrasilServiceBuilder() {
    }

    private static PublicKey loadX509PublicKey(InputStream in) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            byteout.write(buffer, 0, read);
        }
        return loadX509PublicKey(byteout.toByteArray());
    }

    private static PublicKey loadX509PublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory;

        keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private static PublicKey loadDefaultSessionPublicKey() {
        try (InputStream in = PropertiesDeserializer.class.getResourceAsStream("/yggdrasil_session_pubkey.der")) {
            return loadX509PublicKey(in);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Couldn't load default yggdrasil session public key.", e);
        }
    }

    public AbstractYggdrasilServiceBuilder<T> apiProvider(YggdrasilAPIProvider provider) {
        this.apiProvider = provider;
        return this;
    }

    public AbstractYggdrasilServiceBuilder<T> sessionPublicKey(PublicKey sessionPublicKey) {
        this.sessionPublicKey = sessionPublicKey;
        useDefaultSessionPublicKey = false;
        return this;
    }

    public AbstractYggdrasilServiceBuilder<T> loadSessionPublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return sessionPublicKey(loadX509PublicKey(Objects.requireNonNull(encodedKey)));
    }

    public AbstractYggdrasilServiceBuilder<T> loadSessionPublicKey(InputStream in) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return sessionPublicKey(loadX509PublicKey(Objects.requireNonNull(in)));
    }

    public AbstractYggdrasilServiceBuilder<T> loadSessionPublicKey(File keyFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (InputStream in = new FileInputStream(Objects.requireNonNull(keyFile))) {
            return sessionPublicKey(loadX509PublicKey(in));
        }
    }

    public AbstractYggdrasilServiceBuilder<T> loadSessionPublicKey(String keyFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return loadSessionPublicKey(new File(Objects.requireNonNull(keyFile)));
    }

    public AbstractYggdrasilServiceBuilder<T> proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public AbstractYggdrasilServiceBuilder<T> debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    protected YggdrasilAPIProvider buildAPIProvider() {
        return apiProvider == null ? new MojangYggdrasilAPIProvider() : apiProvider;
    }

    protected HttpRequester buildHttpRequester() {
        HttpRequester requester = debug ? new DebugHttpRequester() : new HttpRequester();
        requester.setProxy(proxy);
        return requester;
    }

    protected PropertiesDeserializer buildPropertiesDeserializer() {
        return new PropertiesDeserializer(useDefaultSessionPublicKey
                ? loadDefaultSessionPublicKey()
                : sessionPublicKey);
    }

}
