package com.PathMe.course_reservation.service.imp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

class JWTTokenServiceImpTest {

    @Test
    void generateAndParseTokenReturnsUserAndAuthorities() {
        JwtEncoder encoder = mock(JwtEncoder.class);
        JwtDecoder decoder = mock(JwtDecoder.class);
        JWTTokenServiceImp service = new JWTTokenServiceImp(encoder, decoder, 60, "course-reservation");

        String jwtToken = "mocked.jwt.token";
        when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(Jwt.withTokenValue(jwtToken)
                .header("alg", "HS256")
                .claims(c -> {
                    c.put("scope", "ADMIN");
                    c.put("sub", "student");
                })
                .build());
        when(decoder.decode(jwtToken)).thenReturn(Jwt.withTokenValue(jwtToken)
                .header("alg", "HS256")
                .claims(c -> {
                    c.put("scope", "ADMIN");
                    c.put("sub", "student");
                })
                .build());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "student",
                null,
                List.of(new SimpleGrantedAuthority("ADMIN")));

        String generated = service.generateJWTToken(auth);
        Authentication parsed = service.parseJWT(generated);

        assertNotNull(generated);
        assertEquals("student", parsed.getName());
        assertTrue(parsed.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }
}