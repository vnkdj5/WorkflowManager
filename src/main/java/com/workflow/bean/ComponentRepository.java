package com.workflow.bean;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.mongodb.annotations.Beta;
import com.workflow.annotation.wfComponent;

public class ComponentRepository {
	private static ComponentRepository mComponentRepository=null;
	
	public ArrayList<String> components;
	
	
	private ComponentRepository() {
		components = new ArrayList<>();
		components.add("Start");
		components.add("End");
		
		//addition of @component annotation and reading all component classes
		ClassPathScanningCandidateComponentProvider scanner =
				new ClassPathScanningCandidateComponentProvider(true);
		scanner.addIncludeFilter(new AnnotationTypeFilter(wfComponent.class));
		for (BeanDefinition bd : scanner.findCandidateComponents("com.workflow.component")) {
			try {
				Class<?> cls = Class.forName(bd.getBeanClassName().toString());
				if(cls.getAnnotation(wfComponent.class).complete()) {
					components.add(bd.getBeanClassName().replace("com.workflow.component.", ""));
				}
				
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	//initilization or contextaware
	public static ComponentRepository getInstance(){
		if(mComponentRepository==null) {
			
			mComponentRepository = new ComponentRepository();
		}
		
		return mComponentRepository;
	}
}