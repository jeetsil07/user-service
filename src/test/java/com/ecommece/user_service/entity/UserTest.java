package com.ecommece.user_service.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testUserBuilderAndGetters(){
        User user = User.builder()
                .id(1L)
                .username("jeet")
                .email("jeet@example.com")
                .password("password123")
                .role("USER")
                .build();

        assertEquals(1L,user.getId());
        assertEquals("jeet",user.getUsername());
        assertEquals("jeet@example.com",user.getEmail());
        assertEquals("password123",user.getPassword());
        assertEquals("USER",user.getRole());
    }

    @Test
    @DisplayName("should use USER as default role")
    void testDefaultRoleValue() {
        User user = new User();
        assertEquals("USER", user.getRole()); // default role from entity
    }
}
