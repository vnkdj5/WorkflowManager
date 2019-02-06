package com.workflow.component;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.workflow.annotation.wfComponent;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
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
    private String opCmd;

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
        if(cmd.equals("find")){
            if(q.length()==0)
                command.append("filter",new BasicDBObject());
            else
                command.append("filter",(DBObject)(new JSONObject(q)));
        }else if(cmd.equals("aggregate")){
            command.append("pipeline",new Gson().fromJson(q,ArrayList.class));
        }
        opCmd=cmd;
        return true;
    }

    @Override
    public Entity process(Entity input) {
        if(flag){
            entries=new JSONArray();
            MongoTemplate mongoTemplate = new MongoTemplate(mongo,db.getName());
            result= mongoTemplate.executeCommand(command.toJson());
            JSONObject obj=new JSONObject(result.toJson());
            if(opCmd.equals("find")){
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
            }else if(opCmd.equals("aggregate")){
                ArrayList<Document> batch = (ArrayList<Document>) result.get("result");
                for(int i=0;i<batch.size();i++){
                    entries.put(batch.get(i));
                }
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
        if(opCmd.equals("find"))
            ret.getEntity().remove("_id");
        return ret;//new Entity((Map<String, Object>) pointer.next().getValue());
    }

    @Override
    public Entity getConfig() {

        String Configform = "{ \"form\":[ { \"type\":\"section\", \"htmlClass\":\"row\", \"items\":[ { \"type\":\"section\", \"htmlClass\":\"col-md-6\", \"items\":[\"name\" ] }, { \"type\":\"section\", \"htmlClass\":\"col-md-6\", \"items\":[ \"password\" ] } ] },{ \"type\":\"section\", \"htmlClass\":\"row\", \"items\":[ { \"type\":\"section\", \"htmlClass\":\"col-md-6\", \"items\":[\"database\" ] }, { \"type\":\"section\", \"htmlClass\":\"col-md-6\", \"items\":[ \"collection\" ] } ] }, \"url\", { \"key\":\"query\", \"type\":\"textarea\", \"placeholder\":\"db.collectionName.operation()\", \"onChange\": \"function(modelValue,form) { try{ balanced.matches({source: modelValue, open: ['{', '(', '['], close: ['}', ')', ']'], balance: true, exceptions: true}); document.getElementById('query').style.borderColor='green'; } catch (error) {errorMessage = error.message;console.log(error.message); document.getElementById('query').style.borderColor='red'; } }\" }, { \"type\":\"section\", \"htmlClass\":\"row\", \"items\":[ { \"type\":\"section\", \"htmlClass\":\"col-md-6\", \"items\":[ { \"type\": \"submit\", \"style\": \"btn-info text-right\", \"title\": \"Save\" } ] }, { \"type\":\"section\", \"htmlClass\":\"col-md-6\", \"items\":[ { \"type\": \"button\", \"style\": \"btn-info testConBtn text-left\", \"title\": \"Test\", " +
                "\"onClick\": \"function(modelValue,myForm){ " +
                "var scope1 = angular.element($('#mainScope')).scope();" +
                "scope1.testConn(myForm); " +
                "scope1.onSubmit(myForm);" +
                " let WFID=scope1.currentWorkflowName; let compId=scope1.selectedComponent.key;" +
                "var xhttp = new XMLHttpRequest();" +
                "xhttp.onreadystatechange = function() {" +
                "    if (this.readyState == 4 && this.status == 200) {" +
                "       console.log(xhttp.responseText);" +
                "       document.getElementById('queryOutput').value=xhttp.responseText" +
                "    }" +
                "};" +
                "xhttp.open('GET', 'getOutput/'+WFID+'/'+compId, true);" +
                "xhttp.send();" +
                "      }\" } ] } ] }, {\"key\":\"queryOutput\",\"type\":\"textarea\",\"placeholder\":\"Output\", \"readonly\":true}], \"schema\":{ \"type\": \"object\", \"title\": \"MongoReader\", \"properties\": { \"name\": { \"title\": \"Username\", \"type\": \"string\" }, \"password\": { \"title\": \"Password\", \"type\": \"string\" }, \"database\": { \"title\": \"Database Name\", \"type\": \"string\" }, \"collection\": { \"title\": \"Collection Name\", \"type\": \"string\" }, \"url\": { \"title\": \"Sever URL\", \"type\": \"string\" }, \"query\": { \"title\":\"Query\", \"type\":\"string\" }, \"queryOutput\":{\"title\":\"Query Output\", \"type\":\"string\"} }, \"required\": [\"name\", \"password\", \"collection\", \"database\", \"url\"] }}";


        //FORM INSIDE CODE:: $scope.onSubmit(form);let WFID=$scope.workflowName; let componentKey=$scope.selectedComponent.key; componentService.getOutput(WFID, componentKey).then( function success(response){ document.getElementById(\"output\").value=response.data; }, function error(response { notify.showError(response.data.message); });
       // JSONObject obj = new JSONObject(Configform);
        Entity config = new Entity();
        config.addKeyValue("FORM", Configform);// obj.toMap()
        HashMap<String, Object> model = null;

        if(this.config!=null){
            model = this.config.getEntity();
        } else {
            model = new HashMap<>();
        }
        config.addKeyValue("MODEL",model);

        return config;
    }

    @Override
    public Entity getOutput() {
        if(config==null)
            return null;
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
        init();
        /*try{

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
        }catch(Exception e){
            e.printStackTrace();
        }*/
        Entity test=process(null);
        if(test!=null) {
            HashMap<String, Object> out = test.getEntity();
            JSONArray outputE = new JSONArray();
            for (String h : out.keySet()) {
                JSONObject temp = new JSONObject();
                temp.put("fieldName", h);
                temp.put("dataType", "String");
                temp.put("check", false);
                outputE.put(temp);

            }
            this.output.addKeyValue("output", outputE.toList());
        }
        command=null;
        collection=null;
        db=null;
        mongo=null;
        opCmd=null;
        entries=null;
        result=null;
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

    public ArrayList<String> testQuery(){
        ArrayList<String> ret=new ArrayList<>();
        init();
        BasicDBObject cmd=new BasicDBObject(command);
        cmd.append("batchSize",5);
        MongoTemplate mongoTemplate = new MongoTemplate(mongo,db.getName());
        Document res= mongoTemplate.executeCommand(cmd.toString());
        JSONObject obj=new JSONObject(res.toJson());
        JSONArray batch=(JSONArray)((JSONObject)obj.get("cursor")).get("firstBatch");
        for(int i=0;i<batch.length();i++)
            ret.add(batch.get(i).toString());
        return  ret;
    }
}
