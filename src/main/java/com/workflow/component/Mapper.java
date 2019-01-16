package com.workflow.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import com.workflow.annotation.wfComponent;

@wfComponent(complete=true)
public class Mapper implements Component{
	
	Entity input;
	Entity output;
	Entity config;

	ArrayList<String> allowedheaders;
	ArrayList<String> allowedHeadersDatatypes;
	@Override
	public boolean init() {
		input = new Entity();
		allowedheaders = new ArrayList<>();
		allowedHeadersDatatypes = new ArrayList<>();
		JSONArray temp =new JSONArray((List<JSONObject>)this.output.getEntity().get("output"));
		
		for(int i=0;i<temp.length();i++) {
			JSONObject obj = new JSONObject(temp.get(i).toString());
			String header = obj.getString("fieldName");
			String datatype = obj.getString("dataType");
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
                "\t\t\t\"title\": \"Add >\",\n" +
                "\t\t\t\"style\": \"btn-info float-btn\",\n" +
                "\t\t\t\"htmlClass\": \"text-center\",\n" +
                "\t\t\t\"onClick\": \"mapperHandler()\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"button\",\n" +
                "\t\t\t\"title\": \"Add All\",\n" +
                "\t\t\t\"style\": \"btn-info float-btn2\",\n" +
                "\t\t\t\"htmlClass\": \"text-center\",\n" +
                "\t\t\t\"onClick\": \"mapperAddAllHandler()\"\n" +
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

		HashMap<String,Object> model = new HashMap<>();

		if(this.config==null) {
            if (input == null) {
                input = new Entity();
                model.put("field", new ArrayList<JSONObject>());
            } else {
                model.put("field", output.getEntity().get("input"));
            }
			if (output == null) {
				output = new Entity();
				model.put("outputFields", new ArrayList<JSONObject>());
			} else {
				model.put("outputFields", output.getEntity().get("output"));
			}
			config.addKeyValue("MODEL", model);
		}else{
			config.addKeyValue("MODEL",this.config.getEntity());
		}
        return config;
	}

	@Override
	public Entity getOutput() {
		// TODO Auto-generated method stub
		return output;
	}

	@Override
    public Entity getInput(Component component) {
		// TODO Auto-generated method stub
		System.out.println("MAPPER GETINPUT"+component.getOutput().toString());
		setInput(component.getOutput());
		return input;
	}

	@Override
	public void setInput(Entity input) {
		// TODO Auto-generated method stub
		this.input = new Entity();
		this.input.addKeyValue("input", input.getObjectByName("output"));

	}

	@Override
	public void setOutput(Entity output) {
		this.output = new Entity();
		this.output.addKeyValue("output",this.config.getEntity().get("outputFields"));
	}

	@Override
	public void setConfig(Entity config) {
        // TODO method updation
		this.config = config;
		//init(config);
		setOutput(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}
}
