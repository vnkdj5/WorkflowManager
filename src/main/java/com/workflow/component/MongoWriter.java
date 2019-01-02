package com.workflow.component;


import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.workflow.annotation.wfComponent;

@wfComponent(complete=true)
public class MongoWriter implements Component{

	Entity input;
	Entity output;
	
	MongoClient mongo;
	MongoDatabase db;
	MongoCollection<Document> collection;

	@Override
	public Entity process(Entity input) {
		Document document = new Document(input.getEntity());
		collection.insertOne(document);
		//System.out.println("Writing to Mongo "+ document);
		return null;
	}

	@Override
	public boolean init(Entity config,Entity input,Entity output) {
		mongo = new MongoClient((String)config.getObjectByName("url"),27017);
		db = mongo.getDatabase((String)config.getObjectByName("database"));
		collection = db.getCollection((String)config.getObjectByName("collection"));
		return true;
	}

	@Override
	public Entity getConfig() {
		// TODO Auto-generated method stub
		String Configform = "{\"schema\":{\"type\":\"object\",\"title\":\"Comment\",\"properties\":{\"name\":{\"title\":\"Username\",\"type\":\"string\",\"required\":true},\"password\":{\"title\":\"Password\",\"type\":\"string\",\"required\":true},\"collection\":{\"title\":\"collection_name\",\"type\":\"string\",\"required\":true},\"database\":{\"title\":\"database_name\",\"type\":\"string\",\"required\":true},\"url\":{\"title\":\"Url\",\"type\":\"string\",\"required\":true}},\"required\":[\"name\",\"password\",\"collection\",\"database\",\"url\"]},\"form\":[\"name\",\"password\",\"collection\",\"database\",\"url\",{\"type\":\"submit\",\"style\":\"btn-info\",\"title\":\"OK\"},{\"type\":\"button\",\"style\":\"btn-info testConBtn\",\"title\":\"Test\",\"onClick\":\"testConn(myForm)\"}]}";
		Entity config = new Entity();
		config.addKeyValue("FORM", Configform);
		return config;
	}

	@Override
	public Entity getOutput() {
		// TODO Auto-generated method stub
		return output;
	}

	@Override
    public Entity getInput(Component component) {
		// TODO Auto-generated method stub
        input.addKeyValue("INPUT", component.getOutput().getEntity().get("OUTPUT"));
		return input;
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
