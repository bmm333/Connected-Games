package com.connectedgames.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectedgames.identity.application.port.in.RegisterUseCase;
import com.connectedgames.identity.application.port.in.UserSession;
import com.connectedgames.identity.application.port.out.PasswordHasher;
import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.application.port.out.UserRepository;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;

public final class RegisterService implements RegisterUseCase
{
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final SessionRepository sessions;
    private final SessionTokens tokens;
    private final Clock clock;

    public RegisterService(UserRepository users,PasswordHasher passwordHasher,SessionRepository sessions,
            SessionTokens tokens,Clock clock)
    {
        this.users=users;
        this.passwordHasher=passwordHasher;
        this.sessions=sessions;
        this.tokens=tokens;
        this.clock=clock;
    }

    @Override
    public UserSession register(Email email,String password,String displayName,Optional<SessionId> guestSession)
    {
        if(users.findByEmail(email).isPresent())
        {
            throw new EmailAlreadyRegisteredException();
        }

        Instant now=clock.instant();

        PlayerId playerId=guestSession.map(id -> takeOverGuest(id,now))
                .orElseGet(() -> new PlayerId(UUID.randomUUID()));
        User user=new User(new UserId(UUID.randomUUID()),email,displayName,passwordHasher.hash(password),
                playerId,List.of(RoleAssignment.of(Role.PLAYER)));
        users.save(user);

        Session session=Session.start(new SessionId(UUID.randomUUID()),SessionKind.USER,user.id().value(),now);
        sessions.save(session);

        return new UserSession(user,tokens.issue(session,user.roles(),now));
    }

    // the guest session ends here: its refresh token must not keep a second way into the same player
    private PlayerId takeOverGuest(SessionId id,Instant now)
    {
        Session session=sessions.findById(id)
                .orElseThrow(() -> new IllegalStateException("Session " + id.value() + " does not exist"));
        if(session.kind()!=SessionKind.GUEST || !session.isActive(now))
        {
            throw new IllegalArgumentException("Only an active guest session can become a user");
        }
        sessions.save(session.revoked(now));
        return new PlayerId(session.subjectId());
    }
}
