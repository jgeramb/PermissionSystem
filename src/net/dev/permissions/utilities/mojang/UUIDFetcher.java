package net.dev.permissions.utilities.mojang; 

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.mojang.util.UUIDTypeAdapter;

import lib.org.json.JSONArray;
import lib.org.json.JSONObject;

public class UUIDFetcher {

	private final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
	private final String NAME_URL = "https://api.mojang.com/user/profiles/%s/names";
	private Map<String, UUID> uuidCache = new HashMap<String, UUID>();
	private Map<UUID, String> nameCache = new HashMap<UUID, String>();

	public UUID getUUID(String name) {
		return getUUIDAt(name, System.currentTimeMillis());
	}

	public UUID getUUIDAt(String name, long timestamp) {
		name = name.toLowerCase();

		//Check for cached unique id
		if (uuidCache.containsKey(name))
			return uuidCache.get(name);

		try {
			//Open connection
			HttpURLConnection connection = (HttpURLConnection) new URL(String.format(UUID_URL, name, timestamp / 1000)).openConnection();
			connection.setReadTimeout(5000);

			//Parse response
			try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				StringBuilder response = new StringBuilder();
				String line;
				
				while((line = bufferedReader.readLine()) != null)
					response.append(line);
				
				JSONObject data = new JSONObject(response.toString());
				UUID uniqueId = UUID.fromString(data.getString("id").replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
				
				//Cache data
				uuidCache.put(name, uniqueId);
				nameCache.put(uniqueId, data.getString("name"));

				return uniqueId;
			}
		} catch (Exception ex) {
			return UUID.fromString("11110000-1111-0000-1111-000011110000");
		}
	}

	public String getName(UUID uuid) {
		//Check for cached name
		if (nameCache.containsKey(uuid))
			return nameCache.get(uuid);

		try {
			//Open connection
			HttpURLConnection connection = (HttpURLConnection) new URL(String.format(NAME_URL, UUIDTypeAdapter.fromUUID(uuid))).openConnection();
			connection.setReadTimeout(5000);

			//Parse response
			try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				StringBuilder response = new StringBuilder();
				String line;
				
				while((line = bufferedReader.readLine()) != null)
					response.append(line);
				
				JSONArray data = new JSONArray(response.toString());
				JSONObject currentNameData = (JSONObject) data.get(data.length() - 1);
				
				//Cache data
				uuidCache.put(currentNameData.getString("name").toLowerCase(), uuid);
				nameCache.put(uuid, currentNameData.getString("name"));
	
				return currentNameData.getString("name");
			}
		} catch (Exception ignore) {
		}

		return null;
	}

}