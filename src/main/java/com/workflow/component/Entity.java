package com.workflow.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


public class Entity {
	private HashMap<String, Object> entity;
	
	public Entity() {
		super();	
		entity = new HashMap<>();
		
	}
	public Entity(Map<String, Object> entity) {
		this.entity=(HashMap<String, Object>) entity;
	}
	
	public Object getObjectByName(String name) {
		return entity.get(name);
	}
	
	public void addKeyValue(String name, Object value) {
		entity.put(name,value);
		
	}

	public HashMap<String, Object> getEntity() {
		return entity;
	}

	public void setEntity(HashMap<String, Object> entity) {
		this.entity = entity;
	}

	public static Entity getEntityFromJson(String json) {
		try {
			Entity createEntity = new Entity();
			JSONObject jsonObject = new JSONObject(json);
			Iterator<?> keys = jsonObject.keys();
			while(keys.hasNext()) {
				String key = (String)keys.next();
				createEntity.addKeyValue(key, jsonObject.get(key));
			}
			return createEntity;
		}catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	@Override
	public String toString() {
		return "Entity [entity=" + entity + "]";
	}
	
	
	
}
