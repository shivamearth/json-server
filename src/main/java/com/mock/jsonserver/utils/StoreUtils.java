package com.mock.jsonserver.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mock.jsonserver.constants.JsonServerConstants;
import com.mock.jsonserver.exceptions.JsonServerException;

public class StoreUtils {

	public static final String FILEPATH = "./store.json"; 

	public static JsonObject readStore() {
		JsonObject store = null;
		try(Reader reader = Files.newBufferedReader(Paths.get(FILEPATH));) {
			Gson gson = new Gson();
			store = gson.fromJson(reader, JsonObject.class);
		} catch (IOException e) {
			System.out.println("Failed to access file");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Failed reading from store");
			e.printStackTrace();
		}
		return store;
	}

	private static void updateStore(JsonObject store) throws Exception {
		try(Writer writer = Files.newBufferedWriter(Paths.get(FILEPATH));) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setPrettyPrinting();
			Gson gson = gsonBuilder.create();
			gson.toJson(store, writer);
		} catch (IOException e) {
			System.out.println("Failed to access file");
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			System.out.println("Failed updating the store");
			e.printStackTrace();
			throw e;
		}
	}

	public static boolean isEntityPresent(JsonObject store, String entityName) {
		return store.has(entityName);
	}

	public static JsonArray getEntityList(JsonObject store, String entityType) throws Exception{
		JsonArray entityList = null;
		if(isEntityPresent(store, entityType)) {
			entityList = store.get(entityType).getAsJsonObject().get(JsonServerConstants.ENTITY_DATA).getAsJsonArray();
		} else {
			throw new JsonServerException("Entity type does not exist", HttpStatus.NOT_FOUND);
		}
		return entityList;
	}

	public static JsonObject getEntityById(JsonObject store, String entityType, long id) throws Exception {
		JsonObject finalEntity = null;
		JsonArray entityList = getEntityList(store,entityType);
		for(JsonElement entity : entityList) {
			if(entity.getAsJsonObject().get(JsonServerConstants.ID).getAsLong() == id) {
				finalEntity = entity.getAsJsonObject();
				break;
			}
		}
		if(finalEntity == null) {
			throw new JsonServerException("Failed to get entity by id", HttpStatus.NOT_FOUND);
		}
		return finalEntity;
	}

	public static int getEntityIndexById(JsonObject store, String entityType, long id) throws Exception{
		int index = -1;
		JsonArray entityList = getEntityList(store,entityType);
		for(int count = 0; count < entityList.size(); count++) {
			if(entityList.get(count).getAsJsonObject().get(JsonServerConstants.ID).getAsLong() == id) {
				index = count;
				break;
			}
		}
		return index;
	}

	public static JsonObject createEntity(JsonObject store, String entityType, JsonObject entityParams) throws Exception {
		JsonObject finalEntity = null;
		if(entityParams.has(JsonServerConstants.ID)) {
			throw new JsonServerException(JsonServerConstants.ID_IMMUTABLE_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
		}
		JsonObject entity = null;
		long id = -1;
		if(isEntityPresent(store, entityType)) {
			entity = store.get(entityType).getAsJsonObject();
			id = entity.get(JsonServerConstants.ENTITY_AUTO_INCREMENT).getAsLong();
			finalEntity = createEntityUsingParamsAndId(entityParams, id);
		} else {
			entity = new JsonObject();
			id = 1;
			finalEntity = createEntityUsingParamsAndId(entityParams, id);
			entity.add(JsonServerConstants.ENTITY_DATA, new JsonArray());
			store.add(entityType, entity);
		}
		entity.addProperty(JsonServerConstants.ENTITY_AUTO_INCREMENT, id + 1);
		entity.get(JsonServerConstants.ENTITY_DATA).getAsJsonArray().add(finalEntity);
		updateStore(store);
		return finalEntity;
	}

	public static JsonObject createEntityUsingParamsAndId(JsonObject entityParams, long id) {
		JsonObject finalEntity = new JsonObject();
		finalEntity.addProperty(JsonServerConstants.ID, id);
		Utils.addAllParams(entityParams, finalEntity);
		return finalEntity;
	}
	
	public static JsonObject createOrReplaceEntity(JsonObject store, String entityType, long id, JsonObject entityParams) throws Exception {
		JsonObject finalEntity = null;
		if(entityParams.has(JsonServerConstants.ID)) {
			throw new JsonServerException(JsonServerConstants.ID_IMMUTABLE_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
		}
		JsonArray entityList = getEntityList(store,entityType);
		if(entityList != null) {
			int index = getEntityIndexById(store, entityType, id);
			finalEntity = createEntityUsingParamsAndId(entityParams, id);
			if(index > -1) {
				entityList.set(index, finalEntity);
			} else {
				JsonObject entity = store.get(entityType).getAsJsonObject();
				entityList.add(finalEntity);
				entity.addProperty(JsonServerConstants.ENTITY_AUTO_INCREMENT, id + 1);
			}
			updateStore(store);
		}
		return finalEntity;
	}

	public static JsonObject updateEntity(JsonObject store, String entityType, long id,	JsonObject entityParams) throws Exception {
		JsonObject finalEntity = null;
		if(entityParams.has(JsonServerConstants.ID)) {
			throw new JsonServerException(JsonServerConstants.ID_IMMUTABLE_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
		}
		finalEntity = getEntityById(store, entityType, id);
		if(finalEntity != null && finalEntity.keySet().containsAll(entityParams.keySet())) {
			Utils.addAllParams(entityParams, finalEntity);
			updateStore(store);
		}
		return finalEntity;
	}

	public static JsonObject deleteEntity(JsonObject store, String entityType, long id) throws Exception {
		JsonObject deletedEntity = null;
		JsonArray entityList = getEntityList(store, entityType);
		int index = getEntityIndexById(store, entityType, id);
		if(index > -1) {
			deletedEntity = entityList.remove(index).getAsJsonObject();
			updateStore(store);
		} else {
			throw new JsonServerException("Entity does not exist", HttpStatus.NOT_FOUND);
		}
		return deletedEntity;
	}

	public static JsonArray getSortedEntityList(JsonObject store, String entityType, String sortProperty, String order) throws Exception {
		JsonArray sortedEntityList = null;
		boolean sortOrder;
		if(order.equalsIgnoreCase(JsonServerConstants.ASCENDING)) {
			sortOrder = true;
		} else if(order.equalsIgnoreCase(JsonServerConstants.DESCENDING)) {
			sortOrder = false;
		} else {
			throw new JsonServerException("Invalid value for sort order", HttpStatus.BAD_REQUEST);
		}
		JsonArray entityList = getEntityList(store, entityType);
		sortedEntityList = Utils.sortJsonArray(entityList, sortProperty, sortOrder);
		return sortedEntityList;
	}

	public static JsonArray getEntityListUsingFilter(JsonObject store, String entityType, Map<String, String> allParams) throws Exception {
		JsonArray filteredEntityList = new JsonArray();
		JsonArray entityList = getEntityList(store, entityType);
		for(JsonElement entity : entityList) {
			JsonObject jsonObjectEntity = entity.getAsJsonObject();
			if(jsonObjectEntity.keySet().containsAll(allParams.keySet())) {
				boolean allParamsMathching = true;
				for(Map.Entry<String, String> entry : allParams.entrySet()) {
					String searchValue = entry.getValue();
					if(!searchValue.equals(jsonObjectEntity.get(entry.getKey()).getAsString())) {
						allParamsMathching = false;
						break;
					}
				}
				if(allParamsMathching) {
					filteredEntityList.add(entity);
				}
			} else {
				throw new JsonServerException("All passed paramters do not match with entity paramaters.", HttpStatus.BAD_REQUEST);
			}
		}
		return filteredEntityList;
	}

}
