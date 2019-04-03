package com.workflow.component;


import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.workflow.annotation.wfComponent;

import javax.xml.crypto.Data;
import java.util.HashMap;

@wfComponent(complete=true)
public class MongoWriter implements Component{

	Entity input;
	Entity output;
	Entity config;

	private MongoClient mongo;
	private MongoDatabase db;
	private MongoCollection<Document> collection;

	@Override
	public Entity process(Entity input) {
		if(input!=null) {
			Document document = new Document(input.getEntity());
			collection.insertOne(document);
		}
		return null;
	}

	@Override
	public boolean init() {
		mongo = new MongoClient(config.getObjectByName("url").toString(),27017);
		db = mongo.getDatabase(config.getObjectByName("database").toString());
		collection = db.getCollection(config.getObjectByName("collection").toString());
		return true;
	}

	@Override
	public Entity getConfig() {
		// TODO Auto-generated method stub
        String Configform = "{\"schema\":{\"type\":\"object\",\"title\":\"Comment\",\"properties\":{\"name\":{\"title\":\"Username\",\"type\":\"string\",\"required\":true},\"password\":{\"title\":\"Password\",\"type\":\"string\",\"required\":true},\"collection\":{\"title\":\"collection_name\",\"type\":\"string\",\"required\":true},\"database\":{\"title\":\"database_name\",\"type\":\"string\",\"required\":true},\"url\":{\"title\":\"Url\",\"type\":\"string\",\"required\":true}},\"required\":[\"name\",\"password\",\"collection\",\"database\",\"url\"]},\"form\":[\"name\",{   \"key\": \"password\",\"type\":\"password\"},\"collection\",\"database\",\"url\",{\"type\":\"submit\",\"style\":\"btn-info\",\"title\":\"OK\"},{\"type\":\"button\",\"style\":\"btn-info testConBtn\",\"title\":\"Test\",\"onClick\":\"testConn(myForm)\"}]}";
		JSONObject obj = new JSONObject(Configform);
		Entity config = new Entity();
		config.addKeyValue("FORM", Configform);
		HashMap<String, Object> model = null;
		if(this.config!=null){
			model = this.config.getEntity();
		}
		config.addKeyValue("MODEL",model);
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
		setInput(component.getOutput());
		return input;
	}

	@Override
	public void setInput(Entity input) {
		// TODO Auto-generated method stub
		this.input = new Entity();
		if(input!=null)
			if(input.getEntity().get("output")!=null) {
				this.input.addKeyValue("input", input.getEntity().get("output"));
			}
		else this.input.addKeyValue("input", null);
	}

	@Override
	public void setOutput(Entity output) {
		// TODO Auto-generated method stub
		this.output = new Entity();

	}

	@Override
	public void setConfig(Entity config) {
		// TODO Auto-generated method stub
		this.config = config;
		setOutput(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

}
