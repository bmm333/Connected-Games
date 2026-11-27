package com.connectedgames.stats.adapters.in.web;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;

import com.connectedgames.stats.application.NotAllowedException;
import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.domain.TenantId;

final class ViewerResolver {

    private ViewerResolver() {
    }

    static Viewer of(Jwt token) {
        String kind = token.getClaimAsString("kind");
        if (kind == null) {
            throw new NotAllowedException();
        }
        return switch (kind) {
            case "DEVICE" -> tenants(Set.of(requireText(token.getClaimAsString("organizationId"))));
            case "GUEST" -> player(firstText(token.getClaimAsString("playerId"), token.getSubject()));
            case "USER" -> user(token);
            default -> throw new NotAllowedException();
        };
    }

    private static Viewer user(Jwt token) {
        Set<String> organizations = new HashSet<>();
        for (Map<?, ?> role : roles(token)) {
            if ("VENUE_MANAGER".equals(role.get("role")) && role.get("organizationId") instanceof String id
                    && !id.isBlank()) {
                organizations.add(id);
            }
        }
        if (!organizations.isEmpty()) {
            return tenants(organizations);
        }
        boolean isPlayer = roles(token).stream().anyMatch(role -> "PLAYER".equals(role.get("role")));
        if (!isPlayer) {
            throw new NotAllowedException();
        }
        return player(requireText(token.getClaimAsString("playerId")));
    }

    private static List<Map<?, ?>> roles(Jwt token) {
        Object claim = token.getClaim("roles");
        if (!(claim instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(Map.class::isInstance).<Map<?, ?>>map(Map.class::cast).toList();
    }

    private static Viewer tenants(Set<String> organizations) {
        return new Viewer.OfTenants(organizations.stream().map(TenantId::new).collect(Collectors.toSet()));
    }

    private static Viewer player(String playerId) {
        return new Viewer.OfPlayer(playerId);
    }

    private static String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : requireText(second);
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new NotAllowedException();
        }
        return value;
    }
}
