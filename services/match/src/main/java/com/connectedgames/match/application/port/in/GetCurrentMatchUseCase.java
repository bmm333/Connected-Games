package com.connectedgames.match.application.port.in;

import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.match.Match;

public interface GetCurrentMatchUseCase {

    Match getCurrentMatch(TableId tableId);
}
