package org.olzhas.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.olzhas.project.ExceptionHandler.AlreadyExistException;
import org.olzhas.project.ExceptionHandler.AuthException;
import org.olzhas.project.ExceptionHandler.NotFoundException;
import org.olzhas.project.service.Impl.AuthServiceImpl;
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

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private SignUpRequest signUpRq;
    private SignInRequest signInRq;
    private Role roleUser;
    private User savedUser;

    @BeforeEach
    void init() {
        signUpRq = new SignUpRequest("Olzhas","olzhas@mail.com","Password123");
        signInRq = new SignInRequest("olzhas@mail.com","Password123");


        roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName("USER");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("Olzhas");
        savedUser.setEmail("olzhas@mail.com");
        savedUser.setPassword("encoded-pwd");
        savedUser.setRoles(Set.of(roleUser));
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail(signUpRq.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signUpRq.getPassword())).thenReturn("encoded-pwd");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(roleUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser))
                .thenReturn(new JwtAuthenticationDto("access", "refresh"));

        JwtAuthenticationDto dto = authService.register(signUpRq);

        assertEquals("access", dto.getToken());
        assertEquals("refresh", dto.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailExists_throws() {
        when(userRepository.existsByEmail(signUpRq.getEmail())).thenReturn(true);
        assertThrows(AlreadyExistException.class,
                () -> authService.register(signUpRq));
    }

    @Test
    void register_roleMissing_throws() {
        when(userRepository.existsByEmail(signUpRq.getEmail())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("x");
        assertThrows(NotFoundException.class,
                () -> authService.register(signUpRq));
    }

    @Test
    void login_success() {
        CustomUserDetails cud = new CustomUserDetails(savedUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(cud, null, cud.getAuthorities());

        when(authManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(savedUser))
                .thenReturn(new JwtAuthenticationDto("access", "refresh"));

        JwtAuthenticationDto dto = authService.login(signInRq);

        assertEquals("access", dto.getToken());
        verify(authManager).authenticate(any());
    }

    @Test
    void refresh_success() {
        String refresh = "validRefresh";
        when(jwtService.validateJwtToken(refresh)).thenReturn(true);
        when(jwtService.getEmailFromToken(refresh)).thenReturn(savedUser.getEmail());
        when(userRepository.findByEmail(savedUser.getEmail()))
                .thenReturn(Optional.of(savedUser));
        when(jwtService.refreshToken(savedUser, refresh))
                .thenReturn(new JwtAuthenticationDto("newAccess", refresh));

        JwtAuthenticationDto dto = authService.refresh(refresh);

        assertEquals("newAccess", dto.getToken());
        assertEquals(refresh, dto.getRefreshToken());
    }

    @Test
    void refresh_invalidToken_throws() {
        when(jwtService.validateJwtToken("bad")).thenReturn(false);
        assertThrows(AuthException.class,
                () -> authService.refresh("bad"));
    }

    @Test
    void refresh_userNotFound_throws() {
        when(jwtService.validateJwtToken("ref")).thenReturn(true);
        when(jwtService.getEmailFromToken("ref")).thenReturn("ghost@mail.com");
        when(userRepository.findByEmail("ghost@mail.com")).thenReturn(Optional.empty());
        assertThrows(AuthException.class,
                () -> authService.refresh("ref"));
    }
}
