package com.connectedgames.tournament.domain.tournament;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import com.connectedgames.tournament.domain.OrganizationId;
import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.PlayerId;
import com.connectedgames.tournament.domain.RegistrationId;
import com.connectedgames.tournament.domain.TournamentId;
import com.connectedgames.tournament.domain.TournamentMatchId;
import com.connectedgames.tournament.domain.bracket.Bracket;
import com.connectedgames.tournament.domain.match.Entrant;
import com.connectedgames.tournament.domain.match.Outcome;
import com.connectedgames.tournament.domain.match.TournamentMatch;
import com.connectedgames.tournament.domain.match.TournamentMatchStatus;
import com.connectedgames.tournament.domain.registration.Member;
import com.connectedgames.tournament.domain.registration.Registration;
import com.connectedgames.tournament.domain.standing.EliminationStandings;
import com.connectedgames.tournament.domain.standing.Standing;

public record Tournament(TournamentId id, OrganizationId organizationId, TournamentDetails details,
                         TournamentSetup setup, TournamentStatus status, List<Registration> registrations,
                         Bracket bracket, Instant createdAt, Instant startedAt, Instant completedAt) {

    public Tournament {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(organizationId, "organizationId");
        Objects.requireNonNull(details, "details");
        Objects.requireNonNull(setup, "setup");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(registrations, "registrations");
        Objects.requireNonNull(createdAt, "createdAt");
        registrations = List.copyOf(registrations);
        if (registrations.size() > details.capacity()) {
            throw new IllegalArgumentException("There are more registrations than the maximum");
        }
        if (status == TournamentStatus.DRAFT && !registrations.isEmpty()) {
            throw new IllegalArgumentException("A draft has no registrations");
        }
        checkRegistrations(registrations, setup);
        checkProgress(status, bracket, startedAt, completedAt);
    }

    public static Tournament draft(TournamentId id, OrganizationId organizationId, TournamentDetails details,
                                   TournamentSetup setup, Instant now) {
        return new Tournament(id, organizationId, details, setup, TournamentStatus.DRAFT, List.of(), null, now, null,
                null);
    }

    public int registeredCount() {
        return registrations.size();
    }

    public Optional<Registration> registration(RegistrationId registrationId) {
        return registrations.stream().filter(registration -> registration.id().equals(registrationId)).findFirst();
    }

    public Tournament edit(TournamentDetails newDetails) {
        Objects.requireNonNull(newDetails, "newDetails");
        requireNotStarted();
        if (registrations.size() > newDetails.capacity()) {
            throw new TournamentConflictException("There are already more registrations than the maximum");
        }
        return new Tournament(id, organizationId, newDetails, setup, status, registrations, bracket, createdAt,
                startedAt, completedAt);
    }

    public Tournament reconfigure(TournamentSetup newSetup) {
        Objects.requireNonNull(newSetup, "newSetup");
        if (status != TournamentStatus.DRAFT) {
            throw new TournamentConflictException("The game, the format and the scoring change only in a draft");
        }
        return new Tournament(id, organizationId, details, newSetup, status, registrations, bracket, createdAt,
                startedAt, completedAt);
    }

    public void ensureDeletable() {
        if (status != TournamentStatus.DRAFT) {
            throw new TournamentConflictException("Only a draft can be deleted, a published tournament is cancelled");
        }
    }

    public Tournament publish() {
        if (status == TournamentStatus.REGISTRATION_OPEN) {
            return this;
        }
        if (status != TournamentStatus.DRAFT) {
            throw new TournamentConflictException("Only a draft can be published");
        }
        return new Tournament(id, organizationId, details, setup, TournamentStatus.REGISTRATION_OPEN, registrations,
                bracket, createdAt, startedAt, completedAt);
    }

    public Tournament register(Registration registration, Instant now) {
        Objects.requireNonNull(registration, "registration");
        Objects.requireNonNull(now, "now");
        if (status != TournamentStatus.REGISTRATION_OPEN) {
            throw new TournamentConflictException("The registrations are not open");
        }
        Instant closesAt = details.registrationClosesAt();
        if (closesAt != null && now.isAfter(closesAt)) {
            throw new TournamentConflictException("The registrations are closed");
        }
        if (registrations.size() >= details.capacity()) {
            throw new TournamentConflictException("The tournament is full");
        }
        if (registrations.stream().anyMatch(each -> each.participantId().equals(registration.participantId()))) {
            throw new TournamentConflictException("The participant is already registered");
        }
        for (Member member : registration.members()) {
            if (registrations.stream().anyMatch(each -> each.hasPlayer(member.playerId()))) {
                throw new TournamentConflictException("A player is already registered");
            }
        }
        if (registration.seed() != null) {
            throw new IllegalArgumentException("A registration starts without a seed");
        }
        List<Registration> updated = new ArrayList<>(registrations);
        updated.add(registration);
        return new Tournament(id, organizationId, details, setup, status, updated, bracket, createdAt, startedAt,
                completedAt);
    }

    public Tournament withdraw(RegistrationId registrationId) {
        if (status != TournamentStatus.REGISTRATION_OPEN) {
            throw new TournamentConflictException("A participant can leave only while the registrations are open");
        }
        if (registration(registrationId).isEmpty()) {
            throw new UnknownRegistrationException(registrationId);
        }
        List<Registration> updated = new ArrayList<>(registrations);
        updated.removeIf(each -> each.id().equals(registrationId));
        return new Tournament(id, organizationId, details, setup, status, updated, bracket, createdAt, startedAt,
                completedAt);
    }

    public Tournament seed(RegistrationId registrationId, Integer seed) {
        requireNotStarted();
        if (registration(registrationId).isEmpty()) {
            throw new UnknownRegistrationException(registrationId);
        }
        if (seed != null && registrations.stream()
                .anyMatch(each -> !each.id().equals(registrationId) && seed.equals(each.seed()))) {
            throw new TournamentConflictException("The seed is already taken");
        }
        List<Registration> updated = new ArrayList<>();
        for (Registration each : registrations) {
            updated.add(each.id().equals(registrationId) ? each.withSeed(seed) : each);
        }
        return new Tournament(id, organizationId, details, setup, status, updated, bracket, createdAt, startedAt,
                completedAt);
    }

    public Tournament start(Instant now, Supplier<TournamentMatchId> ids, RandomGenerator random) {
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(ids, "ids");
        Objects.requireNonNull(random, "random");
        if (status == TournamentStatus.IN_PROGRESS || status == TournamentStatus.COMPLETED) {
            return this;
        }
        if (status != TournamentStatus.REGISTRATION_OPEN) {
            throw new TournamentConflictException("Only a published tournament can start");
        }
        if (registrations.size() < details.minParticipants()) {
            throw new TournamentConflictException("There are fewer registrations than the minimum");
        }
        List<Entrant> entrants = setup.format().seeding().order(registrations, random).stream()
                .map(registration -> new Entrant(registration.participantId(), registration.displayName()))
                .toList();
        Bracket started = new Bracket(setup.format().format().createMatches(entrants, now, ids));
        return new Tournament(id, organizationId, details, setup, TournamentStatus.IN_PROGRESS, registrations,
                started, createdAt, now, null);
    }

    public Tournament recordResult(TournamentMatchId matchId, Outcome outcome, Instant now) {
        Objects.requireNonNull(now, "now");
        if (status != TournamentStatus.IN_PROGRESS && status != TournamentStatus.COMPLETED) {
            throw new TournamentConflictException("The tournament is not in progress");
        }
        Bracket updated = bracket.recordResult(matchId, outcome);
        if (updated == bracket) {
            return this;
        }
        boolean completes = status == TournamentStatus.IN_PROGRESS && updated.isComplete();
        return new Tournament(id, organizationId, details, setup,
                completes ? TournamentStatus.COMPLETED : status, registrations, updated, createdAt, startedAt,
                completes ? now : completedAt);
    }

    public Tournament cancel() {
        if (status == TournamentStatus.CANCELLED) {
            return this;
        }
        if (status == TournamentStatus.COMPLETED) {
            throw new TournamentConflictException("A completed tournament cannot be cancelled");
        }
        return new Tournament(id, organizationId, details, setup, TournamentStatus.CANCELLED, registrations, bracket,
                createdAt, startedAt, completedAt);
    }

    public List<Standing> standings() {
        if (bracket == null) {
            return List.of();
        }
        return switch (setup.format()) {
            case SingleEliminationSpec _ -> EliminationStandings.of(bracket, setup.scoring());
        };
    }

    public OptionalInt currentRound() {
        if (status != TournamentStatus.IN_PROGRESS) {
            return OptionalInt.empty();
        }
        return bracket.matches().stream()
                .filter(match -> match.status() != TournamentMatchStatus.COMPLETED)
                .mapToInt(TournamentMatch::round)
                .min();
    }

    private void requireNotStarted() {
        if (status != TournamentStatus.DRAFT && status != TournamentStatus.REGISTRATION_OPEN) {
            throw new TournamentConflictException("A tournament that has started cannot be changed");
        }
    }

    private static void checkRegistrations(List<Registration> registrations, TournamentSetup setup) {
        Set<RegistrationId> ids = new HashSet<>();
        Set<ParticipantId> participants = new HashSet<>();
        Set<PlayerId> players = new HashSet<>();
        Set<Integer> seeds = new HashSet<>();
        for (Registration registration : registrations) {
            if (!ids.add(registration.id())) {
                throw new IllegalArgumentException("A registration is twice in the tournament");
            }
            if (!participants.add(registration.participantId())) {
                throw new IllegalArgumentException("A participant is registered twice");
            }
            if (registration.members().size() != setup.squadSize()) {
                throw new IllegalArgumentException("A registration has " + setup.squadSize() + " players");
            }
            if (setup.participantKind() == ParticipantKind.PLAYER && !registration.participantId().value()
                    .equals(registration.members().get(0).playerId().value().toString())) {
                throw new IllegalArgumentException("The participant of a player is the player");
            }
            for (Member member : registration.members()) {
                if (!players.add(member.playerId())) {
                    throw new IllegalArgumentException("A player is registered twice");
                }
            }
            if (registration.seed() != null && !seeds.add(registration.seed())) {
                throw new IllegalArgumentException("Two registrations have the same seed");
            }
        }
    }

    private static void checkProgress(TournamentStatus status, Bracket bracket, Instant startedAt,
                                      Instant completedAt) {
        switch (status) {
            case DRAFT, REGISTRATION_OPEN -> {
                if (bracket != null || startedAt != null || completedAt != null) {
                    throw new IllegalArgumentException("A tournament that has not started has no bracket");
                }
            }
            case IN_PROGRESS -> {
                if (bracket == null || startedAt == null || completedAt != null || bracket.isComplete()) {
                    throw new IllegalArgumentException("A tournament in progress has an unfinished bracket");
                }
            }
            case COMPLETED -> {
                if (bracket == null || startedAt == null || completedAt == null || !bracket.isComplete()) {
                    throw new IllegalArgumentException("A completed tournament has a finished bracket");
                }
            }
            case CANCELLED -> {
                if (completedAt != null || (bracket == null) != (startedAt == null)) {
                    throw new IllegalArgumentException("A cancelled tournament keeps what it had when it stopped");
                }
            }
        }
    }
}
