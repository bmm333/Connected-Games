package com.connectedgames.match.infrastructure;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.connectedgames.match.application.port.in.EventTopic;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.application.port.out.MatchChangeNotifier;
import com.connectedgames.match.domain.event.MatchEvent;

public class TransactionalRecordMatchEvent implements RecordMatchEventUseCase {

    private final RecordMatchEventUseCase delegate;
    private final TransactionTemplate transaction;
    private final MatchChangeNotifier notifier;

    TransactionalRecordMatchEvent(RecordMatchEventUseCase delegate, PlatformTransactionManager transactionManager, MatchChangeNotifier notifier)
    {
        this.delegate=delegate;
        this.transaction= new TransactionTemplate(transactionManager);
        this.notifier=notifier;
    }
    @Override
    public RecordResult record(MatchEvent event,EventTopic topic)
    {
        RecordResult result= transaction.execute(status -> delegate.record(event,topic));
        if(result instanceof RecordResult.Accepted){
            notifier.matchChanged(event.tableId(),event.matchId());
        }
        return result;
    }
}
