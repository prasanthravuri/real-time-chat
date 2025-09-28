package com.chatapp.real_time_chat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "✅ Server is running! Time: " + java.time.LocalDateTime.now();
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK - WebSocket server is up";
    }

    @GetMapping("/ws-test")
    public String wsTest() {
        return "WebSocket endpoint should be available at: /ws";
    }
}