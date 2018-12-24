package com.workflow.controller;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workflow.bean.JsonGraph;
import com.workflow.service.GraphService;


@RestController
public class GraphController {

	@Autowired
	GraphService graphService;
	
	@RequestMapping(value="/save", method=RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<String> saveWorkflow(@RequestBody JsonGraph updatedGraph) {
		try {
			JSONParser parser=new JSONParser();
			JSONObject graph=null;
			//try {
				graph=updatedGraph.getJgraph();
				String name = updatedGraph.getName();
			//} catch (ParseException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
				System.out.println("SAVEEEEEEEEE:");
				System.out.println(name);
				System.out.println(graph);
			graphService.saveGraph(updatedGraph);
		}catch(Exception e) {
			return new ResponseEntity<String>("{\"message\":\"Workflow Save Error! Try Again\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<String>("{\"message\":\"Workflow Saved Successfully\"}",HttpStatus.OK);
	}
	
	@RequestMapping(value="/create", method=RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<HashMap> createWorkflow(@RequestBody String name) {
		JSONParser parser=new JSONParser();
		String iname="";
		try {
			iname=(String)((JSONObject)(parser.parse(name))).get("name");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String,Object> map = graphService.newWorkflow(iname);
		if(map!=null && (Boolean)map.get("Found")) {
			return new ResponseEntity<HashMap>(map,HttpStatus.FOUND);
		}else {
			return new ResponseEntity<HashMap>(map,HttpStatus.OK);
		}
		
	}
	
	@RequestMapping(value="/open", method=RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<HashMap> getWorkflow(@RequestBody String name) {
		JSONParser parser=new JSONParser();
		
		try {
			name=(String)((JSONObject)(parser.parse(name))).get("name");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String,Object> map = graphService.getWorkflow(name);
		if((Boolean)map.get("Found")) {
			return new ResponseEntity<HashMap>(map,HttpStatus.OK);
		}else {
			return new ResponseEntity<HashMap>(map,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@RequestMapping(value="/delete/{name}", method=RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<String> deleteWorkflow(@PathVariable("name")String name) {
		try {
			graphService.deleteGraph(name);
		}catch(Exception e) {
			return new ResponseEntity<String>("{\"message\":\"Workflow Deletion Error! Try Again\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<String>("{\"message\":\"Workflow Deleted Successfully\"}",HttpStatus.OK);
	}
	
	@RequestMapping(value="/getAll", method=RequestMethod.GET, headers = "Accept=application/json")
	public List getAllWF(){
		ArrayList<JsonGraph> graphs = (ArrayList<JsonGraph>) graphService.getWF();
		Collections.sort(graphs,Collections.reverseOrder());
		return	graphs;
	}
	
	@RequestMapping(value="/getValidLinks", method=RequestMethod.GET, headers = "Accept=application/json")
	public JSONArray getValidLinks(){
		return graphService.validLinks();
	}
}
