package com.workflow.service.runmanager;

import com.workflow.bean.GraphNode;
import com.workflow.bean.LogicGraph;
import com.workflow.bean.WFGraph;
import com.workflow.component.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Service("DefaultRM")
public class DefaultRunManager implements RunManager {

    @Autowired
    SimpMessagingTemplate template;


    @Override
    public ArrayList<String> run(LogicGraph logicGraph, Entity runConfig) {

        this.template.convertAndSend("/chat", new SimpleDateFormat("HH:mm:ss").format(new Date())  + "- " + "Running In Progressss");
        ArrayList<String> status=new ArrayList<>();
        boolean anchor;
        ArrayList<GraphNode> flow=logicGraph.getNodes();
        int phaseStart=0, phaseEnd=0;
        for (int i=0;i<flow.size();i++){
            flow.get(i).getComponent().init();
        }
        do {
            while(phaseEnd!=flow.size() && !flow.get(phaseEnd).getCategory().equals("Phase")){
                phaseEnd++;
            }
            int count=0;
            do {
                count++;
                anchor=false;
                int i=phaseStart;
                Entity io=null;
                try{
                    io = flow.get(phaseStart).getComponent().process(io);
                    for (i +=1; i < phaseEnd; i++) {
                        if(io!=null){
                            anchor=true;
                        }
                        io = flow.get(i).getComponent().process(io);
                    }
                }catch (Exception e){
                    status.add("Problem faced during execution of "+flow.get(phaseEnd).getName());
                    status.add("Problem at: "+flow.get(i).getName());
                    status.add("Cause: "+e.getMessage());
                    e.printStackTrace();
                    return status;
                }
            } while (anchor);
            phaseStart=phaseEnd+1;
            phaseEnd++;
            System.out.println("phase over count:"+count+"\nphasestart:"+phaseStart+"\nphaseend:"+phaseEnd);
        }while (phaseEnd<flow.size());
        status.add("success");
        this.template.convertAndSend("/chat", new SimpleDateFormat("HH:mm:ss").format(new Date())  + "- " + "Running");
        return status;
    }
}
