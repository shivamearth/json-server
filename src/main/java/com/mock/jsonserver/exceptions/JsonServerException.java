package com.mock.jsonserver.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.JsonElement;
import com.mock.jsonserver.utils.Utils;

public class JsonServerException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private HttpStatus status;

	public HttpStatus getStatus() {
		return status;
	}

	public JsonServerException(String message, HttpStatus status) {
		super(message);
		if(status != null) {
			this.status = status;
		} else {
			this.status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}
	
	public ResponseEntity<JsonElement> handleException(){
		return Utils.generateResponse(getMessage(), false, status);
	}
}
