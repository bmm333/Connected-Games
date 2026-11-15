package com.connectedgames.identity.application.port.in;

import com.connectedgames.identity.domain.user.User;

public record UserSession(User user,IssuedSession session) implements ActiveSession {}
