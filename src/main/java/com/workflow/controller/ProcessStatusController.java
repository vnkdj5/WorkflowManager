package com.workflow.controller;

import org.json.HTTP;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Controller
public class ProcessStatusController {

    private final List<SseEmitter> emitters = new ArrayList<>();

    @RequestMapping(path = "/stream", method = RequestMethod.GET)
    public SseEmitter stream() throws IOException {

        SseEmitter emitter = new SseEmitter();

        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        System.out.println("Emitter started");
        return emitter;
    }

    @RequestMapping(path = "/events", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> handle() {
        String message = "Message{" +
                "from='" + "Vaibhav" + '\'' +
                ", message='" + "hello" + '\'' +
                '}';
        emitters.forEach((SseEmitter emitter) -> {
            try {
                for (int i = 0; i < 10; i++) {
                    emitter.send(message, MediaType.APPLICATION_JSON);
                }
                emitter.send(message, MediaType.APPLICATION_JSON);
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
                e.printStackTrace();
            }
        });
        ;


        return new ResponseEntity<String>(message, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(path = "/chat", method = RequestMethod.POST)
    public Message sendMessage(Message message) {


        emitters.forEach((SseEmitter emitter) -> {
            try {
                for (int i = 0; i < 10; i++) {
                    emitter.send(message, MediaType.APPLICATION_JSON);
                }
                emitter.send(message, MediaType.APPLICATION_JSON);
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
                e.printStackTrace();
            }
        });
        return message;
    }

}
