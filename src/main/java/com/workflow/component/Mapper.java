	package com.workflow.component;

	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map.Entry;

	import com.mongodb.util.JSON;
	import org.json.JSONArray;
	import org.json.JSONObject;

	import com.workflow.annotation.wfComponent;
	import org.json.simple.parser.JSONParser;
	import org.json.simple.parser.ParseException;

	@wfComponent(complete=true)
	public class Mapper implements Component{

		Entity input;
		Entity output;
		Entity config;

		ArrayList<String> allowedheaders;
		ArrayList<String> allowedHeadersDatatypes;
		HashMap<String,String> newLabels;
		@Override
		public boolean init() {
			input = new Entity();
			allowedheaders = new ArrayList<>();
			allowedHeadersDatatypes = new ArrayList<>();
			newLabels = new HashMap<>();
			JSONArray temp =new JSONArray((List<JSONObject>)this.output.getEntity().get("output"));

			for(int i=0;i<temp.length();i++) {
				JSONObject obj = new JSONObject(temp.get(i).toString());
				String header = obj.getString("fieldName");
				String datatype = obj.getString("dataType");
				String newLabel = obj.getString("newFieldName");
				allowedheaders.add(header);
				allowedHeadersDatatypes.add(datatype);
				newLabels.put(header,newLabel);
			}

			return true;
		}

		@Override
		public Entity process(Entity input) {
			if(input==null)
				return null;
			Entity out= new Entity();
			HashMap<String, Object> in = input.getEntity();
			for (Entry<String, Object> entry : in.entrySet()) {
				try {
					if (allowedheaders.contains(entry.getKey())) {
						//improve logic here
						String datatype = allowedHeadersDatatypes.get(allowedheaders.indexOf(entry.getKey()));

						try {
							if (datatype.equals("boolean")) {
								out.addKeyValue(newLabels.get(entry.getKey()), Boolean.parseBoolean((String) entry.getValue()));
							} else if (datatype.equals("int")) {
								out.addKeyValue(newLabels.get(entry.getKey()), Integer.parseInt((String) entry.getValue()));
							} else if (datatype.equals("float")) {
								out.addKeyValue(newLabels.get(entry.getKey()), Float.parseFloat((String) entry.getValue()));
							} else {
								out.addKeyValue(newLabels.get(entry.getKey()), entry.getValue());
							}
						}catch (Exception e){
							out.addKeyValue(newLabels.get(entry.getKey()),Double.NaN);
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
					out.addKeyValue(newLabels.get(entry.getKey()), 0);
				}


			}

			return out;
		}

		@Override
		public Entity getConfig() {

			// TODO Auto-generated method stub
			String Configform = "{\"schema\":{\"type\":\"object\",\"title\":\"\",\"properties\":{\"field\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"check\":{\"title\":\"\",\"type\":\"boolean\"},\"fieldName\":{\"type\":\"string\",\"readonly\":true},\"dataType\":{\"type\":\"string\",\"readonly\":true,\"enum\":[\"boolean\",\"int\",\"float\",\"String\"]}}}},\"outputFields\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"check\":{\"title\":\"\",\"type\":\"boolean\"},\"newFieldName\":{\"type\":\"string\",\"readonly\":false},\"dataType\":{\"type\":\"string\",\"readonly\":false,\"enum\":[\"boolean\",\"int\",\"float\",\"String\"]}}}}},\"required\":[\"field\"]},\"form\":[{\"type\":\"button\",\"title\":\"Add >\",\"style\":\"btn-info float-btn\",\"htmlClass\":\"text-center\",\"onClick\":\"mapperHandler()\"},{\"type\":\"button\",\"title\":\"Add All\",\"style\":\"btn-info float-btn2\",\"htmlClass\":\"text-center\",\"onClick\":\"mapperAddAllHandler()\"},{\"type\":\"section\",\"htmlClass\":\"row\",\"items\":[{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[{\"key\":\"field\",\"htmlClass\":\"\",\"notitle\":false,\"add\":null,\"remove\":null,\"items\":[{\"type\":\"section\",\"htmlClass\":\"form-row\",\"items\":[{\"type\":\"section\",\"htmlClass\":\"col-md-2\",\"items\":[{\"key\":\"['field'][].['check']\",\"type\":\"checkbox\",\"title\":\"\",\"notitle\":true}]},{\"type\":\"section\",\"htmlClass\":\"col-md-4\",\"items\":[{\"key\":\"['field'][].['fieldName']\",\"notitle\":true}]},{\"type\":\"section\",\"htmlClass\":\"col-md-4\",\"items\":[{\"key\":\"['field'][].['dataType']\",\"notitle\":true}]}]}]}]},{\"type\":\"section\",\"htmlClass\":\"col-md-6\",\"items\":[{\"key\":\"outputFields\",\"htmlClass\":\"\",\"notitle\":false,\"add\":null,\"remove\":null,\"startEmpty\":true,\"items\":[{\"type\":\"section\",\"htmlClass\":\"form-row\",\"items\":[{\"type\":\"section\",\"htmlClass\":\"col-md-5\",\"items\":[{\"key\":\"['outputFields'][].['newFieldName']\",\"notitle\":true}]},{\"type\":\"section\",\"htmlClass\":\"col-md-5\",\"items\":[{\"key\":\"['outputFields'][].['dataType']\",\"notitle\":true}]}]}]}]}]},{\"type\":\"submit\",\"style\":\"btn-info btn\",\"htmlClass\":\"text-center\",\"title\":\"Save\"}]}";

			JSONObject obj = new JSONObject(Configform);
			Entity config = new Entity();
			config.addKeyValue("FORM", Configform);

			HashMap<String,Object> model = new HashMap<>();

			if(this.config.getEntity().keySet().size()>2){
				config.addKeyValue("MODEL",this.config.getEntity());
			}else {
				if (input == null) {
					input = new Entity();
					model.put("field", new ArrayList<JSONObject>());
				} else {
					model.put("field", input.getEntity().get("input"));
				}
				if (output == null) {
					output = new Entity();
					model.put("outputFields", new ArrayList<JSONObject>());
				} else {
					model.put("outputFields", output.getEntity().get("output"));
				}
				config.addKeyValue("MODEL", model);
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
			setInput(component.getOutput());
			return input;
		}

		@Override
		public void setInput(Entity input) {
			// TODO Auto-generated method stub

			this.input = new Entity();
			//this.input.addKeyValue("input", input.getObjectByName("output"));
			for(String h : this.input.getEntity().keySet()){
				if(!input.getEntity().keySet().contains(h)){
					output=null;
					break;
				}
			}
			if(input!=null)
			this.input.addKeyValue("input", input.getObjectByName("output"));
			else this.input=null;

		}

		@Override
		public void setOutput(Entity output) {
			this.output = new Entity();
			if(this.config.getEntity().get("outputFields")==null){
				this.output.addKeyValue("output",new ArrayList<JSONObject>());
			}else{
				this.output.addKeyValue("output",this.config.getEntity().get("outpu	tFields"));
			}

		}

		@Override
		public void setConfig(Entity config) {
			// TODO method updation
			this.config = config;
			setOutput(null);
		}

		@Override
		public boolean isValid() {
			return true;
		}
	}
