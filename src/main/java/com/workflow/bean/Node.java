package com.workflow.bean;

import java.util.HashMap;
import java.util.Map;

import com.workflow.component.Entity;

public class Node {
	private String label;
	private Entity config= new Entity();
	private Entity input= new Entity();
	private Entity output= new Entity();
	private boolean valid=false;
	public Entity getConfig() {
		return config;
	}
	public Entity getInput() {
		return input;
	}
	public Entity getOutput() {
		return output;
	}
	public void setConfig(Entity config) {
		this.config = config;
	}
	public void setInput(Entity input) {
		this.input = input;
	}
	public void setOutput(Entity output) {
		this.output = output;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	@Override
	public String toString() {
		return "Node [label="+label+", config=" + config + ", input=" + input + ", output=" + output + "]";
	}
	
}
