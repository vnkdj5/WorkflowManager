package com.workflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.workflow.bean.LogicGraph;
import com.workflow.component.Component;
import com.workflow.component.Entity;

@Service("CodeGenerationService")
public class CodeGenerationService {

	@Autowired
	Helper help;
	
	public void generateCode(LogicGraph graph) {
		
		
		
		
		
		
		
		
		//CsvReader csv = new CsvReader();
		Component csv = (Component) help.getObjectByClassName("CsvReader");
		csv.init(graph.getNodes().get(0).getConfig(),null,null);
		
		//MongoWriter mw = new MongoWriter();
		Component mw = (Component) help.getObjectByClassName("MongoWriter");
		mw.init(graph.getNodes().get(1).getConfig(),null,null);
		
		Entity out;
		
		while((out = csv.process(null))!=null) {
			mw.process(out);
		}
		
	}
	
}
