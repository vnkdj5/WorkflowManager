package com.workflow.service;

import com.workflow.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.workflow.component.Component;
import com.workflow.component.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("CodeGenerationService")
public class CodeGenerationService {

	@Autowired
	Helper help;

	@Autowired
	MongoTemplate mongoTemplate;
	
	public void generateCode(LogicGraph graph) {

		Entity io=null;
		boolean anchor=false;
		ArrayList<GraphNode> flow=graph.getNodes();
		for (int i=0;i<flow.size();i++){
			flow.get(i).getComponent().init();
		}
		do{
			io=flow.get(0).getComponent().process(io);
			if(io!=null) {
				anchor=true;
			}else break;
			for (int i=1;i<flow.size();i++){
				io=flow.get(i).getComponent().process(io);
			}
		}while (anchor);
		
	}


}
