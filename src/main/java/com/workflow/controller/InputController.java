package com.workflow.controller;

import com.workflow.service.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InputController {

    @Autowired
    Helper help;

    @Autowired
    MongoTemplate mongoTemplate;

    @RequestMapping(value="/getInput/{component_name}", method= RequestMethod.GET)
    public ResponseEntity<String> getInput(@PathVariable("component_name") String componentName){

        return new ResponseEntity<String>(help.getObjectByClassName(componentName).getInput(null).toString(), HttpStatus.OK);
    }
}
