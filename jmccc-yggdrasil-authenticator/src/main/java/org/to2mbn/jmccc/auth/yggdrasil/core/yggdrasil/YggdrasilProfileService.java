package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.ProfileService;
import org.to2mbn.jmccc.auth.yggdrasil.core.PropertiesGameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.TextureType;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.JSONHttpRequester;
import org.to2mbn.jmccc.auth.yggdrasil.core.util.Base64;
import org.to2mbn.jmccc.util.IOUtils;
import org.to2mbn.jmccc.util.UUIDUtils;

class YggdrasilProfileService extends AbstractYggdrasilService implements ProfileService {

	private static final Logger LOGGER = Logger.getLogger(YggdrasilProfileService.class.getCanonicalName());

	public YggdrasilProfileService(JSONHttpRequester requester, PropertiesDeserializer propertiesDeserializer, YggdrasilAPIProvider api) {
		super(requester, propertiesDeserializer, api);
	}

	@Override
	public PropertiesGameProfile getGameProfile(UUID profileUUID) throws AuthenticationException {
		Objects.requireNonNull(profileUUID);

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("unsigned", "false");
		JSONObject response;
		try {
			response = (JSONObject) getRequester().jsonGet(getApi().profile(profileUUID), arguments);
		} catch (JSONException | IOException e) {
			throw new AuthenticationException(e);
		}
		requireNonEmptyResponse(response);

		try {
			Map<String, String> properties;

			JSONArray jsonProperties = response.optJSONArray("properties");
			if (jsonProperties == null) {
				properties = null;
			} else {
				try {
					properties = Collections.unmodifiableMap(getPropertiesDeserializer().toProperties(jsonProperties, true));
				} catch (GeneralSecurityException e) {
					throw new AuthenticationException("Invalid signature", e);
				}
			}

			return new PropertiesGameProfile(
					UUIDUtils.toUUID(response.getString("id")),
					response.getString("name"),
					properties);
		} catch (JSONException e) {
			throw new AuthenticationException("Couldn't parse response: " + response, e);
		}
	}

	@Override
	public Map<TextureType, Texture> getTextures(GameProfile profile) throws AuthenticationException {
		Objects.requireNonNull(profile);

		if (!(profile instanceof PropertiesGameProfile)) {
			profile = getGameProfile(profile.getUUID());
		}
		return getTextures(((PropertiesGameProfile) profile).getProperties());
	}

	@Override
	public UUID lookupUUIDByName(String playerName) throws AuthenticationException {
		Objects.requireNonNull(playerName);

		JSONArray request = new JSONArray();
		request.put(playerName);
		Object rawResponse;
		try {
			rawResponse = getRequester().jsonPost(getApi().profileLookup(), null, request);
		} catch (JSONException | IOException e) {
			throw new AuthenticationException(e);
		}
		try {
			if (rawResponse instanceof JSONObject) {
				requireNonEmptyResponse((JSONObject) rawResponse);
				throw new JSONException("Response should be a json array");
			}
			JSONArray response = (JSONArray) rawResponse;
			switch (response.length()) {
				case 0:
					// no profile is in the response
					return null;

				case 1:
					return UUIDUtils.toUUID(response.getJSONObject(0).getString("id"));

				default:
					throw new AuthenticationException("We only queried one player's profile, but the server sent us more than one profile: " + response);
			}
		} catch (JSONException e) {
			throw new AuthenticationException("Couldn't parse response: " + rawResponse, e);
		}
	}

	private Map<TextureType, Texture> getTextures(Map<String, String> properties) throws AuthenticationException {
		if (properties == null) {
			return null;
		}

		String encodedTextures = properties.get("textures");
		if (encodedTextures == null) {
			return null;
		}

		JSONObject payload;
		try {
			payload = IOUtils.toJson(Base64.decode(encodedTextures.toCharArray()));
		} catch (JSONException e) {
			throw new AuthenticationException("Couldn't decode texture payload: " + encodedTextures, e);
		}

		Map<TextureType, Texture> result = new EnumMap<>(TextureType.class);

		try {
			JSONObject textures = payload.getJSONObject("textures");
			for (String textureTypeName : textures.keySet()) {
				TextureType textureType;
				try {
					textureType = TextureType.valueOf(textureTypeName);
				} catch (IllegalArgumentException e) {
					LOGGER.log(Level.WARNING, "Unknown texture type: " + textureTypeName, e);
					continue;
				}
				result.put(textureType, getTexture(textures.getJSONObject(textureTypeName)));
			}
		} catch (JSONException e) {
			throw new AuthenticationException("Couldn't parse texture payload: " + payload, e);
		}

		return Collections.unmodifiableMap(result);
	}

	private Texture getTexture(JSONObject json) {
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
		return new Texture(url, metadata == null ? null : Collections.unmodifiableMap(metadata));
	}
}
