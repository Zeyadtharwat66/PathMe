package com.PathMe.course_reservation.service.imp;

import com.PathMe.course_reservation.service.JWTTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JWTTokenServiceImp implements JWTTokenService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final Duration expiry;
    private final String issuer;
    public JWTTokenServiceImp(JwtEncoder encoder,
                              JwtDecoder decoder,
                              @Value("${app.jwt.exp-minutes}") long expMinutes,
                              @Value("${app.jwt.issuer}") String issuer) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.expiry = Duration.ofMinutes(expMinutes);
        this.issuer = issuer;
    }
    @Override
    public String generateJWTToken(Authentication auth) {
        Instant now= Instant.now();
        Instant expires = now.plus(expiry);
        String scope=auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet cliams=JwtClaimsSet.builder()
                .claim("scope" , scope)
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expires)
                .subject(auth.getName())
                .build();
        JwsHeader header= JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header,cliams)).getTokenValue();
    }
    @Override
    public Authentication parseJWT(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            String username = jwt.getSubject();
            String scope = (String) jwt.getClaim("scope");
            List<GrantedAuthority> authorities = Arrays.stream(scope.split(" "))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(username, null, authorities);

        } catch (JwtException e) {
            return null;
        }
    }
}
