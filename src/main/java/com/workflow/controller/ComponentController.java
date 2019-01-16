package com.workflow.controller;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import com.workflow.service.ComponentService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.CSVReader;
import com.workflow.annotation.wfComponent;
import com.workflow.bean.ComponentRepository;
import com.workflow.component.Entity;
import com.workflow.service.Helper;

@RestController
public class ComponentController {

	@Autowired
	Helper helper;

	@Autowired
	ComponentService componentService;
	
	@RequestMapping(value="/getConfig/{WFId}/{componentId}", method= RequestMethod.GET)
	public ResponseEntity<HashMap> getConfig(@PathVariable("WFId") String WFId, @PathVariable("componentId") String CId){
		
		Entity config = componentService.getConfig(WFId, CId);
		System.out.println(config.getEntity());
		//add config model to the response and send back the entity.
		if(config==null) {

			HashMap obj=new HashMap<String,String>();
			obj.put("message","No config found");
			return new ResponseEntity<>(obj,HttpStatus.INTERNAL_SERVER_ERROR);
		}else {
			return new ResponseEntity<>(config.getEntity(),HttpStatus.OK);
		}
		
		
		
	}

	@RequestMapping(value="/setConfig/{WFId}/{componentId}", method= RequestMethod.POST)
	public ResponseEntity<HashMap> setConfig(@RequestBody JSONObject config, @PathVariable("WFId") String WFId, @PathVariable("componentId") String CId){

		System.out.println(config.toJSONString());

		JSONParser parser=new JSONParser();
		Entity pass=new Entity();
		HashMap<String,Object> hmap=new HashMap<>();
		Set<String> keys=config.keySet();
		Iterator it=keys.iterator();
		while(it.hasNext()){
		    String key=it.next().toString();
		    hmap.put(key,config.get(key));
        }
		System.out.println(hmap.toString());
		if(hmap.isEmpty()) {
			HashMap<String,String> ret=new HashMap<>();
			ret.put("message", "Success");
			return new ResponseEntity<>(ret, HttpStatus.OK);
		}
		else{
		    pass.setEntity(hmap);
		    String res=componentService.setConfig(WFId,CId,pass);
		    HashMap<String,String> ret=new HashMap<>();
		    ret.put("message", res);
		    if(res.equals("Success"))
                return new ResponseEntity<>(ret,HttpStatus.OK);
            return new ResponseEntity<>(ret,HttpStatus.INTERNAL_SERVER_ERROR);
        }

	}
	
	@RequestMapping(value="/getInput/{WFId}/{componentId}", method=RequestMethod.GET)
	public ResponseEntity<ArrayList<JSONObject>> getInput(@PathVariable("WFId") String WFId, @PathVariable("componentId") String CId){
		Entity response=componentService.getInput(WFId, CId);
		return new ResponseEntity<>((ArrayList<JSONObject>)response.getEntity().get("input"),HttpStatus.OK);
	}

	@RequestMapping(value="/getOutput/{WFId}/{componentId}", method=RequestMethod.GET)
	public ResponseEntity<ArrayList<JSONObject>> getOutput(@PathVariable("WFId") String WFId, @PathVariable("componentId") String CId){
		Entity response=componentService.getOutput(WFId, CId);
		if(response==null){
			return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>((ArrayList<JSONObject>)response.getEntity().get("output"),HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value="/components", method=RequestMethod.GET)
	public ResponseEntity<String> getAllComponents() throws ParseException{
		
		ArrayList<String> components = new ArrayList<>();
		/*components.add("Start");
		components.add("End");
		
		//addition of @component annotation and reading all component classes
		ClassPathScanningCandidateComponentProvider scanner =
				new ClassPathScanningCandidateComponentProvider(true);
		scanner.addIncludeFilter(new AnnotationTypeFilter(wfComponent.class));
		for (BeanDefinition bd : scanner.findCandidateComponents("com.workflow.component")) {
			try {
				Class<?> cls = Class.forName(bd.getBeanClassName().toString());
				System.out.println(cls.isAnnotationPresent(wfComponent.class));
				if(cls.getAnnotation(wfComponent.class).complete()) {
					components.add(bd.getBeanClassName().replace("com.workflow.component.", ""));
				}
				
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}*/
		ComponentRepository repo = ComponentRepository.getInstance();
		components = repo.components;
		    
		
		JSONArray array = new JSONArray();
		
		
		for(String c : components) {
			JSONObject obj = new JSONObject();
			obj.put("category", c);
			obj.put("text", c);
            obj.put("key", c);
			array.add(obj);
		}
		//"{\"pallets\":"+array.toString()+"}"
		//new JSONObject().put("pallete", array).toString().replaceAll("\\\\","")
		return new ResponseEntity<String>("{\"pallete\":"+array.toString().replaceAll("\\\\","")+"}",HttpStatus.OK);
	}
	
}
