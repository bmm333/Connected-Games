package com.connectedgames.match.adapters.out.persistence;

import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.GameTypeId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.TenantId;
import com.connectedgames.match.domain.TournamentMatchId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.match.Match;
import com.connectedgames.match.domain.match.MatchStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "match")
class MatchEntity {

    @Id
    private UUID id;
    private UUID venueId;
    private UUID tableId;
    private String tenantId;
    private UUID gameTypeId;
    private UUID tournamentMatchId;
    @Enumerated(EnumType.STRING)
    private MatchStatus status;
    @Enumerated(EnumType.STRING)
    private GameMode mode;
    private int pointsToWin;
    @Enumerated(EnumType.STRING)
    private Side winnerSide;
    private Instant startedAt;
    private Instant endedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "match_side", joinColumns = @JoinColumn(name = "match_id"))
    @OrderColumn(name = "position")
    private List<MatchSideEmbeddable> sides = new ArrayList<>();

    protected MatchEntity() {
        // required by JPA
    }

    static MatchEntity from(Match match) {
        MatchEntity row = new MatchEntity();
        row.id = match.id().value();
        row.venueId = match.venueId().value();
        row.tableId = match.tableId().value();
        row.tenantId = match.tenantId().value();
        row.gameTypeId = match.gameTypeId().value();
        row.tournamentMatchId = match.tournamentMatchId() == null ? null : match.tournamentMatchId().value();
        row.status = match.status();
        row.mode = match.mode();
        row.pointsToWin = match.pointsToWin();
        row.winnerSide = match.winnerSide();
        row.startedAt = match.startedAt();
        row.endedAt = match.endedAt();
        row.sides = new ArrayList<>(match.sides().stream().map(MatchSideEmbeddable::from).toList());
        return row;
    }

    Match toDomain() {
        return new Match(new MatchId(id), new VenueId(venueId), new TableId(tableId), new TenantId(tenantId),
                new GameTypeId(gameTypeId), tournamentMatchId == null ? null : new TournamentMatchId(tournamentMatchId),
                status, mode, pointsToWin,
                sides.stream().map(MatchSideEmbeddable::toDomain).toList(), winnerSide, startedAt, endedAt);
    }
}
