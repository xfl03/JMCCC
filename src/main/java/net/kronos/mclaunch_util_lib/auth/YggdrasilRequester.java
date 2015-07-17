package net.kronos.mclaunch_util_lib.auth;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kronos.mclaunch_util_lib.auth.model.YggdrasilAgent;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilAuthenticateReq;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilAuthenticateRes;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilError;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilInvalidateReq;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilRefreshReq;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilRefreshRes;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilSignoutReq;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilValidateReq;

/**
 * @author Kronos
 */
public class YggdrasilRequester
{
	private static boolean debug;
	
	//===================Darkyoooooo Edited=======================
	public static boolean isDebug() {
		return debug;
	}
	
	public static void setDebug(boolean flag) {
		debug = flag;
	}
	//============================================================
	
	/**
	 * Create a new requester
	 */
	public YggdrasilRequester() {
	}
	
	/**
	 * Authenticate an user with his username and password and a previously acquired accessToken
	 * 
	 * @param agent
	 * @param username
	 * @param password
	 * @param clientToken
	 * 
	 * @return The appropriated response object
	 * 
	 * @throws IOException
	 * @throws YggdrasilError
	 */
	public YggdrasilAuthenticateRes authenticate(YggdrasilAgent agent, String username, String password, String clientToken) throws IOException, YggdrasilError {
		YggdrasilAuthenticateReq req = new YggdrasilAuthenticateReq();
		req.setAgent(agent);
		req.setUsername(username);
		req.setPassword(password);
		req.setClientToken(clientToken);
		
		return request(req, "authenticate", YggdrasilAuthenticateRes.class);
	}
	
	/**
	 * Authenticate an user with his username and password
	 * 
	 * @param agent
	 * @param username
	 * @param password
	 * 
	 * @return The appropriated response object
	 * 
	 * @throws IOException
	 * @throws YggdrasilError
	 */
	public YggdrasilAuthenticateRes authenticate(YggdrasilAgent agent, String username, String password) throws IOException, YggdrasilError {
		YggdrasilAuthenticateReq req = new YggdrasilAuthenticateReq();
		req.setAgent(agent);
		req.setUsername(username);
		req.setPassword(password);
		
		return request(req, "authenticate", YggdrasilAuthenticateRes.class);
	}
	
	/**
	 * Refresh an access token, provided access token gets invalidated and a new one is returned
	 * 
	 * @param accessToken
	 * @param clientToken
	 * 
	 * @return The appropriated response object
	 * 
	 * @throws IOException
	 * @throws YggdrasilError
	 */
	public YggdrasilRefreshRes refresh(String accessToken, String clientToken) throws IOException, YggdrasilError {
		YggdrasilRefreshReq req = new YggdrasilRefreshReq();
		req.setAccessToken(accessToken);
		req.setClientToken(clientToken);
		
		return request(req, "refresh", YggdrasilRefreshRes.class);
	}
	
	/**
	 * Check if an access token is valid
	 * 
	 * @param accessToken
	 * 
	 * @throws IOException
	 * @throws YggdrasilError
	 */
	public void validate(String accessToken) throws IOException, YggdrasilError {
		YggdrasilValidateReq req = new YggdrasilValidateReq();
		req.setAccessToken(accessToken);
		
		request(req, "validate", null);
	}
	
	/**
	 * Invalidate all the access tokens of the provided account (username+password)
	 * 
	 * @param username
	 * @param password
	 * 
	 * @throws IOException
	 * @throws YggdrasilError
	 */
	public void signout(String username, String password) throws IOException, YggdrasilError {
		YggdrasilSignoutReq req = new YggdrasilSignoutReq();
		req.setPassword(password);
		req.setUsername(username);
		
		request(req, "signout", null);
	}
	
	/**
	 * Invalidate an access token
	 * 
	 * @param accessToken
	 * @param clientToken
	 * 
	 * @throws IOException
	 * @throws YggdrasilError
	 */
	public void invalidate(String accessToken, String clientToken) throws IOException, YggdrasilError {
		YggdrasilInvalidateReq req = new YggdrasilInvalidateReq();
		req.setAccessToken(accessToken);
		req.setClientToken(clientToken);
		
		request(req, "invalidate", null);
	}
	
	/**
	 * Internal use only, build request to the yggdrasil authentication server
	 * 
	 * @param request
	 * @param route
	 * @param responseClass
	 * 
	 * @return
	 *
	 * @throws IOException
	 * @throws YggdrasilError
	 */
	private <T> T request(Object data, String route, Class<T> responseClass) throws IOException, YggdrasilError {
		long currentTime = System.currentTimeMillis();
		Gson gson = new Gson();
		
		// Serialize the request in json
		String request = gson.toJson(data);
		
		if(debug) {
			System.out.println(String.format("[%s] request:  %s", currentTime, request));
		}
		
		// Url and data
		URL url = new URL("https://authserver.mojang.com/" + route);
		byte[] postDataByte = request.getBytes();
		
		// Open the connection and specify the headers
		HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Content-Length", Integer.toString(postDataByte.length));
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);

		// Write the data
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.write(postDataByte);
		wr.flush();
		wr.close();
		
		// True if no problem, false otherwise
		boolean status = String.valueOf(connection.getResponseCode()).startsWith("2");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(status ? connection.getInputStream() : connection.getErrorStream()));
		String response = br.readLine();
		
		if(debug) {
			System.out.println(String.format("[%s] response: %s", currentTime, response));
		}
		
		if(responseClass != null && (response == null || response.isEmpty())) {
			throw new IOException("Empty response");
		}
		
		if(status) {
			return responseClass == null ? null : gson.fromJson(response, responseClass);
		}
		else {
			throw new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(response, YggdrasilError.class);
		}
	}
}
