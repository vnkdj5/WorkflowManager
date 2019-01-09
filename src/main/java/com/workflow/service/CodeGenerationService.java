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

	public HashMap<String,Object> extract(String WFId){
		HashMap<String,Object> ret= new HashMap<>();
		Query query=new Query();
		query.addCriteria(Criteria.where("id").is(WFId));
		WFGraph graph=mongoTemplate.findOne(query, WFGraph.class, "WFGraph");
		List<GraphNode> nodeList=graph.getNodes();
		List<GraphLink> links=graph.getLinks();
		ArrayList<GraphNode> nodeArray=new ArrayList<>();
		String currentNode="Start";
		GraphNode previous=null;
		ArrayList<String> errorList=new ArrayList<>();
		boolean error=false;
		int flag=0;
		for (int i = 0; i < links.size(); i++) {
			if (links.get(i).getFrom().equals(currentNode)) {
				currentNode = links.get(i).getTo();
				previous=new GraphNode();
				previous.setName(currentNode);
				previous.setComponent(null);
				break;
			}
		}
		if(currentNode.equals("Start")){
			errorList.add("No starting point specified.");
			error=true;
			currentNode=null;
		}
		while(!currentNode.equals("End") && currentNode!=null) {
			GraphNode temp=null;
			for (int i = 0; i < nodeList.size(); i++) {
				if (nodeList.get(i).getCId().equals(currentNode)) {
					temp=nodeList.get(i);
					nodeArray.add(temp);
					break;
				}
			}
			if(temp==null){
				errorList.add("Component missing after "+previous.getName()+".");
				error=true;
				break;
			}
			for (int i = 0; i < links.size(); i++) {
				if (links.get(i).getFrom().equals(currentNode)) {
					flag=1;
					previous=temp;
					currentNode = links.get(i).getTo();
					break;
				}
			}
			if(flag==0){
				errorList.add("Disconnected at: "+temp.getName());
				currentNode=null;
				error=true;
			}
		}
		for(int i=0;i<nodeList.size();i++) {
			GraphNode node=nodeList.get(i);
			if(!node.getComponent().isValid() && !node.getCId().equals("Start") && !node.getCId().equals("End")) {
				errorList.add("Incomplete configuration at " + node.getName());
				error=true;
			}
		}
		LogicGraph lgraph=new LogicGraph();
		lgraph.setId(WFId);
		lgraph.setNodes(nodeArray);
		ret.put("error",error );
		if(error)
			ret.put("cause", errorList);
		ret.put("nodeList", lgraph);
		return ret;
	}
}
