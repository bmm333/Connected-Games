package com.connectedgames.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.connectedgames.identity.application.port.in.CreatePairingCodeUseCase;
import com.connectedgames.identity.application.port.out.PairingCodeGenerator;
import com.connectedgames.identity.application.port.out.PairingCodeRepository;
import com.connectedgames.identity.application.port.out.UserRepository;
import com.connectedgames.identity.domain.device.PairingCode;
import com.connectedgames.identity.domain.device.Surface;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;

public final class CreatePairingCodeService implements CreatePairingCodeUseCase{

    private final UserRepository users;
    private final PairingCodeRepository codes;
    private final PairingCodeGenerator generator;
    private final Clock clock;

    public CreatePairingCodeService(UserRepository users, PairingCodeRepository codes,PairingCodeGenerator generator, Clock clock){
        this.users=users;
        this.codes=codes;
        this.generator=generator;
        this.clock=clock;
    }

    @Override
    public PairingCode createPairingCode(UserId requester, UUID venueId, UUID tableId,Surface surface){
        User user = users.findById(requester).orElseThrow(() -> new IllegalStateException("User " +
                requester.value() + " does not exist"));
        if(!user.managesVenue(venueId)){
            throw new NotVenueManagerException();
        }
        Instant now = clock.instant();
        PairingCode code;
        do{
            code = PairingCode.issue(generator.generate(),venueId,tableId,surface,now);
        }while(isInUse(code.value(),now));
        codes.save(code);
        return code;
    }
    /**An old code of the same 6char expired/used can be replaced; a still valid one cannot*/
    private boolean isInUse(String value,Instant now){
        return codes.findByValueForUpdate(value).map(existing ->
        existing.isUsable(now)).orElse(false);
    }
}
