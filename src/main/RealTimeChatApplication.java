package com.chatapp.real_time_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;

@SpringBootApplication
public class RealTimeChatApplication {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(RealTimeChatApplication.class, args);
    }

    @PostConstruct
    public void init() {
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles.length == 0) {
            System.out.println("🚀 Real-Time Chat Application Started in DEVELOPMENT mode!");
            System.out.println("🌐 Open your browser: http://localhost:8080");
        } else {
            System.out.println("🚀 Real-Time Chat Application Started in " + 
                             String.join(", ", activeProfiles).toUpperCase() + " mode!");
        }
    }
}