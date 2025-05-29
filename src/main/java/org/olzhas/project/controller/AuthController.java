package org.olzhas.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.olzhas.project.Service.Impl.AuthServiceImpl;
import org.olzhas.project.dto.JwtAuthenticationDto;
import org.olzhas.project.dto.SignInRequest;
import org.olzhas.project.dto.SignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationDto> signIn(
            @Valid @RequestBody SignInRequest signInDto) {
        return ResponseEntity.ok(authServiceImpl.login(signInDto));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<JwtAuthenticationDto> signUp(
            @Valid @RequestBody SignUpRequest signUpDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authServiceImpl.register(signUpDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationDto> refresh(
            @RequestBody String refreshToken) {
        return ResponseEntity.ok(authServiceImpl.refresh(refreshToken));
    }
}
