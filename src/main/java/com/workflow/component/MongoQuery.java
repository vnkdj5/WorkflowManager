package com.workflow.component;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.workflow.annotation.wfComponent;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;

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
    private int pointer;
    private JSONArray entries;

    @Override
    public boolean init() {
        flag=true;
        mongo = new MongoClient(config.getObjectByName("url").toString(),27017);
        db = mongo.getDatabase(config.getObjectByName("database").toString());
        collection = db.getCollection(config.getObjectByName("collection").toString());

        String query = config.getObjectByName("query").toString();

        String cmd = query.substring(0,query.indexOf("("));
        String q = query.substring(query.indexOf("(")+1,query.lastIndexOf(")"));
        command = new BasicDBObject();
        command.append(cmd,config.getObjectByName("collection").toString());
        command.append("filter",new BasicDBObject());
        return true;
    }

    @Override
    public Entity process(Entity input) {
        if(flag){
            entries=new JSONArray();
            MongoTemplate mongoTemplate = new MongoTemplate(mongo,db.getName());
            result= mongoTemplate.executeCommand(command.toString());
            JSONObject obj=new JSONObject(result.toJson());
            Object nextbatch=((Document)result.get("cursor")).get("id");
            JSONArray batch=(JSONArray)((JSONObject)obj.get("cursor")).get("firstBatch");
            for(int i=0;i<batch.length();i++)
                entries.put(batch.get(i));
            while (nextbatch!=null && !nextbatch.toString().equals("0")) {

                Document getMore = new Document("getMore", nextbatch).append("collection", config.getObjectByName("collection").toString());
                result = mongoTemplate.executeCommand(getMore);
                obj=new JSONObject(result.toJson());
                batch=(JSONArray)((JSONObject)obj.get("cursor")).get("nextBatch");
                for(int i=0;i<batch.length();i++)
                    entries.put(batch.get(i));
                nextbatch=((Document)result.get("cursor")).get("id");
            }
            pointer=0;
            flag=false;
        }
        if(pointer>=entries.length())
            return null;
        Entity ret=new Entity();
        JSONObject obj=(JSONObject) entries.get(pointer);
        for(String key:obj.keySet()) {
            ret.addKeyValue(key,obj.get(key));
        }
        pointer++;
        ret.getEntity().remove("_id");
        return ret;
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
        setOutput(null);
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
        try{
            init();
            BasicDBObject cmd=new BasicDBObject(command);
            cmd.append("batchSize",5);
            MongoTemplate mongoTemplate = new MongoTemplate(mongo,db.getName());
            Document res= mongoTemplate.executeCommand(cmd.toString());
            JSONObject obj=new JSONObject(res.toJson());
            JSONArray batch=(JSONArray)((JSONObject)obj.get("cursor")).get("firstBatch");
            Set<String> headers=new HashSet();
            ArrayList<Document> out=new ArrayList<>();
            for(int i=0;i<batch.length();i++)
                headers.addAll((((JSONObject)batch.get(i)).toMap()).keySet());
            for(String s:headers){
                Document temp=new Document();
                temp.put("fieldName",s);
                temp.put("dataType", "String");
                temp.put("check",false);
                out.add(temp);
            }
            this.output.addKeyValue("output",out);
        }catch(Exception e){}
        command=null;
        collection=null;
        db=null;
        mongo=null;
    }

    @Override
    public void setConfig(Entity config) {
        this.config = config;
        setOutput(null);
    }

    public Entity testQuery(){
        Entity ret=new Entity();
        try{
            init();
            BasicDBObject cmd=new BasicDBObject(command);
            cmd.append("batchSize",5);
            MongoTemplate mongoTemplate = new MongoTemplate(mongo,db.getName());
            Document res= mongoTemplate.executeCommand(cmd.toString());
            JSONObject obj=new JSONObject(res.toJson());
            JSONArray batch=(JSONArray)((JSONObject)obj.get("cursor")).get("firstBatch");
            ArrayList<Document> out=new ArrayList<>();
            for(int i=0;i<batch.length();i++) {
                Document temp=new Document(((JSONObject)batch.get(i)).toMap());
                out.add(temp);
            }
            ret.addKeyValue("output",out);
        }catch(Exception e){}
        return  ret;
    }
    @Override
    public boolean isValid() {
        return true;
    }
}
