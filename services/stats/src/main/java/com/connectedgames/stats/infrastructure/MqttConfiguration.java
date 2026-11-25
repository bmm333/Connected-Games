package com.connectedgames.stats.infrastructure;

import com.connectedgames.stats.adapters.in.mqtt.MatchCompletedListener;
import com.connectedgames.stats.application.port.in.RecordMatchCompletedUseCase;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
class MqttConfiguration {
    static final String MATCH_COMPLETED_TOPIC = "platform/matches/completed";

    @Bean
    MqttPahoClientFactory mqttClientFactory(@Value("${stats.mqtt.url}") String url){
        MqttConnectOptions options=new MqttConnectOptions();
        options.setServerURIs(new String[]{url});
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }
    
    @Bean
    MessageChannel matchCompletedIn(){
        return new DirectChannel();
    }
    
    @Bean
    MqttPahoMessageDrivenChannelAdapter matchCompletedAdapter(MqttPahoClientFactory factory)
    {
        //fixed id: session persistent, broker can find it everytime
        MqttPahoMessageDrivenChannelAdapter adapter= 
            new MqttPahoMessageDrivenChannelAdapter("stats-service-in", factory,MATCH_COMPLETED_TOPIC);
        adapter.setQos(1);
        adapter.setOutputChannel(matchCompletedIn());
        return adapter;
    }
    @Bean
    MatchCompletedListener matchCompletedListener(RecordMatchCompletedUseCase record)
    {
        return new MatchCompletedListener(record);
    }

    @Bean
    @ServiceActivator(inputChannel = "matchCompletedIn")
    MessageHandler matchCompletedHandler(MatchCompletedListener listener) {
        return message -> listener.onMessage((String) message.getPayload());
    }
}
