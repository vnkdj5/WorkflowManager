package com.workflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class WebSocetController {

    private final SimpMessagingTemplate template;

    @Autowired
    WebSocetController(SimpMessagingTemplate template) {
        this.template = template;
    }

    //@MessageMapping("/send/message?wfid=dd")
    //@Scheduled(fixedDelay = 2000)
    public void onReceiveMessage() {
        this.template.convertAndSend("/chat", new SimpleDateFormat("HH:mm:ss").format(new Date())  + "- " + "chahhhaha");
    }
}
