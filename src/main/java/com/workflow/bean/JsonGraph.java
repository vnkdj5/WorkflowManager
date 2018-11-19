package com.workflow.bean;

import java.util.Date;

import org.json.simple.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="jsonGraph")
public class JsonGraph {

	@Id
	private String name;
	private Date timestamp;
	private JSONObject jgraph;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public JSONObject getJgraph() {
		return jgraph;
	}
	public void setJgraph(JSONObject jgraph) {
		this.jgraph = jgraph;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
}
