package com.workflow.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

public class Mapper implements Component{
	
	Entity output;
	ArrayList<String> allowedheaders;
	ArrayList<String> allowedHeadersDatatypes;
	@Override
	public boolean init(Entity config,Entity input,Entity output) {
		
		this.output = output;
		
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
			if(allowedheaders.contains(entry.getKey())) {
				//improve logic here
				String datatype = allowedHeadersDatatypes.get(allowedheaders.indexOf(entry.getKey()));
				if(datatype.equals("boolean")) {
					out.addKeyValue(entry.getKey(), Boolean.parseBoolean((String) entry.getValue()));
				}else if(datatype.equals("int")) {
					out.addKeyValue(entry.getKey(), Integer.parseInt((String) entry.getValue()));
				}else if(datatype.equals("float")) {
					out.addKeyValue(entry.getKey(), Float.parseFloat((String) entry.getValue()));
				}else {
					out.addKeyValue(entry.getKey(), entry.getValue());
				}
				
				
			}
		}
            
		return out;
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
        return "{\n" +
                "  \"schema\": {\n" +
                "    \"type\": \"object\",\n" +
                "    \"title\": \"\",\n" +
                "    \"properties\": {\n" +
                "      \"field\": {\n" +
                "        \"type\": \"array\",\n" +
                "        \"items\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"properties\": {\n" +
                "            \"check\": {\n" +
                "              \"title\": \"\",\n" +
                "              \"type\": \"boolean\"\n" +
                "            },\n" +
                "            \"fieldName\": {\n" +
                "              \"type\": \"string\",\n" +
                "              \"readonly\": true\n" +
                "            },\n" +
                "            \"dataType\": {\n" +
                "              \"type\": \"string\",\n" +
                "              \"readonly\": true\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"required\": [\n" +
                "      \"field\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"form\": [\n" +
                "    {\n" +
                "      \"type\": \"section\",\n" +
                "      \"htmlClass\": \"\",\n" +
                "      \"items\": [\n" +
                "        {\n" +
                "          \"key\": \"field\",\n" +
                "          \"htmlClass\": \"\",\n" +
                "          \"notitle\": true,\n" +
                "          \"add\": null,\n" +
                "          \"remove\": null,\n" +
                "          \"items\": [\n" +
                "            {\n" +
                "              \"type\": \"section\",\n" +
                "              \"htmlClass\": \"form-row redb\",\n" +
                "              \"items\": [\n" +
                "                {\n" +
                "                  \"type\": \"section\",\n" +
                "                  \"htmlClass\": \"col-md-2\",\n" +
                "                  \"items\": [\n" +
                "                    {\n" +
                "                      \"key\": \"['field'][].['check']\",\n" +
                "                      \"type\": \"checkbox\",\n" +
                "                      \"title\": \"\",\n" +
                "                      \"notitle\": true\n" +
                "                    }\n" +
                "                  ]\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"section\",\n" +
                "                  \"htmlClass\": \"form-md-4\",\n" +
                "                  \"items\": [\n" +
                "                    {\n" +
                "                      \"key\": \"['field'][].['fieldName']\",\n" +
                "                      \"notitle\": true\n" +
                "                    }\n" +
                "                  ]\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"section\",\n" +
                "                  \"htmlClass\": \"col-md-4\",\n" +
                "                  \"items\": [\n" +
                "                    {\n" +
                "                      \"key\": \"['field'][].['dataType']\",\n" +
                "                      \"notitle\": true\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"submit\",\n" +
                "      \"style\": \"btn-info\",\n" +
                "      \"title\": \"OK\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
	}

	@Override
	public Entity getOutput() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
