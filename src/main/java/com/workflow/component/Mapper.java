package com.workflow.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import com.workflow.annotation.wfComponent;

@wfComponent(complete=true)
public class Mapper implements Component{
	
	
	Entity input;
	Entity output;

	
	ArrayList<String> allowedheaders;
	ArrayList<String> allowedHeadersDatatypes;
	@Override
	public boolean init(Entity config,Entity input,Entity output) {
		
		this.output = output;
		this.input = input;

		
		allowedheaders = new ArrayList<>();
		allowedHeadersDatatypes = new ArrayList<>();
		JSONArray temp = (JSONArray) this.output.getEntity().get("allowed");
		
		for(int i=0;i<temp.length();i++) {
			String header = ((JSONObject)temp.get(i)).getString("fieldName");
			String datatype = ((JSONObject)temp.get(i)).getString("dataType");
			allowedheaders.add(header);
			allowedHeadersDatatypes.add(datatype);
		}
		return true;
	}

	@Override
	public Entity process(Entity input) {
		Entity out= new Entity();
		HashMap<String, Object> in = input.getEntity();
		for (Entry<String, Object> entry : in.entrySet()) {
            try {
                if (allowedheaders.contains(entry.getKey())) {
                    //improve logic here
                    String datatype = allowedHeadersDatatypes.get(allowedheaders.indexOf(entry.getKey()));
                    if (datatype.equals("boolean")) {
                        out.addKeyValue(entry.getKey(), Boolean.parseBoolean((String) entry.getValue()));
                    } else if (datatype.equals("int")) {
                        out.addKeyValue(entry.getKey(), Integer.parseInt((String) entry.getValue()));
                    } else if (datatype.equals("float")) {
                        out.addKeyValue(entry.getKey(), Float.parseFloat((String) entry.getValue()));
                    } else {
                        out.addKeyValue(entry.getKey(), entry.getValue());
                    }
                }
            } catch (NumberFormatException e) {
                out.addKeyValue(entry.getKey(), 0);
			}


        }
            
		return out;
	}

	@Override
	public Entity getConfig() {
		
		// TODO Auto-generated method stub
		String Configform =  "{\n" +
                "\t\"schema\": {\n" +
                "\t\t\"type\": \"object\",\n" +
                "\t\t\"title\": \"\",\n" +
                "\t\t\"properties\": {\n" +
                "\t\t\t\"field\": {\n" +
                "\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\"items\": {\n" +
                "\t\t\t\t\t\"type\": \"object\",\n" +
                "\t\t\t\t\t\"properties\": {\n" +
                "\t\t\t\t\t\t\"check\": {\n" +
                "\t\t\t\t\t\t\t\"title\": \"\",\n" +
                "\t\t\t\t\t\t\t\"type\": \"boolean\"\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"fieldName\": {\n" +
                "\t\t\t\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\t\t\t\"readonly\": false\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"dataType\": {\n" +
                "\t\t\t\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\t\t\t\"readonly\": false\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t},\n" +
                "\t\t\t\"outputFields\": {\n" +
                "\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\"items\": {\n" +
                "\t\t\t\t\t\"type\": \"object\",\n" +
                "\t\t\t\t\t\"properties\": {\n" +
                "\t\t\t\t\t\t\"check\": {\n" +
                "\t\t\t\t\t\t\t\"title\": \"\",\n" +
                "\t\t\t\t\t\t\t\"type\": \"boolean\"\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"fieldName\": {\n" +
                "\t\t\t\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\t\t\t\"readonly\": false\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"dataType\": {\n" +
                "\t\t\t\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\t\t\t\"readonly\": false\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\"required\": [\n" +
                "\t\t\t\"field\"\n" +
                "\t\t]\n" +
                "\t},\n" +
                "\t\"form\": [" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"button\",\n" +
                "\t\t\t\"title\": \"==>\",\n" +
                "\t\t\t\"style\": \"btn-info float-btn\",\n" +
                "\t\t\t\"htmlClass\": \"text-center\",\n" +
                "\t\t\t\"onClick\": \"mapperHandler()\"\n" +
                "\t\t},\n" +
                "{\n" +
                "\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\"htmlClass\": \"row\",\n" +
                "\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\"htmlClass\": \"col-md-6\",\n" +
                "\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\"key\": \"field\",\n" +
                "\t\t\t\t\t\t\"htmlClass\": \"\",\n" +
                "\t\t\t\t\t\t\"notitle\": false,\n" +
                "\t\t\t\t\t\t\"add\": null,\n" +
                "\t\t\t\t\t\t\"remove\": null,\n" +
                "\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\"htmlClass\": \"form-row\",\n" +
                "\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\t\t\"htmlClass\": \"col-md-2\",\n" +
                "\t\t\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\t\t\"key\": \"['field'][].['check']\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"type\": \"checkbox\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"title\": \"\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"notitle\": true\n" +
                "\t\t\t\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\t\t\"htmlClass\": \"col-md-4\",\n" +
                "\t\t\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\t\t\"key\": \"['field'][].['fieldName']\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"notitle\": true\n" +
                "\t\t\t\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\t\t\"htmlClass\": \"col-md-4\",\n" +
                "\t\t\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\t\t\"key\": \"['field'][].['dataType']\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"notitle\": true\n" +
                "\t\t\t\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\"htmlClass\": \"col-md-6\",\n" +
                "\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\"key\": \"outputFields\",\n" +
                "\t\t\t\t\t\t\"htmlClass\": \"\",\n" +
                "\t\t\t\t\t\t\"notitle\": false ,\n" +
                "\t\t\t\t\t\t\"add\": null,\n" +
				"\t\t\t\t\t\t\"remove\": null,\n" +
                "\t\t\t\t\t\t\"startEmpty\": true,\n" +
                "\n" +
                "\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\"htmlClass\": \"form-row\",\n" +
                "\t\t\t\t\t\t\t\"items\": [\n" +
                "\n" +
                "\t\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\t\t\"htmlClass\": \"col-md-5\",\n" +
                "\t\t\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\t\t\"key\": \"['outputFields'][].['fieldName']\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"notitle\": true\n" +
                "\t\t\t\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\t\t\"htmlClass\": \"col-md-5\",\n" +
                "\t\t\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\t\t\"key\": \"['outputFields'][].['dataType']\",\n" +
                "\t\t\t\t\t\t\t\t\t\t\"notitle\": true\n" +
                "\t\t\t\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t\t]\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"submit\",\n" +
                "\t\t\t\"style\": \"btn-info btn\",\n" +
                "\t\t\t\"htmlClass\": \"text-center\",\n" +
                "\t\t\t\"title\": \"Save\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
		
		Entity config = new Entity();
		config.addKeyValue("FORM", Configform);


		return config;
	}

	@Override
	public Entity getOutput() {
		// TODO Auto-generated method stub
		return input;
	}

	@Override
	public Entity getInput(Component component) {
		// TODO Auto-generated method stub
		return output;
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
        // TODO method updation
		
	}

	
}
