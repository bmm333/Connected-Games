package com.connectedgames.identity.adapters.in.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Who can call which endpoint, as in contracts/openapi/identity.yaml. identity checks its own
 * tokens with the JwtDecoder of the security adapter.
 */
@Configuration
class SecurityConfiguration {

    private static final BearerTokenAuthenticationEntryPoint BEARER = new BearerTokenAuthenticationEntryPoint();
    private static final String UNAUTHORIZED_BODY = """
            {"type":"about:blank","title":"Unauthorized","status":401,"detail":"Token missing or invalid"}""";

    /**
     * Endpoints that start a session or publish keys. A token sent to them is ignored: a client
     * refreshing because its access token expired must not be rejected for that very token.
     */
    private static final RequestMatcher TOKEN_NOT_USED = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/sessions"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/sessions/guest"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/sessions/refresh"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/.well-known/jwks.json"));

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // stateless API with bearer tokens
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(TOKEN_NOT_USED).permitAll()
                        // public, but a GUEST token turns the guest into the new user
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(bearerTokenResolver())
                        .jwt(jwt -> {})
                        .authenticationEntryPoint(SecurityConfiguration::unauthorized))
                .build();
    }

    private static BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver header = new DefaultBearerTokenResolver();
        return request -> TOKEN_NOT_USED.matches(request) ? null : header.resolve(request);
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
