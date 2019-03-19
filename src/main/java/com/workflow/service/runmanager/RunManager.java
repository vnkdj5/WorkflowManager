package com.workflow.service.runmanager;

import com.workflow.bean.LogicGraph;
import com.workflow.bean.WFGraph;
import com.workflow.component.Entity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public interface RunManager {

    ArrayList<String> run(LogicGraph logicGraph, Entity runConfig);

}
