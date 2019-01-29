package com.workflow.service.runmanager;

import com.workflow.bean.GraphNode;
import com.workflow.bean.LogicGraph;
import com.workflow.bean.WFGraph;
import com.workflow.component.Entity;

import java.util.ArrayList;

public class DefaultRunManager implements RunManager {
    @Override
    public void run(LogicGraph logicGraph, Entity runConfig) {
        Entity io=null;
        boolean anchor=false;
        ArrayList<GraphNode> flow=logicGraph.getNodes();
        for (int i=0;i<flow.size();i++){
            flow.get(i).getComponent().init();
        }
        do{
            io=flow.get(0).getComponent().process(io);
            if(io!=null) {
                anchor=true;
            }else break;
            for (int i=1;i<flow.size();i++){
                io=flow.get(i).getComponent().process(io);
            }
        }while (anchor);
    }
}
