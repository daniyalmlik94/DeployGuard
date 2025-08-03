package com.daniyalmlik.deployguard.users;

import com.daniyalmlik.deployguard.common.UserAlreadyExistsException;
import com.daniyalmlik.deployguard.security.JwtService;
import com.daniyalmlik.deployguard.users.dto.AuthResponse;
import com.daniyalmlik.deployguard.users.dto.LoginRequest;
import com.daniyalmlik.deployguard.users.dto.RefreshRequest;
import com.daniyalmlik.deployguard.users.dto.RegisterRequest;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.count() > 0) {
            throw new UserAlreadyExistsException("An admin account already exists");
        }
        User user = new User();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        userRepository.save(user);
        return AuthResponse.of(
                jwtService.generateAccessToken(user.getEmail()),
                jwtService.generateRefreshToken(user.getEmail()));
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        return AuthResponse.of(
                jwtService.generateAccessToken(req.email()),
                jwtService.generateRefreshToken(req.email()));
    }

    public AuthResponse refresh(RefreshRequest req) {
        String email;
        try {
            email = jwtService.extractEmail(req.refreshToken());
        } catch (JwtException ex) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid refresh token");
        }
        userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid refresh token"));
        return AuthResponse.of(
                jwtService.generateAccessToken(email),
                jwtService.generateRefreshToken(email));
    }
}
