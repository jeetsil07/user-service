package com.ecommece.user_service.service;

import com.ecommece.user_service.core.ApiResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public ApiResponse<UserDto> register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new MissingFieldException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new MissingFieldException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new MissingFieldException("Password is required");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlredyExistException("Email already in use: " + request.getEmail());
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);

        UserDto userDto = UserDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();

        return ApiResponse.<UserDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("User registered successfully")
                .data(userDto)
                .build();
    }
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();

        return ApiResponse.<LoginResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Login successful")
                .data(loginResponse)
                .build();
    }
}
