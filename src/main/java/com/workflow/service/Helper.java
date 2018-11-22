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

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoCredential;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ServerAddress;
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
			}

			else if(value instanceof JSONObject) {
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

	public Boolean checkMongoConnection(String url, String userName, char[] password, String database) {
		try {
			MongoCredential credential = MongoCredential.createCredential(userName, database, password);
			MongoClient mongoClient = new MongoClient(new ServerAddress(url), Arrays.asList(credential));
			System.out.println(mongoClient.getAddress());
		}catch (MongoTimeoutException e) {
			// TODO: handle exception
			e.printStackTrace();

			return false;
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

			return false;
		}
		return true;
	}

	public HashMap<String,String> getHeaders(String filePath){
		HashMap<String, String> headerInfo=new HashMap<>();
		String[] headers;
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
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		for(int i=0;i<headers.length;i++) {
			if(headers[i].toLowerCase().equals("true") || headers[i].toLowerCase().equals("false"))
				headerInfo.put(headers[i], "boolean");
			else if(StringUtils.isNumeric(headers[i]))
				headerInfo.put(headers[i], "int");
			else headerInfo.put(headers[i], "String");
		}
		return headerInfo;
	}
}
