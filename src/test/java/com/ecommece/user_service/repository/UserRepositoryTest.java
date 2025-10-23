package com.ecommece.user_service.repository;

import com.ecommece.user_service.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {
    @Mock
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setup(){
        user = User.builder()
                .id(1L)
                .username("jeet")
                .email("jeet@example.com")
                .password("password123")
                .role("USER")
                .build();
    }

    @Test
    @DisplayName("Should save user data correctly")
    void saveUserTest(){
        when(userRepository.save(any(User.class))).thenReturn(user);

        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        assertEquals("jeet", saved.getUsername());
        assertEquals("jeet@example.com", saved.getEmail());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should show user data correctly")
    void testFindByEmailReturnsUser() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Optional<User> found = userRepository.findByEmail(user.getEmail());

        assertTrue(found.isPresent());
        assertEquals("jeet", found.get().getUsername());

        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Should show user data not found")
    void testFindByEmailReturnsEmpty() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Optional<User> found = userRepository.findByEmail("notfound@example.com");

        assertFalse(found.isPresent());
        verify(userRepository, times(1)).findByEmail("notfound@example.com");
    }
}
