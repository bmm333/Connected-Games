package com.connectedgames.stats.application;

import com.connectedgames.stats.Fixtures;
import com.connectedgames.stats.application.port.in.RecordOutcome;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.result.MatchResult;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordMatchCompletedServiceTest {

    private final InMemoryMatchResultRepository results = new InMemoryMatchResultRepository();
    private final RecordMatchCompletedService service = new RecordMatchCompletedService(results);

    @Test
    void aClosedMatchIsRecorded() {
        MatchResult result = Fixtures.result();

        assertThat(service.record(result)).isEqualTo(RecordOutcome.RECORDED);
        assertThat(results.saved).containsEntry(result.id(), result);
    }

    @Test
    void aDrawIsRecordedToo() {
        MatchResult draw = Fixtures.draw(new MatchId(UUID.randomUUID()));

        assertThat(service.record(draw)).isEqualTo(RecordOutcome.RECORDED);
    }

    @Test
    void theSameMatchTwiceIsRecordedOnceAndTheFirstOneStays() {
        MatchId id = new MatchId(UUID.randomUUID());
        MatchResult first = Fixtures.result(id);
        MatchResult again = Fixtures.draw(id);
        service.record(first);

        assertThat(service.record(again)).isEqualTo(RecordOutcome.DUPLICATE);
        assertThat(results.saved).hasSize(1).containsEntry(id, first);
    }

    @Test
    void aCancelledMatchIsIgnoredAndNeverSaved() {
        MatchResult cancelled = Fixtures.cancelled(new MatchId(UUID.randomUUID()));

        assertThat(service.record(cancelled)).isEqualTo(RecordOutcome.IGNORED);
        assertThat(results.saved).isEmpty();
    }

    @Test
    void ifTheRepositoryFailsTheErrorGoesUpSoTheBrokerSendsTheMessageAgain() {
        RecordMatchCompletedService failing = new RecordMatchCompletedService(result -> {
            throw new IllegalStateException("database down");
        });

        assertThatThrownBy(() -> failing.record(Fixtures.result())).isInstanceOf(IllegalStateException.class);
    }
}
