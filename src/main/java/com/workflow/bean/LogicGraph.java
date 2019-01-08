package com.workflow.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.workflow.bean.Node;
import com.workflow.component.Component;
import com.workflow.component.Entity;

public class LogicGraph {

	private String id;
	private ArrayList<GraphNode> nodes= new ArrayList<>();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ArrayList<GraphNode> getNodes() {
		return nodes;
	}
	public void setNodes(ArrayList<GraphNode> nodes) {
		this.nodes = nodes;
	}
	public Entity getInput(int ind, Component parent) {
		return nodes.get(ind).getComponent().getInput(parent);
	}
	public Entity getOutput(int ind) {
		return nodes.get(ind).getComponent().getOutput();
	}
	public Entity getConfig(int ind) {
		return nodes.get(ind).getComponent().getConfig();
	}
	@Override
	public String toString() {
		return "LogicGraph [id=" + id + ", nodes=" + nodes + "]";
	}
	
}
