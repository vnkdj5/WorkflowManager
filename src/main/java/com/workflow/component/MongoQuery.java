package com.workflow.component;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.workflow.annotation.wfComponent;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@wfComponent(complete=true)
public class MongoQuery implements Component {

    Entity config;
    Entity input;
    Entity output;

    private Document result;
    private MongoClient mongo;
    private MongoCollection<Document> collection;
    private MongoDatabase db;
    private BasicDBObject command;
    private boolean flag;
    private Iterator<Map.Entry<String,Object>> pointer;

    @Override
    public boolean init() {
        flag=true;
        mongo = new MongoClient(config.getObjectByName("url").toString(),27017);
        db = mongo.getDatabase(config.getObjectByName("database").toString());
        collection = db.getCollection(config.getObjectByName("collection").toString());

        String query = config.getObjectByName("query").toString();

        String cmd = query.substring(0,query.indexOf("("));
        String q = query.substring(query.indexOf("(")+1,query.lastIndexOf(")"));

        System.out.println("MONGOQUERY: "+cmd+" "+q);

        command = new BasicDBObject();
        command.append(cmd,config.getObjectByName("collection").toString());
        command.append("filter",new BasicDBObject());
        return true;
    }

    @Override
    public Entity process(Entity input) {
        if(flag){
            MongoTemplate mongoTemplate = new MongoTemplate(mongo,db.getName());
            result=mongoTemplate.executeCommand(command.toString());
            System.out.println(result.toString());
            pointer= result.entrySet().iterator();
        }

        return null;//new Entity((Map<String, Object>) pointer.next().getValue());
    }

    @Override
    public Entity getConfig() {

        String Configform = "{\"schema\":{\"type\":\"object\",\"title\":\"MongoQuery\",\"properties\":{\"name\":{\"title\":\"Username\",\"type\":\"string\"},\"password\":{\"title\":\"Password\",\"type\":\"string\"},\"database\":{\"title\":\"Database Name\",\"type\":\"string\"},\"collection\":{\"title\":\"Collection Name\",\"type\":\"string\"},\"url\":{\"title\":\"Sever URL\",\"type\":\"string\"},\"query\":{\"title\":\"Query\",\"type\":\"string\"}},\"required\":[\"name\",\"password\",\"collection\",\"database\",\"url\"]},\"form\":[{\"type\":\"section\",\"htmlClass\":\"row\",\"items\":[{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[\"name\"]},{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[\"password\"]}]},{\"type\":\"section\",\"htmlClass\":\"row\",\"items\":[{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[\"database\"]},{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[\"collection\"]}]},\"url\",{\"key\":\"query\",\"type\":\"textarea\",\"placeholder\":\"db.collectionName.operation()\"},{\"type\":\"section\",\"htmlClass\":\"row\",\"items\":[{\"type\":\"section\",\"htmlClass\":\"col-md-12\",\"items\":[{\"type\":\"submit\",\"style\":\"btn-info text-right\",\"title\":\"Save\"}]},{\"type\":\"section\",\"htmlClass\":\"col-md-12\",\"items\":[{\"type\":\"button\",\"style\":\"btn-info testConBtn text-left\",\"title\":\"Test\",\"onClick\":\"testConn(myForm)\"}]}]}]}";
        JSONObject obj = new JSONObject(Configform);
        Entity config = new Entity();
        config.addKeyValue("FORM", obj.toMap());
        HashMap<String, Object> model = null;

        if(this.config!=null){
            model = this.config.getEntity();
        }
        config.addKeyValue("MODEL",model);

        return config;
    }

    @Override
    public Entity getOutput() {
        return output;
    }

    @Override
    public Entity getInput(Component component) {
        return new Entity();
    }

    @Override
    public void setInput(Entity input) {
        this.input = new Entity();
    }

    @Override
    public void setOutput(Entity output) {
        this.output = new Entity();

    }

    @Override
    public void setConfig(Entity config) {
        this.config = config;

        setOutput(null);
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
