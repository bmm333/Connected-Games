package com.connectedgames.stats;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class MosquittoTestConfiguration {

    @Bean
    public GenericContainer<?> mosquittoContainer() {
        return new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0.22"))
                .withExposedPorts(1883)
                .withCopyFileToContainer(MountableFile.forHostPath("../../infra/docker/mosquitto/mosquitto.conf"),
                        "/mosquitto/config/mosquitto.conf")
                .waitingFor(Wait.forLogMessage(".*mosquitto version .* running.*", 1));
    }

    @Bean
    public DynamicPropertyRegistrar mqttUrl(GenericContainer<?> mosquittoContainer) {
        return registry -> registry.add("stats.mqtt.url",
                () -> "tcp://" + mosquittoContainer.getHost() + ":" + mosquittoContainer.getMappedPort(1883));
    }
}
