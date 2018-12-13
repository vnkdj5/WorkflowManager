package com.workflow.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.workflow.component.*;

@Service("helper")
public class Helper {

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

	public String getConfig(String componentName) {
		return getObjectByClassName(componentName).getConfig();

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
			MongoClient mongoClient = new MongoClient();
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
			JSONObject obj=new JSONObject();
			obj.put("fieldName", headers[i]);
			if(entry[i].toLowerCase().equals("true") || entry[i].toLowerCase().equals("false"))
				obj.put("dataType", "boolean");
			else if(StringUtils.isNumeric(entry[i]))
				obj.put("dataType", "int");
			else if(entry[i].matches("[0-9]([.][0-9])*[0-9]*"))
				obj.put("dataType", "float");
			else obj.put("dataType", "String");
			obj.put("check", false);
			headerInfo.put(obj);
		}
		System.out.println(headerInfo.toString());
		return headerInfo;
	}
}
