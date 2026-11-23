package com.connectedgames.match.infrastructure;

import com.connectedgames.match.adapters.in.mqtt.MatchEventListener;
import com.connectedgames.match.adapters.out.mqtt.MqttAckPublisher;
import com.connectedgames.match.adapters.out.mqtt.MqttMatchCompletedPublisher;
import com.connectedgames.match.application.port.in.AcknowledgeEventUseCase;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.out.AckPublisher;
import com.connectedgames.match.application.port.out.MatchCompletedPublisher;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
class MqttConfiguration {

    static final String EVENTS_TOPIC = "venues/+/tables/+/matches/+/events";

    @Bean
    MqttPahoClientFactory mqttClientFactory(@Value("${match.mqtt.url}") String url) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{url});
        // The broker keeps the QoS 1 messages of this client while the service is down
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }

    // ---- events in: broker -> channel -> listener

    @Bean
    MessageChannel matchEventsIn() {
        return new DirectChannel();
    }

    @Bean
    MqttPahoMessageDrivenChannelAdapter matchEventsAdapter(MqttPahoClientFactory factory) {
        //fixed id: presistent so broker recons even after restart
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter("match-service-in", factory, EVENTS_TOPIC);
        adapter.setQos(1);
        adapter.setOutputChannel(matchEventsIn());
        return adapter;
    }

    @Bean
    MatchEventListener matchEventListener(RecordMatchEventUseCase record, AcknowledgeEventUseCase acknowledge) {
        return new MatchEventListener(record, acknowledge);
    }

    @Bean
    @ServiceActivator(inputChannel = "matchEventsIn")
    MessageHandler matchEventsHandler(MatchEventListener listener) {
        return message -> listener.onMessage(
                (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC), (String) message.getPayload());
    }

    // ---- messages out: acks and MatchCompleted -> channel -> broker. One connection, so one client id.

    @Bean
    MessageChannel matchOut() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "matchOut")
    MessageHandler matchOutHandler(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler("match-service-out", factory);
        handler.setAsync(false);
        handler.setDefaultQos(1);
        return handler;
    }

    @Bean
    AckPublisher ackPublisher() {
        return new MqttAckPublisher(matchOut());
    }

    @Bean
    MatchCompletedPublisher matchCompletedPublisher() {
        return new MqttMatchCompletedPublisher(matchOut());
    }
}
