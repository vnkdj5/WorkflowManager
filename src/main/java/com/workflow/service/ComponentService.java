package com.workflow.service;

import com.workflow.bean.GraphLink;
import com.workflow.bean.GraphNode;
import com.workflow.bean.WFGraph;
import com.workflow.component.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service("componentService")
public class ComponentService {

    @Autowired
    MongoTemplate  mongoTemplate;

    @Autowired
    Helper helper;

    public Entity getConfig(String WFId, String CId){
        Query query=new Query();
        query.addCriteria(Criteria.where("id").is(WFId));
        WFGraph graph=mongoTemplate.findOne(query,WFGraph.class,"WFGraph");
        if(graph==null)
            return null;
        else {
            ArrayList<GraphNode>nodeList=new ArrayList<>(graph.getNodes());
            Iterator<GraphNode> it=nodeList.iterator();
            while(it.hasNext()){
                GraphNode obj=it.next();
                if(obj.getCId().equals(CId)){
                    return obj.getComponent().getConfig();
                }
            }
            return null;
        }
    }

    public String setConfig(String WFId, String CId, Entity entity){
        Query query=new Query();
        query.addCriteria(Criteria.where("id").is(WFId));
        WFGraph graph=mongoTemplate.findOne(query,WFGraph.class,"WFGraph");
        if(graph==null)
            return "Workflow does not exist!";
        else {
            List<GraphNode> nodeList = graph.getNodes();
            Iterator<GraphNode> it = nodeList.iterator();
            while (it.hasNext()) {
                GraphNode obj = it.next();
                if (obj.getCId().equals(CId)) {
                    obj.getComponent().setConfig(entity);

                    System.out.println(obj.getComponent().getConfig().toString());

                    graph.setTimestamp(new Date());

                    System.out.println("UPDATE CONFIG: "+ graph);

                    mongoTemplate.save(graph,"WFGraph");
                    return "Success";
                }
            }
            return "Component does not exist";
        }
    }

    public Entity getInput(String WFId, String CId){
        Query query=new Query();
        query.addCriteria(Criteria.where("id").is(WFId));
        WFGraph graph=mongoTemplate.findOne(query,WFGraph.class,"WFGraph");
        if(graph==null)
            return null;
        else {
            ArrayList<GraphNode>nodeList=new ArrayList<>(graph.getNodes());
            ArrayList<GraphLink>linkList=new ArrayList<>(graph.getLinks());
            Iterator<GraphNode> it=nodeList.iterator();
            while(it.hasNext()){
                GraphNode obj=it.next();
                if(obj.getCId().equals(CId)){
                    GraphNode pass=null;
                    Iterator<GraphLink> lit=linkList.iterator();
                    while(lit.hasNext()){
                        GraphLink link=lit.next();
                        if(link.getTo().equals(CId)){
                            it=nodeList.iterator();
                            while(it.hasNext()) {
                                GraphNode parent = it.next();
                                if (parent.getCId().equals(link.getFrom())) {
                                    Entity updatedinput = obj.getComponent().getInput(parent.getComponent());
                                    mongoTemplate.save(graph,"WFGraph");
                                    return updatedinput;
                                }
                            }
                        }
                    }
                    return obj.getComponent().getInput(null);
                }
            }
            return null;
        }
    }

    public Entity getOutput(String WFId, String CId){
        Query query=new Query();
        query.addCriteria(Criteria.where("id").is(WFId));
        WFGraph graph=mongoTemplate.findOne(query,WFGraph.class,"WFGraph");
        if(graph==null)
            return null;
        else {
            ArrayList<GraphNode>nodeList=new ArrayList<>(graph.getNodes());
            Iterator<GraphNode> it=nodeList.iterator();
            while(it.hasNext()){
                GraphNode obj=it.next();
                if(obj.getCId().equals(CId)){
                    return obj.getComponent().getOutput();
                }
            }
            return null;
        }
    }

    public Entity fileUploadConfig(String WFId,String CId,String path, String[] headers) {
        Query query=new Query();
        query.addCriteria(Criteria.where("id").is(WFId));
        WFGraph graph=mongoTemplate.findOne(query,WFGraph.class,"WFGraph");
        if(graph==null)
            return null;
        else {
            List<GraphNode> nodeList = graph.getNodes();
            Iterator<GraphNode> it = nodeList.iterator();
            while (it.hasNext()) {
                GraphNode obj = it.next();
                if (obj.getCId().equals(CId)) {
                    ArrayList<String> filepaths =(ArrayList<String>) obj.getComponent().getConfig().getObjectByName("filePath");
                    if(filepaths==null){
                        filepaths = new ArrayList<>();
                    }

                    filepaths.add(path);

                    Entity updatedConfig = new Entity();
                    updatedConfig.addKeyValue("filePath", filepaths);
                    updatedConfig.addKeyValue("headers",headers);

                    obj.getComponent().setConfig(updatedConfig);
                    System.out.println(obj.getComponent().getConfig().toString());

                    graph.setTimestamp(new Date());

                    System.out.println("UPDATE CONFIG: "+ graph);

                    mongoTemplate.save(graph,"WFGraph");
                    return updatedConfig;
                }
            }
            return null;
        }

    }
}
