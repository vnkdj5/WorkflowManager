package com.workflow.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.workflow.bean.Node;
import com.workflow.component.Entity;

@Document(collection = "WFGraph")
public class WFGraph {
    @Id
    private Id id;
    private String WFName;
    private Date timestamp;
    private List<GraphNode> nodes = new ArrayList<GraphNode>();
    private List<GraphLink> links = new ArrayList<GraphLink>();

    public String getId() {
        return id.toString();
    }

    public String getWFName() {
        return WFName;
    }

    public void setWFName(String wFName) {
        WFName = wFName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<GraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GraphNode> nodes) {
        this.nodes = nodes;
    }

    public List<GraphLink> getLinks() {
        return links;
    }

    public void setLinks(List<GraphLink> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "LogicGraph [id=" + id + ", nodes=" + nodes + "]";
    }

}


