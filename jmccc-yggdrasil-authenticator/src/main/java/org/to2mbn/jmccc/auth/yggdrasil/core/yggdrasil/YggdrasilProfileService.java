package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.ProfileService;
import org.to2mbn.jmccc.auth.yggdrasil.core.PropertiesGameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.HttpRequester;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Textures;
import org.to2mbn.jmccc.auth.yggdrasil.core.util.Base64;
import org.to2mbn.jmccc.util.IOUtils;
import org.to2mbn.jmccc.util.UUIDUtils;
import static org.to2mbn.jmccc.auth.yggdrasil.core.util.HttpUtils.*;

class YggdrasilProfileService extends AbstractYggdrasilService implements ProfileService {

	private static final Logger LOGGER = Logger.getLogger(YggdrasilProfileService.class.getCanonicalName());

	public YggdrasilProfileService(HttpRequester requester, PropertiesDeserializer propertiesDeserializer, YggdrasilAPIProvider api) {
		super(requester, propertiesDeserializer, api);
	}

	@Override
	public PropertiesGameProfile getGameProfile(final UUID profileUUID) throws AuthenticationException {
		Objects.requireNonNull(profileUUID);

		return invokeOperation(new Callable<PropertiesGameProfile>() {

			@Override
			public PropertiesGameProfile call() throws Exception {
				Map<String, Object> arguments = new HashMap<>();
				arguments.put("unsigned", "false");
				JSONObject response = nullableJsonObject(requester.request("GET", withUrlArguments(api.profile(profileUUID), arguments)));

				if (response == null) {
					return null;
				}

				Map<String, String> properties;
				JSONArray jsonProperties = response.optJSONArray("properties");
				if (jsonProperties == null) {
					properties = null;
				} else {
					properties = Collections.unmodifiableMap(propertiesDeserializer.toProperties(jsonProperties, true));
				}

				return new PropertiesGameProfile(
						UUIDUtils.toUUID(response.getString("id")),
						response.getString("name"),
						properties);
			}
		});
	}

	@Override
	public Map<TextureType, Texture> getTextures(GameProfile profile) throws AuthenticationException {
		Objects.requireNonNull(profile);

		if (!(profile instanceof PropertiesGameProfile)) {
			UUID uuid = profile.getUUID();
			profile = getGameProfile(uuid);
			if (profile == null) {
				throw new AuthenticationException("No such game profile: " + uuid);
			}
		}

		final Map<String, String> properties = ((PropertiesGameProfile) profile).getProperties();

		return invokeOperation(new Callable<Map<TextureType, Texture>>() {

			@Override
			public Map<TextureType, Texture> call() throws Exception {
				return getTextures(properties);
			}
		});
	}

	@Override
	public GameProfile lookupGameProfile(final String name) throws AuthenticationException {
		Objects.requireNonNull(name);
		return invokeOperation(new Callable<GameProfile>() {

			@Override
			public GameProfile call() throws Exception {
				return lookupGameProfile0(api.profileByUsername(name));
			}
		});
	}

	@Override
	public GameProfile lookupGameProfile(final String name, final long timestamp) throws AuthenticationException {
		Objects.requireNonNull(name);
		return invokeOperation(new Callable<GameProfile>() {

			@Override
			public GameProfile call() throws Exception {
				Map<String, Object> arguments = new HashMap<>();
				arguments.put("at", timestamp / 1000);
				return lookupGameProfile0(withUrlArguments(api.profileByUsername(name), arguments));
			}
		});
	}

	private GameProfile lookupGameProfile0(String url) throws AuthenticationException, JSONException, IOException {
		return parseGameProfile(nullableJsonObject(requester.request("GET", url)));
	}

	private Map<TextureType, Texture> getTextures(Map<String, String> properties) {
		if (properties == null) {
			return null;
		}

		String encodedTextures = properties.get("textures");
		if (encodedTextures == null) {
			return null;
		}

		JSONObject payload = IOUtils.toJson(Base64.decode(encodedTextures.toCharArray()));

		Map<TextureType, Texture> result = new EnumMap<>(TextureType.class);

		JSONObject textures = payload.getJSONObject("textures");
		for (String textureTypeName : textures.keySet()) {
			TextureType textureType;
			try {
				textureType = TextureType.valueOf(textureTypeName);
			} catch (IllegalArgumentException e) {
				LOGGER.log(Level.WARNING, "Unknown texture type: " + textureTypeName, e);
				continue;
			}
			JSONObject texureJson = textures.getJSONObject(textureTypeName);
			try {
				result.put(textureType, getTexture(texureJson));
			} catch (MalformedURLException e) {
				LOGGER.log(Level.WARNING, "Couldn't parse texture: " + texureJson, e);
			}
		}

		return Collections.unmodifiableMap(result);
	}

	private Texture getTexture(JSONObject json) throws MalformedURLException {
		String url = json.getString("url");
		Map<String, String> metadata = null;
		if (json.has("metadata")) {
			metadata = new TreeMap<>();
			JSONObject metadataJson = json.getJSONObject("metadata");
			for (Object rawtypeKey : metadataJson.keySet()) {
				String key = (String) rawtypeKey;
				String value = metadataJson.getString(key);
				metadata.put(key, value);
			}
		}
		return Textures.createTexture(url, metadata == null ? null : Collections.unmodifiableMap(metadata));
	}
}
