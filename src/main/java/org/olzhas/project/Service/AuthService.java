package org.olzhas.project.Service;

import org.olzhas.project.dto.JwtAuthenticationDto;
import org.olzhas.project.dto.SignInRequest;
import org.olzhas.project.dto.SignUpRequest;


public interface AuthService {
    JwtAuthenticationDto register(SignUpRequest dto);

    JwtAuthenticationDto login(SignInRequest dto);

    JwtAuthenticationDto refresh(String refreshToken);
}
