package com.workflow.controller;

import java.util.HashMap;

import com.workflow.service.Helper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.workflow.bean.LogicGraph;
import com.workflow.service.CodeGenerationService;
import com.workflow.service.GraphService;

@RestController
public class CodeGenerationController {
	
	@Autowired
	CodeGenerationService codeGenerationService;

	@Autowired
	Helper helper;
	
	@RequestMapping(value="/run/{WFId}", method= RequestMethod.GET)
	public ResponseEntity<HashMap> runWorkflow(@PathVariable("WFId") String WFId){

		HashMap<String,Object> map = helper.extract(WFId);
		System.out.println(map.toString());
		if((boolean)map.get("error")) {
			return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
		}else {
			codeGenerationService.generateCode((LogicGraph)map.get("nodeList"));
		}
		
		System.out.println(map.toString());
		
		return new ResponseEntity<>(map,HttpStatus.OK);
		
	}
	
	
	
}
