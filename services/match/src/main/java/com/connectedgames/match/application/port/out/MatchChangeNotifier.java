package com.connectedgames.match.application.port.out;

import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;

public interface MatchChangeNotifier {

    void matchChanged(TableId tableId,MatchId matchId);
}
