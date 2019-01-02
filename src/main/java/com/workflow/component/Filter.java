package com.workflow.component;

import com.workflow.annotation.wfComponent;

@wfComponent(complete=true)
public class Filter implements Component {

	@Override
	public boolean init(Entity config, Entity input, Entity output) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Entity process(Entity input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getOutput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public Entity getInput(Component conponent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInput(Entity input) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutput(Entity output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConfig(Entity config) {
		// TODO Auto-generated method stub

	}

}
