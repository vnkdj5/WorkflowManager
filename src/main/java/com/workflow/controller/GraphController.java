package com.workflow.controller;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.workflow.bean.GraphLink;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    @RequestMapping(value = "/save/{WFId}", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> saveWorkflow(@RequestBody List<JSONObject> updateList, @PathVariable("WFId") String WFId) {
		try {
            Iterator it = updateList.iterator();
            while (it.hasNext()) {
                graphService.saveGraph(WFId, (JSONObject) it.next());
            }
		}catch(Exception e) {
			e.printStackTrace();
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
    public ResponseEntity<HashMap> getWorkflow(@RequestBody String WFId) {
		JSONParser parser=new JSONParser();
		
		try {
            WFId = (String) ((JSONObject) (parser.parse(WFId))).get("WFId");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        HashMap<String, Object> map = graphService.getWorkflow(WFId);
		if((Boolean)map.get("Found")) {
			return new ResponseEntity<>(map,HttpStatus.OK);
		}else {
			return new ResponseEntity<>(map,HttpStatus.EXPECTATION_FAILED);
		}
	}

    @RequestMapping(value = "/delete/{WFId}", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> deleteWorkflow(@PathVariable("WFId") String id) {
		try {
            graphService.deleteGraph(id);
		}catch(Exception e) {
			return new ResponseEntity<String>("{\"message\":\"Workflow Deletion Error! Try Again\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<String>("{\"message\":\"Workflow Deleted Successfully\"}",HttpStatus.OK);
	}
	
	@RequestMapping(value="/getAll", method=RequestMethod.GET, headers = "Accept=application/json")
	public List getAllWF(){
        ArrayList<JSONObject> graphs = (ArrayList<JSONObject>) graphService.getWF();
        //Collections.sort(graphs,Collections.reverseOrder());
		return	graphs;
	}
	
	@RequestMapping(value="/getValidLinks", method=RequestMethod.GET, headers = "Accept=application/json")
	public JSONArray getValidLinks(){
		return graphService.validLinks();
	}

	@Autowired
	MongoTemplate mongoTemplate;

	@RequestMapping(value="/addLink", method=RequestMethod.GET, headers = "Accept=application/json")
	public String addLink(@RequestParam("to")String to, @RequestParam("from")String from){
		GraphLink link = new GraphLink(from,to);
		mongoTemplate.insert(link,"validLinks");
    	return "success";
	}
}
