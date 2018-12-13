package com.workflow.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.workflow.service.Helper;

@RestController
public class DBConnectionTestingController {
	
	@Autowired
	Helper help;
	
	@RequestMapping(value="checkConnection",method=RequestMethod.POST)
	public ResponseEntity<ArrayList<String>> checkDatabaseConnection(@RequestBody HashMap data){
		String userName = (String) data.get("name");
		String database = (String) data.get("database");
		String collection = (String) data.get("collection");
		String p = (String) data.get("password");
		char[] password = p.toCharArray();
		String url = (String) data.get("url");
		ArrayList<String> response = new ArrayList<>();
		HashMap<String,Boolean> result = help.checkMongoConnection(url,userName, password, database,collection);
		if(result.get("Connection")) {
			response.add("Connection Established");
			if(result.get("Collection")) {
				response.add("Collection exists");
			}else {
				response.add("Collection not exists");
			}
			return new ResponseEntity<ArrayList<String>>(response,HttpStatus.OK);
		}else {
			return new ResponseEntity<ArrayList<String>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
}
