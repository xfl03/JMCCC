package org.to2mbn.jmccc.mojangapi;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.SessionCredential;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.AbstractClientService;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.JSONHttpRequester;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;

public class MojangAPIImpl extends AbstractClientService implements MojangAPI {

	private MojangAPIProvider api;

	public MojangAPIImpl(JSONHttpRequester requester, MojangAPIProvider api) {
		super(requester);
		this.api = api;
	}

	@Override
	public Map<String, ServiceStatus> getServiceStatus() throws AuthenticationException {
		return invokeOperation(new Callable<Map<String, ServiceStatus>>() {

			@Override
			public Map<String, ServiceStatus> call() throws Exception {
				JSONArray response = requireJsonArray(requester.jsonGet(api.apiStatus(), null));
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
				JSONArray response = requireJsonArray(requester.jsonGet(api.nameHistory(uuid), null));

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
	public void setTexture(SessionCredential credential, UUID uuid, TextureType type, Texture texture) throws AuthenticationException {
		// TODO Auto-generated method stub

	}

	@Override
	public AccountInfo getAccountInfo(SessionCredential credential) throws AuthenticationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockedServerList getBlockedServerList() throws AuthenticationException {
		// TODO Auto-generated method stub
		return null;
	}

}
