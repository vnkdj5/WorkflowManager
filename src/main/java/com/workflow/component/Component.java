package com.workflow.component;

public interface Component {
	public boolean init();
	public Entity process(Entity input);
	
	//input output getters
	public Entity getConfig();
	public Entity getOutput();

    public Entity getInput(Component component);
	public void setInput(Entity input);
	public void setOutput(Entity output);
	public void setConfig(Entity config);
	public boolean isValid();
}
