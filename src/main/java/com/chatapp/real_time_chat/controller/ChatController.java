package com.chatapp.real_time_chat.controller;

import com.chatapp.real_time_chat.model.ChatMessage;
import com.chatapp.real_time_chat.model.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    // Track active users in each room for group chat features
    private final Map<String, AtomicInteger> roomParticipants = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> roomUsers = new ConcurrentHashMap<>();

    // Handle regular group chat messages
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, 
                                  @Payload ChatMessage chatMessage) {
        
        logger.info("💬 Group message in room '{}': {} says '{}'", 
                   roomId, chatMessage.getSender(), chatMessage.getContent());
        
        chatMessage.setRoom(roomId);
        chatMessage.setType(MessageType.CHAT);
        
        return chatMessage;
    }

    // Handle user joining a group chat room
    @MessageMapping("/chat/{roomId}/addUser")
    @SendTo("/topic/{roomId}")
    public ChatMessage addUser(@DestinationVariable String roomId,
                              @Payload ChatMessage chatMessage,
                              SimpMessageHeaderAccessor headerAccessor) {
        
        // Store user info in WebSocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("room", roomId);
        
        // Update room participants count
        roomParticipants.computeIfAbsent(roomId, k -> new AtomicInteger(0)).incrementAndGet();
        
        // Track users in room
        roomUsers.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                 .put(chatMessage.getSender(), "online");
        
        int participantCount = roomParticipants.get(roomId).get();
        
        logger.info("👥 {} joined group room '{}' (Total participants: {})", 
                   chatMessage.getSender(), roomId, participantCount);
        
        // Set join message details
        chatMessage.setType(MessageType.JOIN);
        chatMessage.setRoom(roomId);
        chatMessage.setContent(chatMessage.getSender() + " joined the group chat!");
        
        return chatMessage;
    }

    // Handle typing indicators for group chats
    @MessageMapping("/chat/{roomId}/typing")
    @SendTo("/topic/{roomId}/typing")
    public TypingIndicator handleTyping(@DestinationVariable String roomId,
                                       @Payload TypingIndicator indicator) {
        
        logger.debug("⌨️ Typing indicator in room '{}': {} is {}", 
                    roomId, indicator.getSender(), 
                    indicator.isTyping() ? "typing" : "stopped typing");
        
        indicator.setRoom(roomId);
        return indicator;
    }

    // Handle group room info requests
    @MessageMapping("/chat/{roomId}/info")
    @SendToUser("/queue/reply")
    public RoomInfo getRoomInfo(@DestinationVariable String roomId) {
        
        int participantCount = roomParticipants.getOrDefault(roomId, new AtomicInteger(0)).get();
        Map<String, String> users = roomUsers.getOrDefault(roomId, new ConcurrentHashMap<>());
        
        logger.info("ℹ️ Room info requested for '{}': {} participants", roomId, participantCount);
        
        return new RoomInfo(roomId, participantCount, users.keySet());
    }

    // Handle private messages within group (future feature)
    @MessageMapping("/private/{roomId}")
    public void sendPrivateMessage(@DestinationVariable String roomId,
                                  @Payload ChatMessage message,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String sender = (String) headerAccessor.getSessionAttributes().get("username");
        message.setSender(sender);
        logger.info("📨 Private message in room '{}' from {} to {}", 
                   roomId, sender, message.getRoom());
    }

    // Get room statistics
    @MessageMapping("/chat/{roomId}/stats")
    @SendToUser("/queue/stats")
    public RoomStats getRoomStats(@DestinationVariable String roomId) {
        int participantCount = roomParticipants.getOrDefault(roomId, new AtomicInteger(0)).get();
        Map<String, String> users = roomUsers.getOrDefault(roomId, new ConcurrentHashMap<>());
        
        return new RoomStats(roomId, participantCount, users.size());
    }

    // Handle user leaving group (called by WebSocketEventListener)
    public void handleUserLeaveGroup(String username, String roomId) {
        if (roomId != null && username != null) {
            // Update participants count
            AtomicInteger count = roomParticipants.get(roomId);
            if (count != null && count.get() > 0) {
                count.decrementAndGet();
            }
            
            // Remove user from room tracking
            Map<String, String> users = roomUsers.get(roomId);
            if (users != null) {
                users.remove(username);
            }
            
            int remainingCount = count != null ? count.get() : 0;
            logger.info("👋 {} left group room '{}' (Remaining participants: {})", 
                       username, roomId, remainingCount);
        }
    }

    // Get current participant count for a room
    public int getRoomParticipantCount(String roomId) {
        return roomParticipants.getOrDefault(roomId, new AtomicInteger(0)).get();
    }

    // Check if user is in room
    public boolean isUserInRoom(String username, String roomId) {
        Map<String, String> users = roomUsers.get(roomId);
        return users != null && users.containsKey(username);
    }

    // Inner classes for additional group chat features

    /**
     * Typing indicator for group chat
     */
    public static class TypingIndicator {
        private String sender;
        private boolean typing;
        private String room;
        private long timestamp;

        // Default constructor
        public TypingIndicator() {
            this.timestamp = System.currentTimeMillis();
        }

        public TypingIndicator(String sender, boolean typing, String room) {
            this();
            this.sender = sender;
            this.typing = typing;
            this.room = room;
        }

        // Getters and setters
        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public boolean isTyping() {
            return typing;
        }

        public void setTyping(boolean typing) {
            this.typing = typing;
        }

        public String getRoom() {
            return room;
        }

        public void setRoom(String room) {
            this.room = room;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * Room information for group chat
     */
    public static class RoomInfo {
        private String roomName;
        private int participantCount;
        private Set<String> participants;
        private String createdAt;

        public RoomInfo(String roomName, int participantCount, Set<String> participants) {
            this.roomName = roomName;
            this.participantCount = participantCount;
            this.participants = participants;
            this.createdAt = java.time.LocalDateTime.now().toString();
        }

        // Getters and setters
        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public int getParticipantCount() {
            return participantCount;
        }

        public void setParticipantCount(int participantCount) {
            this.participantCount = participantCount;
        }

        public Set<String> getParticipants() {
            return participants;
        }

        public void setParticipants(Set<String> participants) {
            this.participants = participants;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    /**
     * Room statistics for analytics
     */
    public static class RoomStats {
        private String roomId;
        private int currentParticipants;
        private int totalUsers;
        private String lastActivity;

        public RoomStats(String roomId, int currentParticipants, int totalUsers) {
            this.roomId = roomId;
            this.currentParticipants = currentParticipants;
            this.totalUsers = totalUsers;
            this.lastActivity = java.time.LocalDateTime.now().toString();
        }

        // Getters and setters
        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public int getCurrentParticipants() {
            return currentParticipants;
        }

        public void setCurrentParticipants(int currentParticipants) {
            this.currentParticipants = currentParticipants;
        }

        public int getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(int totalUsers) {
            this.totalUsers = totalUsers;
        }

        public String getLastActivity() {
            return lastActivity;
        }

        public void setLastActivity(String lastActivity) {
            this.lastActivity = lastActivity;
        }
    }
}