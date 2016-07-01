package org.to2mbn.jmccc.mojangapi;

import static org.to2mbn.jmccc.auth.yggdrasil.core.io.HttpUtils.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mojangapi.io.MultipartBuilder;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.SessionCredential;
import org.to2mbn.jmccc.auth.yggdrasil.core.RemoteAuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.AbstractClientService;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.HttpRequester;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.URLTexture;
import org.to2mbn.jmccc.util.IOUtils;

class MojangAPIImpl extends AbstractClientService implements MojangAPI {

	private MojangAPIProvider api;

	public MojangAPIImpl(HttpRequester requester, MojangAPIProvider api) {
		super(requester);
		this.api = api;
	}

	@Override
	public Map<String, ServiceStatus> getServiceStatus() throws AuthenticationException {
		return invokeOperation(new Callable<Map<String, ServiceStatus>>() {

			@Override
			public Map<String, ServiceStatus> call() throws Exception {
				JSONArray response = requireJsonArray(requester.request("GET", api.apiStatus()));
				Map<String, ServiceStatus> result = new TreeMap<>();
				for (Object rawEntry : response) {
					JSONObject serviceEntry = (JSONObject) rawEntry;
					for (String service : serviceEntry.keySet()) {
						ServiceStatus status = ServiceStatus.valueOf(serviceEntry.getString(service).toUpperCase());
						result.put(service, status);
					}
				}
				return Collections.unmodifiableMap(result);
			}
		});
	}

	@Override
	public FormerName[] getNameHistory(final UUID uuid) throws AuthenticationException {
		Objects.requireNonNull(uuid);

		return invokeOperation(new Callable<FormerName[]>() {

			@Override
			public FormerName[] call() throws Exception {
				JSONArray response = requireJsonArray(requester.request("GET", api.nameHistory(uuid)));

				FormerName[] names = new FormerName[response.length()];
				for (int i = 0; i < names.length; i++) {
					JSONObject entry = response.getJSONObject(i);
					String name = entry.getString("name");
					Long changedToAt = entry.has("changedToAt") ? entry.getLong("changedToAt") : null;
					names[i] = new FormerName(name, changedToAt);
				}
				return names;
			}
		});
	}

