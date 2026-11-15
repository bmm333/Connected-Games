package com.connectedgames.identity.adapters.in.web;

import com.connectedgames.identity.application.port.in.ActiveSession;
import com.connectedgames.identity.application.port.in.GuestSession;
import com.connectedgames.identity.application.port.in.IssuedSession;
import com.connectedgames.identity.application.port.in.UserSession;
import com.connectedgames.identity.domain.guest.Guest;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.domain.user.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** JSON bodies of the identity API, shaped as in contracts/openapi/identity.yaml */
final class WebResponses {

    private WebResponses() {
        // response container only
    }

    /** One of GuestSession or UserSession of the contract: only the profile of its kind is present */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record SessionResponse(UUID id, SessionKind kind, String accessToken, Instant expiresAt,
                           String refreshToken, Instant refreshExpiresAt,
                           GuestProfileResponse guest, UserResponse user) {

        static SessionResponse from(ActiveSession active) {
            // sealed: the compiler checks that every kind of session is handled
            return switch (active) {
                case GuestSession g -> of(g.session(), GuestProfileResponse.from(g.guest()), null);
                case UserSession u -> of(u.session(), null, UserResponse.from(u.user()));
            };
        }

        private static SessionResponse of(IssuedSession s, GuestProfileResponse guest, UserResponse user) {
            return new SessionResponse(s.id().value(), s.kind(), s.accessToken(), s.accessTokenExpiresAt(),
                    s.refreshToken(), s.refreshExpiresAt(), guest, user);
        }
    }

    record GuestProfileResponse(UUID playerId, String nickname) {
        static GuestProfileResponse from(Guest guest) {
            return new GuestProfileResponse(guest.playerId().value(), guest.nickname().value());
        }
    }

    record UserResponse(UUID id, String email, String displayName, UUID playerId, List<RoleAssignmentResponse> roles) {
        static UserResponse from(User user) {
            return new UserResponse(user.id().value(), user.email().value(), user.displayName(),
                    user.playerId().value(), user.roles().stream().map(RoleAssignmentResponse::from).toList());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record RoleAssignmentResponse(Role role, UUID venueId, UUID organizationId) {
        static RoleAssignmentResponse from(RoleAssignment assignment) {
            return new RoleAssignmentResponse(assignment.role(), assignment.venueId(), assignment.organizationId());
        }
    }
}
