package com.workflow.controller;

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
	public ResponseEntity<String> checkDatabaseConnection(@RequestBody HashMap data){
		String userName = (String) data.get("name");
		String database = (String) data.get("database");
		String p = (String) data.get("password");
		char[] password = p.toCharArray();
		String url = (String) data.get("url");
		if(help.checkMongoConnection(url,userName, password, database)) {
			return new ResponseEntity<String>("{\"message\":\"Connection Established\"}",HttpStatus.OK);
		}else {
			return new ResponseEntity<String>("{\"message\":\"Connection Problem\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
}
