package com.workflow.component;

public interface Component {
	public boolean init(Entity config);
	public Entity process(Entity input);
	public String getConfig();
	//input output getters
	public Entity getOutput();
}
