package com.connectedgames.stats.adapters.in.mqtt;

import com.connectedgames.stats.Messages;
import com.connectedgames.stats.application.port.in.RecordMatchCompletedUseCase;
import com.connectedgames.stats.application.port.in.RecordOutcome;
import com.connectedgames.stats.domain.result.MatchResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchCompletedListenerTest {

    private final List<MatchResult> recorded = new ArrayList<>();
    private final RecordMatchCompletedUseCase record = result -> {
        recorded.add(result);
        return RecordOutcome.RECORDED;
    };
    private final MatchCompletedListener listener = new MatchCompletedListener(record);

    @Test
    void aReadableMessageIsRecorded() {
        UUID match = UUID.randomUUID();

        listener.onMessage(Messages.completed(match));

        assertThat(recorded).hasSize(1);
        assertThat(recorded.get(0).id().value()).isEqualTo(match);
    }

    @Test
    void aMessageNobodyCanReadIsDroppedWithoutAnError() {
        listener.onMessage("not json");
        listener.onMessage(Messages.completed(UUID.randomUUID()).replace("SCORE_REACHED", "GOLDEN_GOAL"));

        assertThat(recorded).isEmpty();
    }

    @Test
    void theNextMessageStillWorksAfterADroppedOne() {
        listener.onMessage("not json");
        listener.onMessage(Messages.draw(UUID.randomUUID()));

        assertThat(recorded).hasSize(1);
    }

    @Test
    void ifTheUseCaseFailsTheErrorGoesUpSoTheBrokerSendsTheMessageAgain() {
        MatchCompletedListener failing = new MatchCompletedListener(result -> {
            throw new IllegalStateException("database down");
        });

        assertThatThrownBy(() -> failing.onMessage(Messages.completed(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class);
    }
}
