package com.workflow.component;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.opencsv.*;
import com.workflow.annotation.wfComponent;

@wfComponent(complete=true)
public class CsvReader implements Component{

	Entity output;
	Entity input;
	
	ArrayList<String> csvFilePath;
	CSVReader reader;
	String[] headers;
	int totalFiles;
	int readCompleteFile;
	
	@Override
	public boolean init(Entity config,Entity input,Entity output) {
		this.input = input;
		this.output = output;
		csvFilePath = (ArrayList<String>) config.getObjectByName("filePath"); 
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
		//System.out.println("Reading from CSV " +  output);
		return output;
	}

	@Override
	public Entity getConfig() {
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
		Entity config = new Entity();
		config.addKeyValue("FORM", Configform);
		return config;
	}

	@Override
	public Entity getOutput() {
		// TODO Auto-generated method stub
		return output;
	}

	@Override
	public Entity getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInput(Entity input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOutput(Entity output) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfig(Entity config) {
		// TODO Auto-generated method stub
		
	}

}
