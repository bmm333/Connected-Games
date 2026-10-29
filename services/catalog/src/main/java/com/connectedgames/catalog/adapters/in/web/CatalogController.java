package com.connectedgames.catalog.adapters.in.web;

import com.connectedgames.catalog.adapters.in.web.WebResponses.GameTypeResponse;
import com.connectedgames.catalog.adapters.in.web.WebResponses.TableLookupResponse;
import com.connectedgames.catalog.adapters.in.web.WebResponses.TableResponse;
import com.connectedgames.catalog.adapters.in.web.WebResponses.VenueResponse;
import com.connectedgames.catalog.adapters.in.web.WebResponses.VenueSummaryResponse;
import com.connectedgames.catalog.application.NotFoundException;
import com.connectedgames.catalog.application.port.in.BrowseCatalogUseCase;
import com.connectedgames.catalog.application.port.in.ResolveTableByCodeUseCase;
import com.connectedgames.catalog.domain.table.QrCode;
import com.connectedgames.catalog.domain.venue.VenueId;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Rest endpoints of contracts/openapi/catalog.yaml Translates http to use cases and back
 * */
@RestController
class CatalogController {
    private final ResolveTableByCodeUseCase resolveTableByCode;
    private final BrowseCatalogUseCase  browseCatalog;
    
    CatalogController(ResolveTableByCodeUseCase resolveTableByCode,
    BrowseCatalogUseCase browseCatalog)
    {
        this.resolveTableByCode=resolveTableByCode;
        this.browseCatalog=browseCatalog;
    }
    @GetMapping("/tables/by-code/{code}")
    TableLookupResponse resolveTableByCode(@PathVariable String code)
    {
        // step1 create qr code, if format not valid check
        // step2 call usecase and check result with TableLookupResponse.from
        QrCode qrCode;
        try{
            qrCode= new QrCode(code);
        } catch(IllegalArgumentException e)
        {
            throw new NotFoundException("No table with code: "+ code);
        }
        return TableLookupResponse.from(resolveTableByCode.resolve(qrCode));
    }
    @GetMapping("/venues")
    List<VenueSummaryResponse> listVenues(@RequestParam Optional<String> city)
    {
        //step1 call browseCataloag.listVenues(city) then map stream to list
        return browseCatalog.listVenues(city).stream().map(VenueSummaryResponse::from).toList();
    }
    @GetMapping("/venues/{venueId}")
    VenueResponse getVenue(@PathVariable UUID venueId)
    {
        //convert uuid in venueid , call use case , convert
        return VenueResponse.from(browseCatalog.getVenue(new VenueId(venueId)));

    }

    @GetMapping("/venues/{venueId}/tables")
    List<TableResponse> listTables(@PathVariable UUID venueId)
    {
      return browseCatalog.listTables(new VenueId(venueId)).stream().map(TableResponse::from).toList();
    }
    @GetMapping("/game-types")
    List<GameTypeResponse> listGameTypes()
    {
        return browseCatalog.listGameTypes().stream().map(GameTypeResponse::from).toList();
    }

}
