package com.workflow.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Mapper implements Component{
	
	Entity output;
	ArrayList<String> allowedheaders;
	@Override
	public boolean init(Entity config,Entity input,Entity output) {
		
		this.output = output;
		allowedheaders = (ArrayList<String>) this.output.getEntity().keySet();
		return true;
	}

	@Override
	public Entity process(Entity input) {
		Entity out= new Entity();
		HashMap<String, Object> in = input.getEntity();
		for (Entry<String, Object> entry : in.entrySet()) {
			if(allowedheaders.contains(entry.getKey())) {
				out.addKeyValue(entry.getKey(), entry.getValue());
			}
		}
            
		return out;
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
		return "Mapper";
	}

	@Override
	public Entity getOutput() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
