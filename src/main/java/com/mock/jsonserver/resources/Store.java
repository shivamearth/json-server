package com.mock.jsonserver.resources;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mock.jsonserver.exceptions.JsonServerException;
import com.mock.jsonserver.utils.StoreUtils;
import com.mock.jsonserver.utils.Utils;

@Component
public class Store {
	
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private JsonObject store;
	
	@PostConstruct
	public void initStore() {
		store = StoreUtils.readStore();
	}

	@Override
	public String toString() {
		return "Store [store=" + store + "]";
	}
	
	public ResponseEntity<JsonElement> reloadStore() {
		ResponseEntity<JsonElement> response = null;
		lock.readLock().lock();
		try {
	    	initStore();
	    	if(store != null) {
	    		response = Utils.generateResponse("Store reloaded from file.", true, HttpStatus.OK);
	    	} else {
	    		throw new Exception("Store is null.");
	    	}
	    } catch (Exception e) {
			response = Utils.generateResponse("Failed to reload store. Check with admin.", false, HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		} finally {
	        lock.readLock().unlock();
	    }
		return response;
	}
	
	public ResponseEntity<JsonElement> getEntityList(String entityType, Map<String, String> allParams) {
		ResponseEntity<JsonElement> response = null;
		lock.readLock().lock();
	    try {
	    	JsonArray entityList = null;
	    	if(allParams != null && !allParams.isEmpty()) {
	    		entityList = StoreUtils.getEntityListUsingFilter(store, entityType, allParams);
	    	} else {
	    		entityList = StoreUtils.getEntityList(store, entityType);
	    	}
	    	response = new ResponseEntity<>(entityList, HttpStatus.OK);
	    } catch (JsonServerException e) {
	    	response = e.handleException();
	    	e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Failed to get entity:" + entityType);
			response = Utils.generateResponse("Failed to get entity. Check with admin.", false, HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		} finally {
	        lock.readLock().unlock();
	    }
		return response;
	}

	public ResponseEntity<JsonElement> getEntityById(String entityType, long id) {
		ResponseEntity<JsonElement> response = null;
		lock.readLock().lock();
	    try {
	    	JsonObject entity = StoreUtils.getEntityById(store, entityType, id);
	    	response = new ResponseEntity<>(entity, HttpStatus.OK);
	    } catch (JsonServerException e) {
	    	response = e.handleException();
	    	e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Failed to get entity:" + entityType + " with id:" + id);
	    	response = Utils.generateResponse("Failed to get entity by id. Check with admin.", false, HttpStatus.INTERNAL_SERVER_ERROR);
	    	e.printStackTrace();
		} finally {
	        lock.readLock().unlock();
	    }
	    return response;
	}
	
	public ResponseEntity<JsonElement> createEntity(String entityType, Object entityParams) {
		ResponseEntity<JsonElement> response = null;
		lock.writeLock().lock();
		try {
			JsonObject entity = StoreUtils.createEntity(store, entityType, Utils.convertMapToJsonObject((Map<String, JsonElement>) entityParams));
			response = new ResponseEntity<>(entity, HttpStatus.OK);
		} catch (JsonServerException e) {
			response = e.handleException();
	    	e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Failed to create entity:" + entityType + " with params:" + entityParams);
			response = Utils.generateResponse("Failed to create entity. Check with admin.", false, HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		} finally {
			 lock.writeLock().unlock();
		}
		return response;
	}

	public ResponseEntity<JsonElement> createOrReplaceEntity(String entityType, long id, Object entityParams) {
		ResponseEntity<JsonElement> response = null;
		lock.writeLock().lock();
		try {
			JsonObject entity = StoreUtils.createOrReplaceEntity(store, entityType, id, Utils.convertMapToJsonObject((Map<String, JsonElement>) entityParams));
			response = new ResponseEntity<>(entity, HttpStatus.OK);
		} catch (JsonServerException e) {
			response = e.handleException();
	    	e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Failed to create or replace entity:" + entityType + " with id:" + id + " and params:" + entityParams);
			response = Utils.generateResponse("Failed to create or update entity. Check with admin.", false, HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		} finally {
			 lock.writeLock().unlock();
		}
		return response;
	}

	public ResponseEntity<JsonElement> updateEntity(String entityType, long id, Object entityParams) {
		ResponseEntity<JsonElement> response = null;
		lock.writeLock().lock();
		try {
			JsonObject entity = StoreUtils.updateEntity(store, entityType, id, Utils.convertMapToJsonObject((Map<String, JsonElement>) entityParams));
			response = new ResponseEntity<>(entity, HttpStatus.OK);
		} catch (JsonServerException e) {
			response = e.handleException();
	    	e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Failed to update entity:" + entityType + " with id:" + id + " and params:" + entityParams);
			response = Utils.generateResponse("Failed to update entity. Check with admin.", false, HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		} finally {
			 lock.writeLock().unlock();
		}
		return response;
	}

	public ResponseEntity<JsonElement> deleteEntity(String entityType, long id) {
		ResponseEntity<JsonElement> response = null;
		lock.writeLock().lock();
		try {
			JsonObject entity = StoreUtils.deleteEntity(store, entityType, id);
			response = new ResponseEntity<>(entity, HttpStatus.OK);
		} catch (JsonServerException e) {
			response = e.handleException();
	    	e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Failed to delete entity:" + entityType + " with id:" + id);
			response = Utils.generateResponse("Failed to delete entity. Check with admin.", false, HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		} finally {
			 lock.writeLock().unlock();
		}
		return response;
	}

	public ResponseEntity<JsonElement> getSortedEntityList(String entityType, String sortProperty, String order) {
		ResponseEntity<JsonElement> response = null;
		lock.readLock().lock();
	    try {
	    	JsonArray entityList = StoreUtils.getSortedEntityList(store, entityType, sortProperty, order);
	    	response = new ResponseEntity<>(entityList, HttpStatus.OK);
	    } catch (JsonServerException e) {
	    	response = e.handleException();
	    	e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Failed to get sorted entity:" + entityType);
			response = Utils.generateResponse("Failed to get sorted entity. Check with admin.", false, HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		} finally {
	        lock.readLock().unlock();
	    }
		return response;
	}

}
