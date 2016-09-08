package com.simplexservers.minecraft.mojangapi;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * A utility wrapper for Mojang's REST API.
 */
public class MojangAPI {

	/**
	 * The timeout, in milliseconds, for Mojang API requests.
	 */
	private static final int TIMEOUT_SECONDS = 3000;

	/**
	 * Makes a request to the Mojang API to get the UUID for the player with the given username.
	 * Returns null if the player UUID could not be found.
	 *
	 * @param username The username of player to get the UUID for.
	 * @return The Minecraft UUID of the player with the given username.
	 * @throws MojangAPIException If an error occurred with the API request.
	 */
	public static UUID requestUUIDForUsername(String username) throws MojangAPIException {
		if (!MinecraftUtil.isValidUsername(username)) {
			throw new IllegalArgumentException("Invalid username '" + username + "'.");
		}

		// Make the JSON payload
		JSONArray payload = new JSONArray();
		payload.add(username);

		JSONArray json = (JSONArray) makeJSONPostRequest("https://api.mojang.com/profiles/minecraft", payload.toJSONString());

		if (json == null) {
			// No player with the username exists
			return null;
		}

		// Get the ID
		if (json.size() == 1) {
			JSONObject jsonPlayer = (JSONObject) json.get(0);
			if (jsonPlayer.containsKey("id")) {
				return MinecraftUtil.uuidFromString((String) jsonPlayer.get("id"), false);
			}
		}

		return null;
	}

	/**
	 * Makes a request to the Mojang API to get the latest username for the player with the given UUID.
	 * Returns null if the player username could not be found.
	 *
	 * @param uuid The UUID of the player to get the username for.
	 * @return The latest username of the player with the given UUID.
	 * @throws MojangAPIException If an error occurred with the API request.
	 */
	public static String requestUsernameForUUID(UUID uuid) throws MojangAPIException {
		// Gets the name history for the player rather than getting the profile since the later has a strict rate limit
		JSONArray nameChanges = (JSONArray) makeJSONGetRequest("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");

		if (nameChanges == null) {
			// No player with the UUID exists
			return null;
		}

		// Get the latest username
		String latestUsername = null;
		long latestChangeTime = -1;

		for (Object nameChangeObj : nameChanges) {
			JSONObject nameChange = (JSONObject) nameChangeObj;
			long changeTime = (long) nameChange.getOrDefault("changedToAt", 0L);
			if (changeTime > latestChangeTime) {
				latestChangeTime = changeTime;
				latestUsername = (String) nameChange.get("name");
			}
		}

		return latestUsername; // null if username isn't found
	}

	/**
	 * Makes a HTTP GET request and parses the JSON response.
	 * Returns null if a 204 response is received.
	 *
	 * @param url The URL to make the connection to.
	 * @return The parsed JSON response.
	 * @throws MojangAPIException If an error occurred making the request to the URL.
	 */
	private static Object makeJSONGetRequest(String url) throws MojangAPIException {
		HttpURLConnection apiConn = null;
		try {
			apiConn = (HttpURLConnection) new URL(url).openConnection();
			apiConn.setConnectTimeout(TIMEOUT_SECONDS);
			apiConn.setReadTimeout(TIMEOUT_SECONDS);

			if (apiConn.getResponseCode() == 204) {
				// No matching player exists
				return null;
			}

			if (apiConn.getResponseCode() != 200) {
				// Something else went wrong
				throw new MojangAPIException("Bad response code received: " + apiConn.getResponseCode() + ". " + apiConn.getResponseMessage());
			}

			// Read the response
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(apiConn.getInputStream()))) {
				// Parse the response as JSON
				try {
					JSONParser parser = new JSONParser();
					return parser.parse(reader);
				} catch (ParseException e) {
					throw new MojangAPIException("Could not properly parse the payload received from the Mojang API.", e);
				}
			} catch (IOException e) {
				throw new MojangAPIException("Could not read the payload from the HTTP request.", e);
			}
		} catch (IOException e) {
			throw new MojangAPIException("Could not complete a request to the Mojang API.", e);
		} finally {
			if (apiConn != null) {
				apiConn.disconnect();
			}
		}
	}

	/**
	 * Makes a HTTP POST request and parses the JSON response.
	 * Returns null if a 204 repsonse is received.
	 *
	 * @param url The URL to make the connection to.
	 * @param payload The payload to send to the server.
	 * @return The parsed JSON response.
	 * @throws MojangAPIException If an error occurred making the request to the URL.
	 */
	private static Object makeJSONPostRequest(String url, String payload) throws MojangAPIException {
		HttpURLConnection apiConn = null;
		try {
			apiConn = (HttpURLConnection) new URL(url).openConnection();
			apiConn.setConnectTimeout(TIMEOUT_SECONDS);
			apiConn.setReadTimeout(TIMEOUT_SECONDS);
			apiConn.setRequestMethod("POST");
			apiConn.setDoOutput(true);
			apiConn.setRequestProperty("Content-Type", "application/json");

			// Send the payload
			try (Writer writer = new OutputStreamWriter(apiConn.getOutputStream(), "UTF-8")) {
				writer.write(payload);
			}

			if (apiConn.getResponseCode() == 204) {
				// No matching player exists
				return null;
			}

			if (apiConn.getResponseCode() != 200) {
				// Something else went wrong
				throw new MojangAPIException("Bad response code was received: " + apiConn.getResponseCode() + ". " + apiConn.getResponseMessage());
			}

			// Read the response
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(apiConn.getInputStream()))) {
				// Parse the response as JSON
				try {
					JSONParser parser = new JSONParser();
					return parser.parse(reader);
				} catch (ParseException e) {
					throw new MojangAPIException("Could not properly parse the payload received from the Mojang API.", e);
				}
			} catch (IOException e) {
				throw new MojangAPIException("Could not read the payload from the HTTP request.", e);
			}
		} catch (IOException e) {
			throw new MojangAPIException("Could not complete a request to the Mojang API.", e);
		} finally {
			if (apiConn != null) {
				apiConn.disconnect();
			}
		}
	}

}
