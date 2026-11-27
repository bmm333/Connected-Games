package com.connectedgames.stats.application;

import java.util.Set;

import com.connectedgames.stats.domain.TenantId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViewerTest {

    @Test
    void aViewerOfTenantsNeedsAtLeastOne() {
        assertThatThrownBy(() -> new Viewer.OfTenants(Set.of())).isInstanceOf(IllegalArgumentException.class);
        assertThat(new Viewer.OfTenants(Set.of(new TenantId("acme"))).tenants()).hasSize(1);
    }

    @Test
    void aPlayerViewerNeedsAnId() {
        assertThatThrownBy(() -> new Viewer.OfPlayer(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Viewer.OfPlayer(null)).isInstanceOf(NullPointerException.class);
    }
}
