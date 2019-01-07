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

	String Username;
	String Password;
	String Database;
	String Collection;
	String Url;

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
	public boolean init(Entity config) {
		mongo = new MongoClient(Url,27017);
		db = mongo.getDatabase(Database);
		collection = db.getCollection(Collection);
		return true;
	}

	@Override
	public Entity getConfig() {
		// TODO Auto-generated method stub
		String Configform = "{\"schema\":{\"type\":\"object\",\"title\":\"Comment\",\"properties\":{\"name\":{\"title\":\"Username\",\"type\":\"string\",\"required\":true},\"password\":{\"title\":\"Password\",\"type\":\"string\",\"required\":true},\"collection\":{\"title\":\"collection_name\",\"type\":\"string\",\"required\":true},\"database\":{\"title\":\"database_name\",\"type\":\"string\",\"required\":true},\"url\":{\"title\":\"Url\",\"type\":\"string\",\"required\":true}},\"required\":[\"name\",\"password\",\"collection\",\"database\",\"url\"]},\"form\":[\"name\",\"password\",\"collection\",\"database\",\"url\",{\"type\":\"submit\",\"style\":\"btn-info\",\"title\":\"OK\"},{\"type\":\"button\",\"style\":\"btn-info testConBtn\",\"title\":\"Test\",\"onClick\":\"testConn(myForm)\"}]}";
		Entity config = new Entity();
		config.addKeyValue("FORM", Configform);
		HashMap<String,String> model = new HashMap<>();
		model.put("name",Username);
		model.put("password",Password);
		model.put("url",Url);
		model.put("collection",Collection);
		model.put("database",Database);

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
		input = new Entity();
		input.addKeyValue("INPUT", input.getObjectByName("OUTPUT"));
	}

	@Override
	public void setOutput(Entity output) {
		// TODO Auto-generated method stub
		output = new Entity();
		
	}

	@Override
	public void setConfig(Entity config) {
		// TODO Auto-generated method stub
		Username = (String)config.getObjectByName("username");
		Password = (String)config.getObjectByName("password");
		Database = (String)config.getObjectByName("database");
		Collection = (String)config.getObjectByName("collection");
		Url = (String)config.getObjectByName("url");
		init(config);
		setOutput(null);
	}
	
}
