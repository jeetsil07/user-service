package com.ecommece.user_service.service;

import com.ecommece.user_service.core.ApiResponse;
import com.ecommece.user_service.core.UserRole;
import com.ecommece.user_service.dto.request.LoginRequest;
import com.ecommece.user_service.dto.request.RegisterRequest;
import com.ecommece.user_service.dto.response.LoginResponse;
import com.ecommece.user_service.dto.response.UserDto;
import com.ecommece.user_service.entity.User;
import com.ecommece.user_service.exception.InvalidCredentialsException;
import com.ecommece.user_service.exception.MissingFieldException;
import com.ecommece.user_service.exception.UserAlredyExistException;
import com.ecommece.user_service.exception.UserNotFoundException;
import com.ecommece.user_service.repository.UserRepository;
import com.ecommece.user_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setup() {
        registerRequest = RegisterRequest.builder()
                .username("jeet")
                .email("jeet@example.com")
                .password("password123")
                .role(UserRole.USER)
                .build();

        loginRequest = LoginRequest.builder()
                .email("jeet@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .username("jeet")
                .email("jeet@example.com")
                .password("encodedPassword")
                .role("USER")
                .build();
    }

    // =======================
    // Register Tests
    // =======================

    @Test
    @DisplayName("Should register user successfully")
    void registerUserSuccess() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        ApiResponse<UserDto> response = authService.register(registerRequest);

        assertEquals(201, response.getStatus());
        assertEquals("User registered successfully", response.getMessage());
        assertEquals("jeet", response.getData().getUsername());

        // Verify saved user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encodedPassword", userCaptor.getValue().getPassword());
    }

    @Test
    @DisplayName("Should throw MissingFieldException if username is blank")
    void registerMissingUsername() {
        registerRequest.setUsername(null);
        assertThrows(MissingFieldException.class, () -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistException if email already exists")
    void registerEmailAlreadyExists() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));
        assertThrows(UserAlredyExistException.class, () -> authService.register(registerRequest));
    }

    // =======================
    // Login Tests
    // =======================

    @Test
    @DisplayName("Should login successfully")
    void loginSuccess() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getEmail(), user.getRole())).thenReturn("jwtToken");

        ApiResponse<LoginResponse> response = authService.login(loginRequest);

        assertEquals(200, response.getStatus());
        assertEquals("Login successful", response.getMessage());
        assertEquals("jeet", response.getData().getUsername());
        assertEquals("USER", response.getData().getRole());
        assertEquals("jwtToken", response.getData().getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for wrong credentials")
    void loginInvalidCredentials() {
        doThrow(new RuntimeException("Auth failed"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if user not found")
    void loginUserNotFound() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest));
    }
}

