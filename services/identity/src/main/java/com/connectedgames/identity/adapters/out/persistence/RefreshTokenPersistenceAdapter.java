package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.application.InvalidRefreshTokenException;
import com.connectedgames.identity.application.port.out.RefreshTokenRepository;
import com.connectedgames.identity.domain.session.RefreshToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Implements the RefreshTokenRepository port with JPA. */
@Component
class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpa;

    RefreshTokenPersistenceAdapter(RefreshTokenJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(RefreshToken token) {
        if (!token.isUsed()) {
            jpa.save(RefreshTokenEntity.from(token));
            return;
        }
        // Two refreshes with the same token at the same moment both read it as unused:
        // the conditional update lets only the first one through.
        if (jpa.markUsed(token.tokenHash(), token.usedAt()) == 0) {
            throw new InvalidRefreshTokenException();
        }
    }

    @Override
    public Optional<RefreshToken> findByHash(String tokenHash) {
        return jpa.findById(tokenHash).map(RefreshTokenEntity::toDomain);
    }
}
