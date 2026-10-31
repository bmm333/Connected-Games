package  com.connectedgames.identity.domain.guest;

import java.util.Objects;

import com.connectedgames.identity.domain.PlayerId;

public record Guest(PlayerId playerId,Nickname nickname) {
    public Guest{
        Objects.requireNonNull(playerId,"playerId");
        Objects.requireNonNull(nickname,"nickname");
    }
}
