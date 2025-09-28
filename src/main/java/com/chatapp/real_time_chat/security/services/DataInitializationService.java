package com.chatapp.real_time_chat.security.services;

import com.chatapp.real_time_chat.model.ERole;
import com.chatapp.real_time_chat.model.Role;
import com.chatapp.real_time_chat.model.User;
import com.chatapp.real_time_chat.repository.RoleRepository;
import com.chatapp.real_time_chat.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class DataInitializationService implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeRoles();
        initializeAdminUser();
        logger.info("🎯 Data initialization completed successfully!");
    }

    private void initializeRoles() {
        logger.info("🔧 Initializing roles...");

        // Create ROLE_USER if it doesn't exist
        if (!roleRepository.findByName(ERole.ROLE_USER).isPresent()) {
            Role userRole = new Role(ERole.ROLE_USER);
            roleRepository.save(userRole);
            logger.info("✅ Created ROLE_USER");
        }

        // Create ROLE_MODERATOR if it doesn't exist
        if (!roleRepository.findByName(ERole.ROLE_MODERATOR).isPresent()) {
            Role modRole = new Role(ERole.ROLE_MODERATOR);
            roleRepository.save(modRole);
            logger.info("✅ Created ROLE_MODERATOR");
        }

        // Create ROLE_ADMIN if it doesn't exist
        if (!roleRepository.findByName(ERole.ROLE_ADMIN).isPresent()) {
            Role adminRole = new Role(ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);
            logger.info("✅ Created ROLE_ADMIN");
        }

        logger.info("🎯 Roles initialization completed");
    }

    private void initializeAdminUser() {
        logger.info("👤 Initializing admin user...");

        // Check if admin user already exists
        if (!userRepository.findByUsername("admin").isPresent()) {
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@chatapp.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEnabled(true);

            // Assign all roles to admin
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleRepository.findByName(ERole.ROLE_USER).get());
            adminRoles.add(roleRepository.findByName(ERole.ROLE_MODERATOR).get());
            adminRoles.add(roleRepository.findByName(ERole.ROLE_ADMIN).get());
            admin.setRoles(adminRoles);

            userRepository.save(admin);
            logger.info("✅ Created admin user - Username: admin, Password: admin123");
        }

        // Create a demo regular user
        if (!userRepository.findByUsername("demo").isPresent()) {
            User demoUser = new User();
            demoUser.setUsername("demo");
            demoUser.setEmail("demo@chatapp.com");
            demoUser.setPassword(passwordEncoder.encode("demo123"));
            demoUser.setFirstName("Demo");
            demoUser.setLastName("User");
            demoUser.setEnabled(true);

            // Assign USER role only
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(roleRepository.findByName(ERole.ROLE_USER).get());
            demoUser.setRoles(userRoles);

            userRepository.save(demoUser);
            logger.info("✅ Created demo user - Username: demo, Password: demo123");
        }

        logger.info("🎯 Users initialization completed");
    }
}