	@Override
	public void setTexture(final SessionCredential credential, final UUID uuid, final TextureType type, final Texture texture) throws AuthenticationException {
		Objects.requireNonNull(credential);
		Objects.requireNonNull(uuid);
		Objects.requireNonNull(type);

		invokeOperation(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				String url = api.texture(uuid, type);
				Map<String, String> headers = getAuthorizationHeaders(credential);

				if (texture == null) {
					// reset texture
					requireEmpty(requester.request("DELETE", url, headers));
				} else {

					if (texture instanceof URLTexture) {
						try {
							updateTexture((URLTexture) texture, url, headers);

						} catch (RemoteAuthenticationException exUpdate) {
							if ("IllegalArgumentException".equals(exUpdate.getRemoteExceptionName())) {
								// maybe cannot fetch texture from the url
								try {
									uploadTexture(texture, url, headers);
								} catch (Exception exUpload) {
									exUpload.addSuppressed(exUpdate);
									throw exUpload;
								}
							} else {
								throw exUpdate;
							}
						}

					} else {
						// upload texture
						uploadTexture(texture, url, headers);
					}
				}

				return null;
			}
		});
	}

	private void updateTexture(URLTexture texture, String url, Map<String, String> headers) throws AuthenticationException, JSONException, IOException {
		Map<String, Object> form = new HashMap<>();

		Map<String, String> metadata = texture.getMetadata();
		if (metadata != null) {
			form.putAll(metadata);
		}

		form.put("url", texture.getURL());

		requireEmpty(requester.requestWithPayload("POST", url, encodeForm(form), "application/x-www-form-urlencoded", headers));
	}

	private void uploadTexture(Texture texture, String url, Map<String, String> headers) throws AuthenticationException, JSONException, IOException {
		byte[] image;
		try (InputStream in = texture.openStream()) {
			image = IOUtils.toByteArray(in);
		}

		MultipartBuilder multipart = new MultipartBuilder();

		Map<String, String> metadata = texture.getMetadata();
		if (metadata != null) {
			for (Entry<String, String> property : metadata.entrySet()) {
				multipart.disposition("name", property.getKey())
						.content(property.getValue().getBytes("UTF-8"));
			}
		}

		multipart.disposition("name", "file")
				.header("Content-Type", "image/png")
				.content(image);

		requireEmpty(requester.requestWithPayload("PUT", url, multipart.finish(), multipart.getContentType(), headers));
	}

	@Override
	public AccountInfo getAccountInfo(final SessionCredential credential) throws AuthenticationException {
		return invokeOperation(new Callable<AccountInfo>() {

			@Override
			public AccountInfo call() throws Exception {
				JSONObject response = requireJsonObject(requester.request("GET", api.userInfo(), getAuthorizationHeaders(credential)));

				String id = response.has("id") ? response.getString("id") : null;
				String email = response.has("email") ? response.getString("email") : null;
				String username = response.has("username") ? response.getString("username") : null;
				String registerIp = response.has("registerIp") ? response.getString("registerIp") : null;
				Long registeredAt = response.has("registeredAt") ? response.getLong("registeredAt") : null;
				Long passwordChangedAt = response.has("passwordChangedAt") ? response.getLong("passwordChangedAt") : null;
				Long dateOfBirth = response.has("dateOfBirth") ? response.getLong("dateOfBirth") : null;
				Boolean deleted = response.has("deleted") ? response.getBoolean("deleted") : null;
				Boolean blocked = response.has("blocked") ? response.getBoolean("blocked") : null;
				Boolean secured = response.has("secured") ? response.getBoolean("secured") : null;
				Boolean migrated = response.has("migrated") ? response.getBoolean("migrated") : null;
				Boolean emailVerified = response.has("emailVerified") ? response.getBoolean("emailVerified") : null;
				Boolean legacyUser = response.has("legacyUser") ? response.getBoolean("legacyUser") : null;
				Boolean verifiedByParent = response.has("verifiedByParent") ? response.getBoolean("verifiedByParent") : null;
				String fullName = response.has("fullName") ? response.getString("fullName") : null;
				Boolean fromMigratedUser = response.has("fromMigratedUser") ? response.getBoolean("fromMigratedUser") : null;
				Boolean hashed = response.has("hashed") ? response.getBoolean("hashed") : null;

				return new AccountInfo(id, email, username, registerIp, registeredAt, passwordChangedAt, dateOfBirth, deleted, blocked, secured, migrated, emailVerified, legacyUser, verifiedByParent, fullName, fromMigratedUser, hashed);
			}
		});
	}

	@Override
	public BlockedServerList getBlockedServerList() throws AuthenticationException {
		return invokeOperation(new Callable<BlockedServerList>() {

			@Override
			public BlockedServerList call() throws Exception {
				String response = requester.request("GET", api.blockedServers());
				if (response.trim().startsWith("{")) {
					requireJsonObject(response);
					throw new AuthenticationException("Illegal response: " + response);
				}

				String[] entries = response.split("\n");

				Set<String> entriesSet = new LinkedHashSet<>();
				for (String entry : entries) {
					entry = entry.trim();
					if (entry.isEmpty()) {
						continue;
					}
					entriesSet.add(entry);
				}

				return new SHA1BlockedServerList(entriesSet);
			}
		});
	}

	@Override
	public SalesStatistics querySales(final String... metricKeys) throws AuthenticationException {
		return invokeOperation(new Callable<SalesStatistics>() {

			@Override
			public SalesStatistics call() throws Exception {
				JSONObject request = new JSONObject();
				request.put("metricKeys", metricKeys);

				JSONObject response = requireJsonObject(requester.requestWithPayload("POST", api.salesStatistics(), request, CONTENT_TYPE_JSON));

				long total = response.optLong("total", -1);
				long last24h = response.optLong("last24h", -1);
				long saleVelocityPerSeconds = response.optLong("saleVelocityPerSeconds", -1);

				return new SalesStatistics(total, last24h, saleVelocityPerSeconds);
			}
		});
	}

	private Map<String, String> getAuthorizationHeaders(SessionCredential credential) throws AuthenticationException {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer: " + credential.session().getAccessToken());
		return headers;
	}

}
