package com.chatapp.real_time_chat.security.jwt;

import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

@Component
public class JwtUtils {

    public String generateJwtToken(Authentication authentication) {
        // Dummy token for development; replace with real JWT logic
        return "dummy-jwt-token";
    }
}