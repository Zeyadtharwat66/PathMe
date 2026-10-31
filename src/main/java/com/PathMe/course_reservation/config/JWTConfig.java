package com.PathMe.course_reservation.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JWTConfig {
    @Value("${app.jwt.secret}")
    private String JWTsecret;
    @Bean
    public JwtEncoder jwtEncoder(){
        var secret=JWTsecret.getBytes(StandardCharsets.UTF_8);
        var jwk=new OctetSequenceKey.Builder(secret).algorithm(JWSAlgorithm.HS256).build();
        var jwkset=new JWKSet(jwk);
        var immutable=new ImmutableJWKSet<>(jwkset);
        return new NimbusJwtEncoder(immutable);
    }
    @Bean
    public JwtDecoder jwtDecoder(){
        SecretKey key=new SecretKeySpec(JWTsecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
