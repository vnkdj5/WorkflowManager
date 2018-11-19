package com.workflow.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.workflow.bean.Node;
import com.workflow.component.Entity;

public class LogicGraph {

	private String id;
	private List<Node> nodes= new ArrayList<Node>();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Node> getNodes() {
		return nodes;
	}
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	public Entity getInput(int ind) {
		return nodes.get(ind).getInput();
	}
	public Entity getOutput(int ind) {
		return nodes.get(ind).getOutput();
	}
	public Entity getConfig(int ind) {
		return nodes.get(ind).getConfig();
	}
}
