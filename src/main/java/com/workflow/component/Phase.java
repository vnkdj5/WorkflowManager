package com.workflow.component;

import com.workflow.annotation.wfComponent;

@wfComponent(complete=true)
public class Phase implements Component {

    boolean completion;

    @Override
    public boolean init() {
        completion=false;
        return true;
    }

    @Override
    public Entity process(Entity input) {
        return null;
    }

    @Override
    public Entity getConfig() {
        return null;
    }

    @Override
    public Entity getOutput() {
        return null;
    }

    @Override
    public Entity getInput(Component component) {
        return null;
    }

    @Override
    public void setInput(Entity input) {

    }

    @Override
    public void setOutput(Entity output) {

    }

    @Override
    public void setConfig(Entity config) {

    }

    @Override
    public boolean isValid() {
        return false;
    }
}
