package com.connectedgames.match.infrastructure;

import com.connectedgames.match.application.port.in.EventTopic;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.application.port.out.MatchChangeNotifier;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.GoalScored;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransactionalRecordMatchEventTest {

    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final MatchId MATCH = new MatchId(UUID.randomUUID());
    private static final GoalScored GOAL = new GoalScored(new EventId(UUID.randomUUID()), VENUE, TABLE, MATCH, 2,
            Instant.parse("2026-11-16T20:00:00Z"), Side.RED);
    private static final EventTopic TOPIC = new EventTopic(VENUE, TABLE, MATCH);

    private final RecordMatchEventUseCase delegate = mock(RecordMatchEventUseCase.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final MatchChangeNotifier notifier = mock(MatchChangeNotifier.class);
    private final RecordMatchEventUseCase useCase = new TransactionalRecordMatchEvent(delegate, transactions, notifier);

    @Test
    void tellsWhoWatchesOnlyAfterTheCommit() {
        when(delegate.record(GOAL, TOPIC)).thenReturn(new RecordResult.Accepted(GOAL.id()));

        RecordResult result = useCase.record(GOAL, TOPIC);

        assertThat(result).isEqualTo(new RecordResult.Accepted(GOAL.id()));
        InOrder order = inOrder(delegate, transactions, notifier);
        order.verify(delegate).record(GOAL, TOPIC);
        order.verify(transactions).commit(any());
        order.verify(notifier).matchChanged(TABLE, MATCH);
    }

    @Test
    void aDuplicateChangedNothingSoNobodyIsTold() {
        when(delegate.record(GOAL, TOPIC)).thenReturn(new RecordResult.Duplicate(GOAL.id()));

        useCase.record(GOAL, TOPIC);

        verifyNoInteractions(notifier);
    }

    @Test
    void aRejectedEventChangedNothingSoNobodyIsTold() {
        when(delegate.record(GOAL, TOPIC)).thenReturn(new RecordResult.Rejected(GOAL.id(), "no"));

        useCase.record(GOAL, TOPIC);

        verifyNoInteractions(notifier);
    }

    @Test
    void ifSavingFailsTheTransactionIsRolledBackAndNobodyIsTold() {
        when(delegate.record(GOAL, TOPIC)).thenThrow(new IllegalStateException("database down"));

        assertThatThrownBy(() -> useCase.record(GOAL, TOPIC)).isInstanceOf(IllegalStateException.class);

        verify(transactions).rollback(any());
        verify(transactions, never()).commit(any());
        verifyNoInteractions(notifier);
    }
}
