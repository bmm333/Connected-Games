package com.connectedgames.stats.adapters.in.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
class SecurityConfiguration{

    private static final BearerTokenAuthenticationEntryPoint BEARER = new BearerTokenAuthenticationEntryPoint();
    private static final String UNAUTHORIZED_BODY = """ 
        {"type":"about:blank","title":"Unauthorized","status":401,"detail":"Token missing or invalid"}
    """;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                        .requestMatchers("/actuator/health/**","/error").permitAll()
                                            .anyRequest().authenticated())
                            .oauth2ResourceServer(oauth -> oauth
                                                 .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(SecurityConfiguration::unauthorized))
                .build();
    }
    private static void unauthorized(HttpServletRequest request,HttpServletResponse response
                                    ,AuthenticationException e) throws IOException{
        BEARER.commence(request,response,e);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getOutputStream().write(UNAUTHORIZED_BODY.getBytes(StandardCharsets.UTF_8));
    }
}

















