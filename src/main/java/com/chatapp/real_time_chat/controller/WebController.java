package com.chatapp.real_time_chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    // Home page - shows login/register
    @GetMapping("/")
    public String index() {
        return "index"; // Show login/register page
    }

    // Dashboard page - after login
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "dashboard"; // Show full dashboard
    }

    // Login page (alternative route)
    @GetMapping("/login")
    public String login() {
        return "index";
    }

    // Register page (alternative route)
    @GetMapping("/register")
    public String register() {
        return "index";
    }

    // Chat room page
    @GetMapping("/chat")
    public String chatRoom(@RequestParam(name = "username", required = false, defaultValue = "Anonymous") String username,
                          @RequestParam(name = "room", required = false, defaultValue = "general") String room,
                          Model model) {
        
        // Pass data to the HTML template
        model.addAttribute("username", username);
        model.addAttribute("room", room);
        
        return "chat"; // This will look for chat.html in templates folder
    }

    // Specific room URL
    @GetMapping("/room/{roomName}")
    public String specificRoom(@PathVariable String roomName,
                              @RequestParam(name = "username", required = false, defaultValue = "Anonymous") String username,
                              Model model) {
        
        model.addAttribute("username", username);
        model.addAttribute("room", roomName);
        return "chat";
    }

    // Test pages
    @GetMapping("/test-websocket")
    public String testWebSocket() {
        return "test-websocket";
    }

    @GetMapping("/test-simple")
    public String testSimple() {
        return "test-simple";
    }

    // Logout success
    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/";
    }
}