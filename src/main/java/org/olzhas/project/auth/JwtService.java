package org.olzhas.project.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.olzhas.project.dto.JwtAuthenticationDto;
import org.olzhas.project.model.Role;
import org.olzhas.project.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthenticationDto generateToken(User user) {
        log.info("Generating tokens for email {}", user.getEmail());
        JwtAuthenticationDto jwtDto = new JwtAuthenticationDto();
        jwtDto.setToken(generateAccessToken(user));
        jwtDto.setRefreshToken(generateRefreshToken(user));
        return jwtDto;
    }

    private String generateAccessToken(User user) {
        log.debug("Generating access token for {}", user.getEmail());
        Date date = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .claim("id", user.getId())
                .expiration(date)
                .signWith(getSignKey())
                .compact();
    }

    private String generateRefreshToken(User user) {
        log.debug("Generating refresh token for {}", user.getEmail());
        Date date = Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .claim("id", user.getId())
                .expiration(date)
                .signWith(getSignKey())
                .compact();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e){
            log.error("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            return false;
        }catch (SecurityException e){
            log.error("JWT token signature is invalid: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        var claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public JwtAuthenticationDto refreshToken(User user, String refreshToken) {
        JwtAuthenticationDto jwtDto = new JwtAuthenticationDto();
        jwtDto.setToken(generateAccessToken(user));
        jwtDto.setRefreshToken(refreshToken);
        return jwtDto;
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
