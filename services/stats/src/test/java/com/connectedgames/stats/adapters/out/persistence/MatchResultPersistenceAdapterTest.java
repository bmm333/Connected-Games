package com.connectedgames.stats.adapters.out.persistence;

import com.connectedgames.stats.Fixtures;
import com.connectedgames.stats.MosquittoTestConfiguration;
import com.connectedgames.stats.TestcontainersConfiguration;
import com.connectedgames.stats.application.port.out.MatchResultRepository;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.result.MatchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import({TestcontainersConfiguration.class, MosquittoTestConfiguration.class})
class MatchResultPersistenceAdapterTest {

    @Autowired
    MatchResultRepository repository;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanTheTables() {
        // the sides and the points go with it: ON DELETE CASCADE
        jdbc.update("delete from match_result");
    }

    private int count(String table, MatchId id) {
        return jdbc.queryForObject("select count(*) from " + table + " where match_id = ?", Integer.class, id.value());
    }

    @Test
    void savesTheResultWithItsSidesAndPoints() {
        MatchResult result = Fixtures.result();

        assertThat(repository.saveIfAbsent(result)).isTrue();

        Map<String, Object> row = jdbc.queryForMap("select * from match_result where match_id = ?", result.id().value());
        assertThat(row.get("message_id")).isEqualTo(result.messageId().value());
        assertThat(row.get("tenant_id")).isEqualTo("acme");
        assertThat(row.get("venue_id")).isEqualTo(result.venueId().value());
        assertThat(row.get("table_id")).isEqualTo(result.tableId().value());
        assertThat(row.get("game_type_id")).isEqualTo(Fixtures.GAME_TYPE.value());
        assertThat(row.get("tournament_match_id")).isEqualTo(Fixtures.TOURNAMENT_MATCH.value());
        assertThat(row.get("mode")).isEqualTo("TEAM");
        assertThat(row.get("points_to_win")).isEqualTo(5);
        assertThat(row.get("reason")).isEqualTo("SCORE_REACHED");
        assertThat(row.get("winner_side")).isEqualTo("RED");
        List<Map<String, Object>> sides = jdbc.queryForList(
                "select * from match_side_result where match_id = ? order by side desc", result.id().value());
        assertThat(sides).hasSize(2);
        assertThat(sides.get(0).get("side")).isEqualTo("RED");
        assertThat(sides.get(0).get("participant_id")).isEqualTo("team-1");
        assertThat(sides.get(0).get("display_name")).isEqualTo("Lightning");
        assertThat(sides.get(0).get("score")).isEqualTo(5);
        assertThat(sides.get(1).get("participant_id")).isNull();
        assertThat(sides.get(1).get("score")).isEqualTo(3);
        List<Map<String, Object>> points = jdbc.queryForList(
                "select * from scored_point where match_id = ? order by seq", result.id().value());
        assertThat(points).hasSize(8);
        assertThat(points.get(0).get("seq")).isEqualTo(2);
        assertThat(points.get(0).get("side")).isEqualTo("RED");
        assertThat(points.get(2).get("side")).isEqualTo("BLUE");
    }

    @Test
    void theTimesComeBackAsTheSameInstants() {
        MatchResult result = Fixtures.result();
        repository.saveIfAbsent(result);

        OffsetDateTime started = jdbc.queryForObject("select started_at from match_result where match_id = ?",
                OffsetDateTime.class, result.id().value());
        OffsetDateTime firstPoint = jdbc.queryForObject(
                "select occurred_at from scored_point where match_id = ? and seq = 2", OffsetDateTime.class, result.id().value());

        assertThat(started.toInstant()).isEqualTo(Fixtures.START);
        assertThat(firstPoint.toInstant()).isEqualTo(result.points().get(0).occurredAt());
    }

    @Test
    void aFriendlyDrawIsSavedWithoutTheOptionalFields() {
        MatchResult draw = Fixtures.draw(new MatchId(UUID.randomUUID()));

        assertThat(repository.saveIfAbsent(draw)).isTrue();

        Map<String, Object> row = jdbc.queryForMap("select * from match_result where match_id = ?", draw.id().value());
        assertThat(row.get("tournament_match_id")).isNull();
        assertThat(row.get("winner_side")).isNull();
        assertThat(row.get("reason")).isEqualTo("TIME_EXPIRED");
        assertThat(count("match_side_result", draw.id())).isEqualTo(2);
        assertThat(count("scored_point", draw.id())).isZero();
        assertThat(jdbc.queryForObject("select count(*) from match_side_result where match_id = ? and participant_id is null",
                Integer.class, draw.id().value())).isEqualTo(2);
    }

    @Test
    void theSameMatchTwiceIsSavedOnceAndTheFirstOneStays() {
        MatchId id = new MatchId(UUID.randomUUID());
        MatchResult first = Fixtures.result(id);
        repository.saveIfAbsent(first);

        assertThat(repository.saveIfAbsent(Fixtures.draw(id))).isFalse();
        assertThat(repository.saveIfAbsent(first)).isFalse();

        assertThat(count("match_result", id)).isEqualTo(1);
        assertThat(count("match_side_result", id)).isEqualTo(2);
        assertThat(count("scored_point", id)).isEqualTo(8);
        assertThat(jdbc.queryForObject("select reason from match_result where match_id = ?", String.class, id.value()))
                .isEqualTo("SCORE_REACHED");
    }

    @Test
    void ifThePointsCannotBeWrittenNothingOfTheMatchIsLeft() {
        MatchResult result = Fixtures.result();
        jdbc.execute("create function fail_point() returns trigger as $$ begin raise exception 'no points today'; end $$ language plpgsql");
        jdbc.execute("create trigger fail_point before insert on scored_point for each row execute function fail_point()");
        try {
            assertThatThrownBy(() -> repository.saveIfAbsent(result)).isInstanceOf(DataAccessException.class);

            assertThat(count("match_result", result.id())).isZero();
            assertThat(count("match_side_result", result.id())).isZero();
            assertThat(count("scored_point", result.id())).isZero();
        } finally {
            jdbc.execute("drop trigger fail_point on scored_point");
            jdbc.execute("drop function fail_point()");
        }
        // Once the trouble is gone the same message can be saved: it was not marked as done
        assertThat(repository.saveIfAbsent(result)).isTrue();
    }

    @Test
    void theDatabaseRefusesWhatTheDomainShouldHaveRefused() {
        // A second layer: even if a row skips the domain, the tables keep the rules
        assertThatThrownBy(() -> jdbc.update("insert into match_result (match_id, message_id, tenant_id, venue_id, table_id,"
                + " game_type_id, mode, points_to_win, reason, started_at, ended_at)"
                + " values (?, ?, '', ?, ?, ?, 'TEAM', 5, 'SCORE_REACHED', now(), now())",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update("insert into match_result (match_id, message_id, tenant_id, venue_id, table_id,"
                + " game_type_id, mode, points_to_win, reason, started_at, ended_at)"
                + " values (?, ?, 'acme', ?, ?, ?, 'TEAM', 5, 'CANCELLED', now(), now())",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .as("a cancelled match is never saved")
                .isInstanceOf(DataAccessException.class);
    }
}
