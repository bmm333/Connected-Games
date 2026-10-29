package com.connectedgames.catalog.adapters.in.web;

import com.connectedgames.catalog.application.port.in.TableLookup;
import com.connectedgames.catalog.domain.gametype.GameMode;
import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.table.TableStatus;
import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueType;

import java.util.UUID;

/*web dtos*/
/*JSON bodies of catalog api, shaped as in contracts/openapi/catalog.yaml*/

final class WebResponses 
{
    private WebResponses()
    {
        //response container only
    }
    record VenueSummaryResponse(UUID id, String name,String city,VenueType type)
    {
        static VenueSummaryResponse from(Venue v)
        {
            return new VenueSummaryResponse(v.id().value(), v.name(), v.city(), v.type());
        }
    }
    record VenueResponse(UUID id,String name,String city,VenueType type,String address,UUID organizationId)
    {
        static VenueResponse from(Venue v){
            return new VenueResponse(v.id().value(), v.name(), v.city(), v.type(), v.address(), v.organizationId().value());
        }
    }
    record TableResponse(UUID id,UUID venueId,String name,String code,UUID gameTypeId,TableStatus status)
    {
        static TableResponse from(Table t)
        {
            return new TableResponse(t.id().value(), t.venueId().value(), t.name(), t.code().value(), t.gameTypeId().value(), t.status());
        }
    }
    record GameTypeResponse(UUID id,String name,GameMode mode,int participantsPerSide,int pointsToWin)
    {
        static GameTypeResponse from(GameType g)
        {
            return new GameTypeResponse(g.id().value(), g.name(), g.mode(), g.participantsPerSide(), g.pointsToWin());
        }
    }
    record TableLookupResponse(TableResponse table,VenueSummaryResponse venue,GameTypeResponse gameType)
    {
        static TableLookupResponse from(TableLookup lookup)
        {
            return new TableLookupResponse(
                    TableResponse.from(lookup.table()),
                    VenueSummaryResponse.from(lookup.venue()),
                    GameTypeResponse.from(lookup.gameType()));
        }
    }


}
