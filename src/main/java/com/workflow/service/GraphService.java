package com.workflow.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.workflow.bean.JsonGraph;
import com.workflow.bean.LogicGraph;
import com.workflow.bean.Node;
import com.workflow.component.Entity;

@Service("graphService")
public class GraphService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Helper help;

	final String COLLECTION="jsonGraph";
	public HashMap extract(String name) {
		Query query=new Query();
		query.addCriteria(Criteria.where("name").is(name));
		JSONObject jtemp=(mongoTemplate.findOne(query, JsonGraph.class, COLLECTION)).getJgraph();
		org.json.JSONObject jgraph=new org.json.JSONObject(jtemp.toJSONString());
		LogicGraph lgraph=new LogicGraph();
		ArrayList<Node> nodes=new ArrayList<Node>();
		lgraph.setId((String)jgraph.get("name"));
		org.json.JSONArray nodeArray=jgraph.getJSONArray("nodeDataArray");
		org.json.JSONArray linkArray=jgraph.getJSONArray("linkDataArray");
		HashMap<String,Object> ret=new HashMap<String,Object>();
		HashMap<Integer,Node> nds=new HashMap<Integer,Node>();
		ArrayList<String> errorList=new ArrayList<>();
		for(int i=0;i<nodeArray.length();i++) {
			Node temp=new Node();
			try {
			temp.setConfig(new Entity(help.toMap((nodeArray.getJSONObject(i)).getJSONObject("config"))));
			}
			catch(JSONException e) {
				//errorList.add("Incomplete configuration at "+nodeArray.getJSONObject(i).getString("text"));
			}
			temp.setLabel(nodeArray.getJSONObject(i).getString("text"));
			if(nodeArray.getJSONObject(i).isNull("valid")) {
				temp.setValid(true);
			}else {
				temp.setValid(nodeArray.getJSONObject(i).getBoolean("valid"));
			}
			//temp.setInput(new Entity(help.toMap((nodeArray.getJSONObject(i)).getJSONObject("input"))));
			
			
			//improve logic here
			if(!nodeArray.getJSONObject(i).isNull("output")) {
				Entity o = new Entity();
				o.addKeyValue("allowed", nodeArray.getJSONObject(i).getJSONArray("output"));
				temp.setOutput(o);
			}
			
			int key=((nodeArray.getJSONObject(i))).getInt("key");
			nds.put(key, temp);
		}
		System.out.println("nds="+nds.size());
		ArrayList<Integer> from=new ArrayList<Integer>(linkArray.length());
		ArrayList<Integer> to=new ArrayList<Integer>(linkArray.length());
		for(int i=0;i<linkArray.length();i++) {
			from.add(i,((linkArray.getJSONObject(i)).getInt("from")));
			to.add(i, ((linkArray.getJSONObject(i)).getInt("to")));
		}
		int cur=-1;
		/*for(int i=0;i<nodeArray.length();i++) {
			if(((nodeArray.getJSONObject(i)).getString("text"))=="start") {
				cur=Integer.parseInt(((nodeArray.getJSONObject(i)).get("key")).toString());
				break;
			}
		}*/
		errorList.addAll(validate(to,from,nds,cur));
		if(!errorList.isEmpty()) {
			ret.put("error",true );
			ret.put("cause", errorList);
			return ret;
		}
		cur=-1;
		while(true) {
			int next;
			try {
				next=to.get(from.indexOf(cur));
			}
			catch (Exception e) {
				break;
			}
			//stopping is -2
			if(next!=-2)//nds.size()*(-1))
				nodes.add(nds.get(next));
			else {
				break;
			}
			cur=next;
		}
		lgraph.setNodes(nodes);
		ret.put("error",false );
		ret.put("nodeList", lgraph);
		System.out.println("logicGraphmap="+lgraph.toString());
		System.out.println("length="+nodes.size());
		return ret;
	}
	public void saveGraph(JsonGraph jsonGraph) {

		jsonGraph.setTimestamp(new Date());
		mongoTemplate.save(jsonGraph, COLLECTION);

	}
	
	public void deleteGraph(String name) {
		Query query=new Query();
		query.addCriteria(Criteria.where("name").is(name));
		mongoTemplate.remove(query,JsonGraph.class,COLLECTION);
	}
	
	public HashMap newWorkflow(String name) {
		HashMap<String,Object> map =new HashMap<>();
		Query query=new Query();
		query.addCriteria(Criteria.where("name").is(name));
		JsonGraph graph=new JsonGraph();
		if((graph=mongoTemplate.findOne(query,JsonGraph.class,COLLECTION))!=null) {
			map.put("Found", true);
			map.put("Graph", graph);
			return map;
		}
		else {
			JsonGraph jgraph=new JsonGraph();
			JSONParser parser=new JSONParser();

			jgraph.setName(name);
			try {
                String defaultgraph = "{\"class\": \"go.GraphLinksModel\",\"linkFromPortIdProperty\": \"fromPort\",\"linkToPortIdProperty\": \"toPort\",\"name\": \"\",\"nodeDataArray\": [{\"key\": -1,\"category\": \"Start\",\"loc\": \"175 0\",\"text\": \"Start\",\"config\":{ \"className\": \"Main\",\"name\": \"Start\",\"file\": null},\"input\": {\"url\": \"https://api.myjson.com/bins/lbzsc\"},\"output\": []},{\"key\": -2,\"category\": \"End\",\"loc\": \"175 407\",\"text\": \"Stop!\",\"config\": {\"className\": \"Main\",\"name\": \"End\",\"file\": null},\"input\": {\"url\": \"https://api.myjson.com/bins/lbzsc\"},\"output\": []}],\"linkDataArray\": []}";
                JSONObject object = (JSONObject) (parser.parse(defaultgraph));
				object.put("name", name);
				jgraph.setJgraph(object);
				jgraph.setTimestamp(new Date());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mongoTemplate.insert(jgraph, COLLECTION);

			map.put("Found", false);
			map.put("Graph", jgraph);
			return map;
		}
	}

	public HashMap getWorkflow(String name) {
		HashMap<String,Object> map=new HashMap<>();
		Query query=new Query();
		query.addCriteria(Criteria.where("name").is(name));
		JsonGraph jgraph = new JsonGraph();
		jgraph= mongoTemplate.findOne(query,JsonGraph.class,COLLECTION);
		if(jgraph!=null) {
			map.put("Found", true);
			map.put("Graph",jgraph);
			System.out.println("found");

		}else {
			map.put("Found",false);
			System.out.println("not found");
		}
		return map;
	}

	public ArrayList<String> validate(ArrayList<Integer> to, ArrayList<Integer> from, HashMap<Integer,Node> nds,int start) {
		ArrayList<String> errorList=new ArrayList<>();
		for(Map.Entry<Integer, Node> node : nds.entrySet()) {
			if(!node.getValue().isValid() && !node.getValue().getLabel().equals("Start") && !node.getValue().getLabel().equals("Stop"))
				errorList.add("Incomplete configuration at "+node.getValue().getLabel());
		}

		int cur=start;
		int count=0;
		System.out.println(nds.size());
		while(count<nds.size()-1) {
			System.out.println(cur);
			try {
				cur=to.get(from.indexOf(cur));
			}
			catch(IndexOutOfBoundsException e) {
				errorList.add("Disconnected at "+nds.get(cur).getLabel());
				break;
			}
			count++;
		}
		return errorList;
	}
	
	public List getWF(){
		Map<String,Date> WFList=new HashMap<>();
		Query query=new Query();
		query.addCriteria(Criteria.where("name").regex("^"));
		List<JsonGraph> graphlist = new ArrayList<JsonGraph>();
		graphlist.addAll(mongoTemplate.find(query,JsonGraph.class,COLLECTION));
		for(int i=0;i<graphlist.size();i++) {
			WFList.put(graphlist.get(i).getName(), graphlist.get(i).getTimestamp());
		}
		System.out.println(graphlist.toString());
		return graphlist;
	}
	
	public JSONArray validLinks(){
		Query query=new Query();
		query.addCriteria(Criteria.where("from").regex("^"));
		JSONArray graphlist = new JSONArray();
		graphlist.addAll((mongoTemplate.find(query,JSONObject.class,"validList")));
		System.out.println(graphlist.toString());
		return graphlist;
	}
}
