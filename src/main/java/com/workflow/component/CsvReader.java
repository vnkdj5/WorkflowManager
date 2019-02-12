package com.workflow.component;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.opencsv.*;
import com.workflow.annotation.wfComponent;
import org.json.JSONArray;
import org.json.JSONObject;

@wfComponent(complete=true)
public class CsvReader implements Component{

	final static String FILEPATH = "filePath";
	final static String HEADERS = "headers";
	final static String INPUT = "input";
	final static String OUTPUT = "output";

	Entity output;
	Entity input;
	Entity config;

	ArrayList<String> csvFilePath;
	CSVReader reader;
	public String[] headers;
	int totalFiles;
	int readCompleteFile;
	
	@Override
	public boolean init() {
		System.out.println("in I csv");
		csvFilePath = (ArrayList<String>) config.getObjectByName(FILEPATH);
		totalFiles = csvFilePath.size();
		readCompleteFile=0;
		try {
			reader = new CSVReader(new FileReader(csvFilePath.get(readCompleteFile)));
			headers = reader.readNext();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public Entity process(Entity input) {
		System.out.println("in P csv");
		Entity output = new Entity();
		String[] record;
		
		
		try {
			if((record = reader.readNext())!=null) {
				for(int i=0;i<record.length;i++) {
					output.addKeyValue(headers[i], record[i]);
				}
				
			}
			if(record==null && totalFiles==readCompleteFile) {
				
				return null;
			}else if(record==null && readCompleteFile<totalFiles){
				readCompleteFile++;
				if(readCompleteFile<totalFiles) {
					reader = new CSVReader(new FileReader(csvFilePath.get(readCompleteFile)));
				}else {
					return null;
				}
				reader.readNext();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return output;
	}

	@Override
	public Entity getConfig() {
		System.out.println("in getC csv");
		// TODO Auto-generated method stub
		String Configform =  "{\"schema\":"
				+ "{"
				+ "\"type\":\"object\","
				+ "\"title\":\"Upload CSV File\""
				+ ",\"properties\":{"
				+ "\"file\":"
				+ "{\"title\":\"CSV File Upload\","
				+ "\"type\":\"array\","
				+ "\"format\":\"multifile\","
				+ "\"x-schema-form\":"
				+ "{"
				+ "\"type\":\"array\""
				+ "},"
				+ "\"pattern\":{"
				+ "\"mimeType\":\".csv\","
				+ "\"validationMessage\":\"Text Files only \""
				+ "},"
				+ "\"maxSize\":{"
				+ "\"maximum\":\"1024MB\","
				+ "\"validationMessage\":\"File upload limit reached: \""
				+ "}"
				+ "},"
				+ "\"filePath\":{"
				+ "\"title\":\"File Paths\","
				+ "\"type\":\"array\","
				+ "\"items\":{"
				+ "\"type\":\"string\","
				+ "\"readonly\":\"true\""
				+ "}"
				+ "}"
				+ "},"
				+ "\"required\":[]"
				+ "},"
				+ "\"form\":["
				+ "{"
				+ "\"key\":\"file\","
				+ "\"type\":\"nwpFileUpload\","
				+ "\"endpoint\":\"/WorkflowManager/uploadfile\","
				+ "\"onChange\":\"updated(model,form)\""
				+ "}"
                + ",{"
                + "\"title\":\"Uploaded Path\","
                + "\"key\":\"filePath\","
                + "\"type\":\"array\","
                + "\"startEmpty\":true,"
                + "\"add\":null"
                + "},"
				+ "{"
				+ "\"type\":\"submit\""
				+ ",\"title\":\"Save\""
				+ "}"
				+ "]"
				+ "}";

		//JSONObject obj = new JSONObject(Configform);
		Entity config = new Entity();
		config.addKeyValue("FORM", Configform);

		//create model
		if(this.config==null){
			this.config = new Entity();
		}
		config.addKeyValue("MODEL", this.config.getEntity());
		return config;
	}

	@Override
	public Entity getOutput() {
		System.out.println("in getO csv");
		return output;
	}

	@Override
    public Entity getInput(Component component) {
		System.out.println("in getI csv");
		if(component!=null){
			setInput(component.getOutput());
		}else{
			setInput(null);
		}


		return input;
	}

	@Override
	public void setInput(Entity input) {
		System.out.println("in setI csv");
		this.input = new Entity();
		this.input.addKeyValue(INPUT, null);
	}

	@Override
	public void setOutput(Entity output) {
		System.out.println("in setO csv");
		this.output = new Entity();
		JSONArray list=new JSONArray();
		ArrayList<String> headers=(ArrayList<String>) this.config.getEntity().get(HEADERS);
		for(int i=0;i<headers.size();i++){
			JSONObject temp=new JSONObject();
			temp.put("fieldName", headers.get(i));
			temp.put("dataType","String");
			temp.put("check", false);
			list.put(temp);
		}
		this.output.addKeyValue(OUTPUT,list.toList());
	}

	@Override
	public void setConfig(Entity config) {
		System.out.println("in setC csv");
		this.config = config;
		if(((ArrayList<String>)this.config.getEntity().get("filePath")).size()<1){
			this.config.getEntity().put("headers",new ArrayList<String>());
		}
		setOutput(null);
	}

	@Override
	public boolean isValid() {
		System.out.println("in isV csv");
		return true;
	}

}
