package com.chatapp.real_time_chat.repository;

import com.chatapp.real_time_chat.model.ERole;
import com.chatapp.real_time_chat.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Find role by name (ROLE_USER, ROLE_ADMIN, etc.)
    Optional<Role> findByName(ERole name);
}