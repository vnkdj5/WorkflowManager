package com.workflow.controller;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workflow.bean.LogicGraph;
import com.workflow.service.CodeGenerationService;
import com.workflow.service.GraphService;

@RestController
public class CodeGenerationController {
	
	@Autowired
	CodeGenerationService codeGenerator;
	
	@Autowired
	GraphService graphService;
	
	@RequestMapping(value="/run", method= RequestMethod.POST)
	public ResponseEntity<HashMap> runWorkflow(@RequestBody String name){
		JSONParser parser=new JSONParser();
		String iname="";
		try {
			iname=(String)((JSONObject)(parser.parse(name))).get("name");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String,Object> map = graphService.extract(iname);
		System.out.println(map.toString());
		if((boolean)map.get("error")) {
			return new ResponseEntity<HashMap>(map,HttpStatus.INTERNAL_SERVER_ERROR);
		}else {
			codeGenerator.generateCode((LogicGraph)map.get("nodeList"));
		}
		
		System.out.println(map.toString());
		
		return new ResponseEntity<HashMap>(map,HttpStatus.OK);
		
	}
	
	
	
}
