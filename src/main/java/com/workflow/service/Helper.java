package com.workflow.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;

import com.workflow.bean.GraphNode;
import com.workflow.bean.WFGraph;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoCredential;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.opencsv.CSVReader;
import com.workflow.bean.GraphLink;
import com.workflow.component.*;

@Service("helper")
public class Helper {
    @Autowired
    MongoTemplate mongoTemplate;
	
	public Component getObjectByClassName(String classname) {
		Object object = null;
		try {
			Class<?> cls = Class.forName("com.workflow.component."+classname);
			object = cls.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (Component)object;
	}


	public Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while(keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if(value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}
	public List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if(value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("deprecation")
	public HashMap<String,Boolean> checkMongoConnection(String url, String userName, char[] password, String database, String collection) {
		HashMap<String,Boolean> checkResult = new HashMap<>();
		try {
			MongoCredential credential = MongoCredential.createCredential("", database, "".toCharArray());
			MongoClient mongoClient = new MongoClient(url);
			System.out.println(mongoClient.getAddress());
			
			
			checkResult.put("Connection", true);
			MongoDatabase db = mongoClient.getDatabase(database);
			for(String name : db.listCollectionNames()){
				System.out.println(name);
				if(name.equals(collection)) {
					checkResult.put("Collection", true);
					
					break;
				}
			}
			if(!checkResult.containsKey("Collection")){
				checkResult.put("Collection", false);

			}
			
		}catch (MongoTimeoutException e) {
			// TODO: handle exception
			e.printStackTrace();
			checkResult.put("Connection", false);
			return checkResult;
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			checkResult.put("Connection", false);
			return checkResult;
		}
		return checkResult;
	}

	public JSONArray getHeaders(String filePath){
		JSONArray headerInfo=new JSONArray();
		String[] headers;
		String[] entry;
		FileReader fr=null;
		try {
			fr=new FileReader(filePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		CSVReader reader=new CSVReader(fr);
		try {
			headers=reader.readNext();
			entry=reader.readNext();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		for(int i=0;i<headers.length;i++) {
			JSONObject obj = new JSONObject();
			obj.put("fieldName", headers[i]);
			/*if(entry[i].toLowerCase().equals("true") || entry[i].toLowerCase().equals("false"))
				obj.put("dataType", "boolean");
			else if(StringUtils.isNumeric(entry[i]))
				obj.put("dataType", "int");
			else if(entry[i].matches("[0-9]([.][0-9])*[0-9]*"))
				obj.put("dataType", "float");
			else */
			obj.put("dataType", "String");
			obj.put("check", false);
			headerInfo.put(obj);
		}
		return headerInfo;
	}

    public boolean isValidLink(GraphLink link) {
		System.out.println(link.toString());
        List<GraphLink> validLinks = mongoTemplate.findAll(GraphLink.class, "validLinks");
        System.out.println(validLinks.toString());
        for(GraphLink l : validLinks){
        	if(l.getFrom().equals(link.getFrom()) && l.getTo().equals(link.getTo())){
        		return true;
			}
		}
        return false;
    }

	public Entity fileUploadConfig(String WFId,String CId,String path, JSONArray headers) {
		Query query=new Query();
		query.addCriteria(Criteria.where("id").is(WFId));
		WFGraph graph=mongoTemplate.findOne(query,WFGraph.class,"WFGraph");
		if(graph==null)
			return null;
		else {
			List<GraphNode> nodeList = graph.getNodes();
			Iterator<GraphNode> it = nodeList.iterator();
			while (it.hasNext()) {
				GraphNode obj = it.next();
				if (obj.getCId().equals(CId)) {
					CsvReader reader=(CsvReader) obj.getComponent();
					ArrayList<String> filepaths =(ArrayList<String>) obj.getComponent().getConfig().getObjectByName("filePath");
					if(filepaths==null){
						filepaths = new ArrayList<>();
						reader.headers=new String[0];
					}
					ArrayList<String> newHeaders= new ArrayList<>();
					for (int i=0;i<headers.length();i++){
						newHeaders.add(((JSONObject)headers.get(i)).get("fieldName").toString());
					}
					ArrayList<String> reqHeaders=new ArrayList<>();
					for(int i=0;i<reader.headers.length;i++){
						reqHeaders.add(reader.headers[i]);
					}
					if(reqHeaders.isEmpty()){

						reader.headers=new String[newHeaders.size()];
						reader.headers=newHeaders.toArray(reader.headers);
					}
					else{
						boolean flag=true;
						for(int i=0;i<newHeaders.size();i++){
							if(!reqHeaders.contains(newHeaders.get(i))){
								flag=false;
								break;
							}
						}
						if(!flag){
							Entity res= new Entity();
							res.addKeyValue("error",	"Incompatible headers");
							File file=new File(path);
							file.delete();
							return res;
						}
					}
					System.out.println("reqarr: "+reader.headers.toString());
					System.out.println("newarr: "+ newHeaders);
					filepaths.add(path);

					Entity updatedConfig = new Entity();
					updatedConfig.addKeyValue("filePath", filepaths);
					updatedConfig.addKeyValue("headers",headers);

					obj.getComponent().setConfig(updatedConfig);
					System.out.println(obj.getComponent().getConfig().toString());

					graph.setTimestamp(new Date());

					System.out.println("UPDATE CONFIG: "+ graph);

					mongoTemplate.save(graph,"WFGraph");
					return updatedConfig;
				}
			}
			return null;
		}

	}
}
