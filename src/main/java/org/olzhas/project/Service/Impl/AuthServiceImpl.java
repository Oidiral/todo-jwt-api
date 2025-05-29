package org.olzhas.project.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olzhas.project.ExceptionHandler.AlreadyExistException;
import org.olzhas.project.ExceptionHandler.AuthException;
import org.olzhas.project.ExceptionHandler.NotFoundException;
import org.olzhas.project.Service.AuthService;
import org.olzhas.project.auth.CustomUserDetails;
import org.olzhas.project.auth.JwtService;
import org.olzhas.project.dto.JwtAuthenticationDto;
import org.olzhas.project.dto.SignInRequest;
import org.olzhas.project.dto.SignUpRequest;
import org.olzhas.project.model.Role;
import org.olzhas.project.model.User;
import org.olzhas.project.repository.RoleRepository;
import org.olzhas.project.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager manager;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public JwtAuthenticationDto register(SignUpRequest dto) {
        log.info("Registration attempt for email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registration failed - email already exists: {}", dto.getEmail());
            throw new AlreadyExistException("User with this email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> {
                    log.error("Role 'ROLE_USER' not found in database");
                    return new NotFoundException("Role not found");
                });

        user.setRoles(Set.of(role));
        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", dto.getEmail());
        return jwtService.generateToken(savedUser);
    }

    @Override
    public JwtAuthenticationDto login(SignInRequest dto) {
        log.info("Login attempt for email: {}", dto.getEmail());
        Authentication auth = manager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.user();
        log.info("User authenticated successfully: {}", dto.getEmail());
        return jwtService.generateToken(user);

    }

    @Override
    public JwtAuthenticationDto refresh(String refreshToken) {
        log.info("Token refresh attempt");

        if (!jwtService.validateJwtToken(refreshToken)) {
            log.warn("Token refresh failed - invalid token");
            throw new AuthException("Invalid refresh token");
        }

        String email = jwtService.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed - user not found: {}", email);
                    return new AuthException("User not found");
                });

        log.info("Token refreshed successfully for user: {}", email);
        return jwtService.refreshToken(user, refreshToken);
    }
}
