package com.workflow.component;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.workflow.annotation.wfComponent;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;

@wfComponent(complete=true)
public class MongoQuery implements Component {

    Entity config;
    Entity input;
    Entity output;

    MongoTemplate mongoTemplate;
    MongoCollection<Document> collection;
    BasicDBObject command;


    @Override
    public boolean init() {
        //mongoTemplate = new MongoClient(config.getObjectByName("url").toString(),27017);
        //db = mongo.getDatabase(config.getObjectByName("database").toString());
        //collection = db.getCollection(config.getObjectByName("collection").toString());


        return false;
    }

    @Override
    public Entity process(Entity input) {
        return null;
    }

    @Override
    public Entity getConfig() {

        String Configform = "{\"schema\":{\"type\":\"object\",\"title\":\"MongoReader\",\"properties\":{\"name\":{\"title\":\"Username\",\"type\":\"string\"},\"password\":{\"title\":\"Password\",\"type\":\"string\"},\"database\":{\"title\":\"Database Name\",\"type\":\"string\"},\"collection\":{\"title\":\"Collection Name\",\"type\":\"string\"},\"url\":{\"title\":\"Sever URL\",\"type\":\"string\"},\"query\":{\"title\":\"Query\",\"type\":\"string\"}},\"required\":[\"name\",\"password\",\"collection\",\"database\",\"url\"]},\"form\":[{\"type\":\"section\",\"htmlClass\":\"row\",\"items\":[{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[\"name\"]},{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[\"password\"]}]},{\"type\":\"section\",\"htmlClass\":\"row\",\"items\":[{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[\"database\"]},{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[\"collection\"]}]},\"url\",{\"key\":\"query\",\"type\":\"textarea\",\"placeholder\":\"db.collectionName.operation()\"},{\"type\":\"section\",\"htmlClass\":\"row\",\"items\":[{\"type\":\"section\",\"htmlClass\":\"col-md-12\",\"items\":[{\"type\":\"submit\",\"style\":\"btn-info text-right\",\"title\":\"Save\"}]},{\"type\":\"section\",\"htmlClass\":\"col-md-12\",\"items\":[{\"type\":\"button\",\"style\":\"btn-info testConBtn text-left\",\"title\":\"Test\",\"onClick\":\"testConn(myForm)\"}]}]}]}";
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
        return null;
    }

    @Override
    public void setInput(Entity input) {

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
