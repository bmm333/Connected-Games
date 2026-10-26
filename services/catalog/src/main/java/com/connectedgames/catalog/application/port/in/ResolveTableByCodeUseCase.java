package com.connectedgames.catalog.application.port.in;

import com.connectedgames.catalog.domain.table.QrCode;

public interface ResolveTableByCodeUseCase {

    /**@throws com.connectedgames.catalog.application.NotFoundException if no table with this code*/
    TableLookup resolve(QrCode code);
}
