package com.chatapp.real_time_chat.repository;

import com.chatapp.real_time_chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by username
    Optional<User> findByUsername(String username);
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Check if username exists
    Boolean existsByUsername(String username);
    
    // Check if email exists
    Boolean existsByEmail(String email);
    
    // Find all online users
    List<User> findByOnlineTrue();
    
    // Find all enabled users
    List<User> findByEnabledTrue();
    
    // Find users created after a specific date
    List<User> findByCreatedAtAfter(LocalDateTime date);
    
    // Search users by username or name
    @Query("SELECT u FROM User u WHERE u.username LIKE %?1% OR u.firstName LIKE %?1% OR u.lastName LIKE %?1%")
    List<User> findByUsernameOrNameContaining(String search);
    
    // Count online users
    @Query("SELECT COUNT(u) FROM User u WHERE u.online = true")
    Long countOnlineUsers();
    
    // Count users registered since a date
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= ?1")
    Long countUsersSince(LocalDateTime date);
}