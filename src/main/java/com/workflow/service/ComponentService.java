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

import java.util.*;

@Service("componentService")
public class ComponentService {

    @Autowired
    MongoTemplate  mongoTemplate;

    @Autowired
    Helper helper;

    public Entity getConfig(String WFId, String CId){
        getInput(WFId, CId);
        Query query=new Query();
        query.addCriteria(Criteria.where("id").is(WFId));
        WFGraph graph=mongoTemplate.findOne(query,WFGraph.class,"WFGraph");
        if (graph == null) {
            return null;
        }

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

    public HashMap<String,Object> setConfig(String WFId, String CId, Entity entity){
        HashMap<String,Object> ret=new HashMap<>();

        Query query=new Query();
        query.addCriteria(Criteria.where("id").is(WFId));
        WFGraph graph=mongoTemplate.findOne(query,WFGraph.class,"WFGraph");
        if(graph==null) {
            ret.put("message", "Workflow does not exist!");
            return ret;

        }else {
            List<GraphNode> nodeList = graph.getNodes();
            Iterator<GraphNode> it = nodeList.iterator();
            while (it.hasNext()) {
                GraphNode obj = it.next();
                if (obj.getCId().equals(CId)) {
                    obj.getComponent().setConfig(entity);
                    graph.setTimestamp(new Date());
                    mongoTemplate.save(graph,"WFGraph");
                    ret.put("message", "Success");
                    ret.put("config",obj.getComponent().getConfig().getEntity().get("MODEL"));
                    return ret;
                }
            }
            ret.put("message", "Component does not exist");
            return ret;
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


}
