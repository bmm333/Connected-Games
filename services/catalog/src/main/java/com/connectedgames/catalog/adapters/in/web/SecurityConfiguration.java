package com.connectedgames.catalog.adapters.in.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Who can call which endpoint, as in contracts/openapi/catalog.yaml: the QR lookup is public,
 * rest signed token .
 */
@Configuration
class SecurityConfiguration {

    private static final BearerTokenAuthenticationEntryPoint BEARER = new BearerTokenAuthenticationEntryPoint();
    private static final String UNAUTHORIZED_BODY = """
            {"type":"about:blank","title":"Unauthorized","status":401,"detail":"Token missing or invalid"}""";

    @Bean
    @Profile("!local")
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //Statelss API with bearer tokens
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/tables/by-code/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(SecurityConfiguration::unauthorized))
                .build();
    }

    /**locally is open TODO remove once identity is up. */
    @Bean
    @Profile("local")
    SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    /** 401 as application/problem+json like every other error, keeping the WWW-Authenticate header. */
    private static void unauthorized(HttpServletRequest request, HttpServletResponse response,
                                     AuthenticationException e) throws IOException {
        BEARER.commence(request, response, e);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        // Bytes, not getWriter.
        response.getOutputStream().write(UNAUTHORIZED_BODY.getBytes(StandardCharsets.UTF_8));
    }
}
