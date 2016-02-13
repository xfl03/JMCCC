package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.PlayerTextures;
import org.to2mbn.jmccc.auth.yggdrasil.core.ProfileService;
import org.to2mbn.jmccc.auth.yggdrasil.core.PropertiesGameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.util.Base64;
import org.to2mbn.jmccc.util.UUIDUtils;

public class YggdrasilProfileService extends YggdrasilService implements ProfileService {

	private static final long serialVersionUID = 1L;

	public YggdrasilProfileService() {
		super();
	}

	public YggdrasilProfileService(SignaturedPropertiesDeserializer propertiesDeserializer, YggdrasilAPIProvider api) {
		super(propertiesDeserializer, api);
	}

	@Override
	public PropertiesGameProfile getGameProfile(UUID profileUUID) throws AuthenticationException {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("unsigned", "false");
		JSONObject response;
		try {
			response = getRequester().jsonGet(getApi().profile(profileUUID), arguments);
		} catch (JSONException | IOException e) {
			throw newRequestFailedException(e);
		}
		checkResponse(response);

		Map<String, String> properties;
		try {
			try {
				properties = getPropertiesDeserializer().toProperties(response.optJSONArray("properties"), true);
			} catch (GeneralSecurityException e) {
				throw newSignatureException(e);
			}
			return new PropertiesGameProfile(UUIDUtils.toUUID(response.getString("id")), response.getString("name"), properties);
		} catch (JSONException e) {
			throw newResponseFormatException(e);
		}
	}

	@Override
	public PlayerTextures getTextures(GameProfile profile) throws AuthenticationException {
		if (!(profile instanceof PropertiesGameProfile)) {
			profile = getGameProfile(profile.getUUID());
		}
		return getTextures(((PropertiesGameProfile) profile).getProperties());
	}

	private PlayerTextures getTextures(Map<String, String> properties) throws AuthenticationException {
		String encodedTextures = properties.get("textures");
		if (encodedTextures == null) {
			return null;
		}

		JSONObject response;
		try {
			response = new JSONObject(new String(Base64.decode(encodedTextures.toCharArray()), "UTF-8"));
		} catch (JSONException | UnsupportedEncodingException e) {
			throw newResponseFormatException(e);
		}

		try {
			JSONObject textures = response.getJSONObject("textures");
			return new PlayerTextures(getTexture(textures.optJSONObject("SKIN")), getTexture(textures.optJSONObject("CAPE")));
		} catch (JSONException e) {
			throw newResponseFormatException(e);
		}
	}

	private Texture getTexture(JSONObject json) {
		if (json == null) {
			return null;
		}

		String url = json.getString("url");
		Map<String, String> metadata = null;
		if (json.has("metadata")) {
			metadata = new HashMap<>();
			JSONObject metadataJson = json.getJSONObject("metadata");
			for (Object rawtypeKey : metadataJson.keySet()) {
				String key = (String) rawtypeKey;
				String value = metadataJson.getString(key);
				metadata.put(key, value);
			}
		}
		return new Texture(url, metadata);
	}
}
