package com.workflow.controller;

import java.util.ArrayList;
import java.util.HashMap;

import com.workflow.component.Entity;
import com.workflow.service.Helper;
import com.workflow.service.runmanager.DefaultRunManager;
import com.workflow.service.runmanager.RunManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.workflow.bean.LogicGraph;

@RestController
public class RunManagerController {

	@Autowired
	Helper helper;

	@RequestMapping(value="/run/{WFId}", method= RequestMethod.GET)
	public ResponseEntity<HashMap> runWorkflow(@PathVariable("WFId") String WFId){

		HashMap<String,Object> map = helper.extract(WFId);
		if((boolean)map.get("error")) {
			return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
		}else {
			RunManager runManager=new DefaultRunManager();
			ArrayList<String> res=runManager.run((LogicGraph)map.get("nodeList"),(Entity)map.get("runConfig"));
			if(!res.get(0).equals("success")){
				map.put("error",true);
				map.put("cause",res);
				return new ResponseEntity<>(map,HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ResponseEntity<>(map,HttpStatus.OK);
		
	}
	
	
	
}