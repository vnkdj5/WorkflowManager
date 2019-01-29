package com.workflow.service.runmanager;

import com.workflow.bean.LogicGraph;
import com.workflow.bean.WFGraph;
import com.workflow.component.Entity;

import java.util.HashMap;

public interface RunManager {

    void run(LogicGraph logicGraph, Entity runConfig);

}
