package com.mock.jsonserver.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.mock.jsonserver.resources.Store;
import com.mock.jsonserver.utils.Utils;

@RestController
@RequestMapping("/store")
public class StoreController {

	@Autowired
	private Store store;
	
	@RequestMapping("**")
	public ResponseEntity<JsonElement> fallback(HttpServletRequest request) {
		return Utils.generateResponse("Path: " + request.getRequestURI() + " not found", false, HttpStatus.NOT_FOUND);
	}
	
	@GetMapping("/admin/reload")
	public ResponseEntity<JsonElement> reloadStore() {
		return store.reloadStore();
	}
	
	@GetMapping("/{entityType}")
	public ResponseEntity<JsonElement> getEntityList(@PathVariable String entityType, @RequestParam Map<String,String> allParams) {
		return store.getEntityList(entityType, allParams);
	}
	
	@GetMapping("/{entityType}/{id:[\\d]+}")
	public ResponseEntity<JsonElement> getEntityById(@PathVariable String entityType, @PathVariable long id) {
		return store.getEntityById(entityType, id);
	}
	
	@PostMapping("/{entityType}")
	public ResponseEntity<JsonElement> newEntity(@PathVariable String entityType, @RequestBody Object entityParams) {
		return store.createEntity(entityType, entityParams);
	}
	
	@PutMapping("/{entityType}/{id:[\\d]+}")
	public ResponseEntity<JsonElement> replaceEntity(@PathVariable String entityType, @PathVariable long id, @RequestBody Object entityParams) {
		return store.createOrReplaceEntity(entityType, id, entityParams);
	}
	
	@PatchMapping("/{entityType}/{id:[\\d]+}")
	public ResponseEntity<JsonElement> updateEntity(@PathVariable String entityType, @PathVariable long id, @RequestBody Object entityParams) {
		return store.updateEntity(entityType, id, entityParams);
	}
	
	@DeleteMapping("/{entityType}/{id:[\\d]+}")
	public ResponseEntity<JsonElement> deleteEntity(@PathVariable String entityType, @PathVariable long id) {
		return store.deleteEntity(entityType, id);
	}
	
	@GetMapping(value="/{entityType}", params= {"_sort","_order"})
	public ResponseEntity<JsonElement> getSortedEntityList(@PathVariable String entityType, @RequestParam(name = "_sort") String sortProperty, @RequestParam(name = "_order") String order) {
		return store.getSortedEntityList(entityType, sortProperty, order);
	}
	
}
