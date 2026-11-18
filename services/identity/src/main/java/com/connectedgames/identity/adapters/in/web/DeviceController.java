package com.connectedgames.identity.adapters.in.web;

import com.connectedgames.identity.adapters.in.web.WebRequests.PairDeviceRequest;
import com.connectedgames.identity.adapters.in.web.WebRequests.PairingCodeRequest;
import com.connectedgames.identity.adapters.in.web.WebResponses.PairingCodeResponse;
import com.connectedgames.identity.adapters.in.web.WebResponses.SessionResponse;
import com.connectedgames.identity.application.port.in.CreatePairingCodeUseCase;
import com.connectedgames.identity.application.port.in.PairDeviceUseCase;
import com.connectedgames.identity.domain.device.PairingCode;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Devices endpoints of contracts/openapi/identity.yaml: pairing codes and pairing */
@RestController
class DeviceController {

    private final CreatePairingCodeUseCase createPairingCode;
    private final PairDeviceUseCase pairDevice;

    DeviceController(CreatePairingCodeUseCase createPairingCode, PairDeviceUseCase pairDevice) {
        this.createPairingCode = createPairingCode;
        this.pairDevice = pairDevice;
    }

    @PostMapping("/devices/pairing-codes")
    ResponseEntity<PairingCodeResponse> createPairingCode(@Valid @RequestBody PairingCodeRequest request,
                                                          @AuthenticationPrincipal Jwt token) {
        // for a guest or a device the sub is not an account
        if (AccessTokens.kind(token) != SessionKind.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a venue manager can create pairing codes");
        }
        PairingCode code = createPairingCode.createPairingCode(new UserId(AccessTokens.subject(token)),
                request.venueId(), request.tableId(), request.surface());
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
                .body(PairingCodeResponse.from(code));
    }

    @PostMapping("/devices/pair")
    ResponseEntity<SessionResponse> pairDevice(@Valid @RequestBody PairDeviceRequest request) {
        return SessionController.withTokens(pairDevice.pairDevice(request.pairingCode(), request.surface()));
    }
}
