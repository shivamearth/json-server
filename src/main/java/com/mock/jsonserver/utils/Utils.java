package com.mock.jsonserver.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Utils {

	public static ResponseEntity<JsonElement> generateResponse(String message, Boolean success, HttpStatus status) {
		JsonObject responseBody = new JsonObject();
		responseBody.addProperty("message", message);
		responseBody.addProperty("success", success);
		return new ResponseEntity<>(responseBody, status);
	}
	
	public static void addAllParams(JsonObject source, JsonObject target) {
		for(Map.Entry<String, JsonElement> entry : source.entrySet()) {
			target.add(entry.getKey(), entry.getValue());
		}
	}

	public static JsonObject convertMapToJsonObject(Map<String, JsonElement> map) {
		Gson gson = new Gson();
		return gson.fromJson(gson.toJson(map), JsonObject.class);
	}
	
	public static JsonArray sortJsonArray(JsonArray jsonArray, String sortBy, boolean sortOrder) throws Exception{
		JsonArray sortedJsonArray = new JsonArray();

		List<JsonObject> jsonValues = new ArrayList<JsonObject>(jsonArray.size());
		for(JsonElement entity : jsonArray) {
			jsonValues.add(entity.getAsJsonObject());
		}

		final String KEY_NAME = sortBy;
		final Boolean SORT_ORDER = sortOrder;
		Collections.sort( jsonValues, new Comparator<JsonObject>() {

			@Override
			public int compare(JsonObject a, JsonObject b){
				String valA = a.get(KEY_NAME).getAsString();
				String valB = b.get(KEY_NAME).getAsString();

				if (SORT_ORDER) {
					return valA.compareTo(valB);
				} else {
					return -valA.compareTo(valB);
				}
			}
		});

		for(JsonElement entity : jsonValues) {
			sortedJsonArray.add(entity);
		}
		return sortedJsonArray;
	}
}
