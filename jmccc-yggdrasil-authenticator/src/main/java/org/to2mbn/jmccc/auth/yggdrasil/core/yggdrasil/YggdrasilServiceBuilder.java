package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;
import org.to2mbn.jmccc.auth.yggdrasil.core.Agent;
import org.to2mbn.jmccc.auth.yggdrasil.core.AuthenticationService;
import org.to2mbn.jmccc.auth.yggdrasil.core.ProfileService;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.JSONHttpRequester;

public class YggdrasilServiceBuilder {

	public static YggdrasilServiceBuilder create() {
		return new YggdrasilServiceBuilder();
	}

	public static AuthenticationService defaultAuthenticationService() {
		return new YggdrasilServiceBuilder().buildAuthenticationService();
	}

	public static ProfileService defaultProfileService() {
		return new YggdrasilServiceBuilder().buildProfileService();
	}

	private YggdrasilAPIProvider apiProvider;
	private PublicKey sessionPublicKey;
	private boolean useDefaultSessionPublicKey = true;
	private Agent agent;
	private Proxy proxy;

	protected YggdrasilServiceBuilder() {
	}

	public YggdrasilServiceBuilder setAPIProvider(YggdrasilAPIProvider provider) {
		this.apiProvider = provider;
		return this;
	}

	public YggdrasilServiceBuilder setSessionPublicKey(PublicKey sessionPublicKey) {
		this.sessionPublicKey = sessionPublicKey;
		useDefaultSessionPublicKey = false;
		return this;
	}

	public YggdrasilServiceBuilder loadSessionPublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		Objects.requireNonNull(encodedKey);
		return setSessionPublicKey(loadX509PublicKey(encodedKey));
	}

	public YggdrasilServiceBuilder loadSessionPublicKey(InputStream in) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		Objects.requireNonNull(in);
		return setSessionPublicKey(loadX509PublicKey(in));
	}

	public YggdrasilServiceBuilder loadSessionPublicKey(File keyFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		Objects.requireNonNull(keyFile);
		try (InputStream in = new FileInputStream(keyFile)) {
			return setSessionPublicKey(loadX509PublicKey(in));
		}
	}

	public YggdrasilServiceBuilder loadSessionPublicKey(String keyFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		Objects.requireNonNull(keyFile);
		return loadSessionPublicKey(new File(keyFile));
	}

	public YggdrasilServiceBuilder setAgent(Agent agent) {
		this.agent = agent;
		return this;
	}

	public YggdrasilServiceBuilder setProxy(Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	public AuthenticationService buildAuthenticationService() {
		return new YggdrasilAuthenticationService(buildJSONHttpRequester(), buildPropertiesDeserializer(), buildAPIProvider(), buildAgent());
	}

	public ProfileService buildProfileService() {
		return new YggdrasilProfileService(buildJSONHttpRequester(), buildPropertiesDeserializer(), buildAPIProvider());
	}

	private Agent buildAgent() {
		return agent == null ? Agent.MINECRAFT : agent;
	}

	private YggdrasilAPIProvider buildAPIProvider() {
		return apiProvider == null ? new MojangYggdrasilAPIProvider() : apiProvider;
	}

	private JSONHttpRequester buildJSONHttpRequester() {
		return new JSONHttpRequester(proxy == null ? Proxy.NO_PROXY : proxy);
	}

	private PropertiesDeserializer buildPropertiesDeserializer() {
		return new PropertiesDeserializer(useDefaultSessionPublicKey
				? loadDefaultSessionPublicKey()
				: sessionPublicKey);
	}

	private static PublicKey loadDefaultSessionPublicKey() {
		try (InputStream in = PropertiesDeserializer.class.getResourceAsStream("/yggdrasil_session_pubkey.der")) {
			return loadX509PublicKey(in);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IllegalStateException("Cannot load default yggdrasil session public key.", e);
		}
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

}
