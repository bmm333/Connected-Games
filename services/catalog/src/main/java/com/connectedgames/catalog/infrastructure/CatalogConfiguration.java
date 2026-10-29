package com.connectedgames.catalog.infrastructure;

import com.connectedgames.catalog.application.BrowseCatalogService;
import com.connectedgames.catalog.application.ResolveTableByCodeService;
import com.connectedgames.catalog.application.port.in.BrowseCatalogUseCase;
import com.connectedgames.catalog.application.port.in.ResolveTableByCodeUseCase;
import com.connectedgames.catalog.application.port.out.GameTypeRepository;
import com.connectedgames.catalog.application.port.out.TableRepository;
import com.connectedgames.catalog.application.port.out.VenueRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the use cases. The app layer has no Spring,
 * so this is the only place where Spring sees it
 */
@Configuration
class CatalogConfiguration {

    @Bean
    BrowseCatalogUseCase browseCatalog(VenueRepository venues, TableRepository tables, GameTypeRepository gameTypes) {
        return new BrowseCatalogService(venues, tables, gameTypes);
    }

    @Bean
    ResolveTableByCodeUseCase resolveTableByCode(TableRepository tables, VenueRepository venues, GameTypeRepository gameTypes) {
        return new ResolveTableByCodeService(tables, venues, gameTypes);
    }
}
