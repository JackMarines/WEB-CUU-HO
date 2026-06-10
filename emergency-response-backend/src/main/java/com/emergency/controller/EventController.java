package com.emergency.controller;

import com.emergency.service.EventEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventEmitter eventEmitter;

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return eventEmitter.subscribe();
    }
}
