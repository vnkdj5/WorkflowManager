package com.workflow.controller;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.CSVReader;
import com.workflow.component.Entity;
import com.workflow.service.Helper;

@RestController
public class ComponentController {
	
	@Autowired
	Helper helper;
	
	@RequestMapping(value="/getConfig/{component_name}", method= RequestMethod.GET)
	public ResponseEntity<String> getConfig(@PathVariable("component_name") String componentName){
		
		Entity config = helper.getConfig(componentName);
		
		
		//add config model to the response and send back the entity.
		if(config==null) {
			return new ResponseEntity<String>("",HttpStatus.INTERNAL_SERVER_ERROR);
		}else {
			return new ResponseEntity<String>(config.getObjectByName("FORM").toString(),HttpStatus.OK);
		}
		
		
		
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/components", method=RequestMethod.GET)
	public ResponseEntity<String> getAllComponents() throws ParseException{
		
		//addition of @component annotation and reading all component classes
		JSONArray array = new JSONArray();
		String[] components = new String[] {"Start","CsvReader","Mapper","MongoWriter","End"};
		
		for(String c : components) {
			JSONObject obj = new JSONObject();
			obj.put("category", c);
			obj.put("text", c);
            obj.put("key", c);
			array.add(obj);
		}
		//"{\"pallets\":"+array.toString()+"}"
		//new JSONObject().put("pallete", array).toString().replaceAll("\\\\","")
		return new ResponseEntity<String>("{\"pallete\":"+array.toString().replaceAll("\\\\","")+"}",HttpStatus.OK);
	}
	
}
