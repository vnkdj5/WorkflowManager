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

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;
import com.workflow.bean.GraphLink;
import com.workflow.bean.GraphNode;
import com.workflow.bean.JsonGraph;
import com.workflow.bean.LogicGraph;
import com.workflow.bean.Node;
import com.workflow.bean.WFGraph;
import com.workflow.component.Component;
import com.workflow.component.Entity;

@Service("graphService")
public class GraphService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Helper help;

    final String COLLECTION = "WFGraph";
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
		HashMap<String,Node> nds=new HashMap<String,Node>();
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

			String key=((nodeArray.getJSONObject(i))).getString("key");
			nds.put(key, temp);
		}
		System.out.println("nds="+nds.size());
		ArrayList<String> from=new ArrayList<String>(linkArray.length());
		ArrayList<String> to=new ArrayList<String>(linkArray.length());
		for(int i=0;i<linkArray.length();i++) {
			from.add(i,((linkArray.getJSONObject(i)).getString("from")));
			to.add(i, ((linkArray.getJSONObject(i)).getString("to")));
		}
		String cur="Start";
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
		cur="Start";
		while(true) {
			String next;
			try {
				next=to.get(from.indexOf(cur));
			}
			catch (Exception e) {
				break;
			}
			//stopping is -2
			if(!next.equals("End"))//nds.size()*(-1))
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

    public String saveGraph(String WFId, JSONObject update) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(WFId));
        WFGraph graph = (WFGraph) mongoTemplate.find(query, WFGraph.class, COLLECTION);
        switch ((String) update.get("type")) {
            case "nodeAdd": {
                String componentId = (String) update.get("CId");
                String componentName = (String) update.get("name");
                String componentCategory = (String) update.get("category");
                double x = (double) update.get("x");
                double y = (double) update.get("y");
                Component newNode = help.getObjectByClassName(componentCategory);
                GraphNode graphNode = new GraphNode(componentId, newNode, componentCategory, x, y);

                int flag = 0;
                List<GraphNode> nodes = graph.getNodes();
                for (int i = 0; i < nodes.size(); i++) {
                    if (nodes.get(i).getName().equals(componentName) || nodes.get(i).getCId().equals(componentId)) {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    nodes.add(graphNode);
                    graph.setNodes(nodes);
                    graph.setTimestamp(new Date());
                    mongoTemplate.save(graph, COLLECTION);
                    return "Success";
                } else return "Component name already exist";
            }
            case "nodeDelete": {
                String componentId = (String) update.get("CId");
                List<GraphNode> nodes = graph.getNodes();
                List<GraphLink> links = graph.getLinks();
                for (int i = 0; i < nodes.size(); i++) {
                    if (nodes.get(i).getCId().equals(componentId)) {
                        nodes.remove(i);
                        break;
                    }
                }
                for (int i = 0; i < links.size(); i++) {
                    if (links.get(i).getFrom().equals(componentId) || links.get(i).getTo().equals(componentId)) {
                        links.remove(i);
                    }
                }
                graph.setNodes(nodes);
                graph.setLinks(links);
                graph.setTimestamp(new Date());
                mongoTemplate.save(graph, COLLECTION);
                return "Success";


            }
            case "linkAdd": {
                String from = (String) update.get("from");
                String to = (String) update.get("to");
                GraphNode fromNode = null;
                GraphNode toNode = null;
                int flag = 0;
                List<GraphNode> nodes = graph.getNodes();
                for (int i = 0; i < nodes.size() && flag < 2; i++) {
                    if (nodes.get(i).getCId().equals(from)) {
                        fromNode = nodes.get(i);
                        flag++;
                    }
                    if (nodes.get(i).getCId().equals(to)) {
                        toNode = nodes.get(i);
                        flag++;
                    }
                }
                if (flag == 2) {
                    GraphLink checkLink = new GraphLink(fromNode.getCategory(), toNode.getCategory());
                    if (help.isValidLink(checkLink)) {
                        List<GraphLink> links = graph.getLinks();
                        GraphLink newlink = new GraphLink(from, to);
                        if (!links.contains(newlink))
                            links.add(newlink);
                        graph.setLinks(links);
                        graph.setTimestamp(new Date());
                        mongoTemplate.save(graph, COLLECTION);
                        return "Success";
                    } else return "Invalid link";
                } else return "Error";

            }
            case "linkDelete": {
                String from = (String) update.get("from");
                String to = (String) update.get("to");
                List<GraphLink> links = graph.getLinks();
                GraphLink deletelink = new GraphLink(from, to);
                if (links.contains(deletelink)) {
                    links.remove(deletelink);
                    graph.setLinks(links);
                    graph.setTimestamp(new Date());
                    mongoTemplate.save(graph, COLLECTION);
                }
                return "Success";
            }
            default:
                return "Error";
        }

        //jsonGraph.setTimestamp(new Date());
        //mongoTemplate.save(jsonGraph, COLLECTION);
    }

	public void deleteGraph(String name) {
		Query query=new Query();
        query.addCriteria(Criteria.where("WFName").is(name));
        mongoTemplate.remove(query, WFGraph.class, COLLECTION);
    }

	public HashMap newWorkflow(String name) {
		HashMap<String,Object> map =new HashMap<>();
		Query query=new Query();
        query.addCriteria(Criteria.where("WFName").is(name));
        WFGraph graph = new WFGraph();
        if ((graph = mongoTemplate.findOne(query, WFGraph.class, COLLECTION)) != null) {
			map.put("Found", true);
			map.put("Graph", graph);
			return map;
		}
		else {
<<<<<<< HEAD
			WFGraph wfGraph=new WFGraph();
			wfGraph.setWFName(name);
			wfGraph.setTimestamp(new Date());
			mongoTemplate.insert(wfGraph, COLLECTION);
			graph=mongoTemplate.findOne(query,WFGraph.class,COLLECTION);
=======
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

>>>>>>> branch 'master' of https://github.com/vnkdj5/WorkflowManager.git
			map.put("Found", false);
			map.put("Graph", graph);
			return map;
		}
	}

	public HashMap getWorkflow(String name) {
		HashMap<String,Object> map=new HashMap<>();
		Query query=new Query();
		query.addCriteria(Criteria.where("WFName").is(name));
		WFGraph WFgraph = new WFGraph();
		WFgraph= mongoTemplate.findOne(query,WFGraph.class,COLLECTION);
		if(WFgraph!=null) {
			map.put("Found", true);
			map.put("Graph",WFgraph);
			System.out.println("found");

		}else {
			map.put("Found",false);
			System.out.println("not found");
		}
		return map;
	}

	public ArrayList<String> validate(ArrayList<String> to, ArrayList<String> from, HashMap<String,Node> nds,String start) {
		ArrayList<String> errorList=new ArrayList<>();
		for(Map.Entry<String, Node> node : nds.entrySet()) {
			if(!node.getValue().isValid() && !node.getValue().getLabel().equals("Start") && !node.getValue().getLabel().equals("Stop"))
				errorList.add("Incomplete configuration at "+node.getValue().getLabel());
		}

		String cur=start;
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
		query.addCriteria(Criteria.where("WFName").regex("^"));
		List<WFGraph> graphlist = new ArrayList<WFGraph>();
		graphlist.addAll(mongoTemplate.find(query,WFGraph.class,COLLECTION));
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
