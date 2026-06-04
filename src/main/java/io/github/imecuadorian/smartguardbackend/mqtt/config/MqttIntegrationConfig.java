package io.github.imecuadorian.smartguardbackend.mqtt.config;

import io.github.imecuadorian.smartguardbackend.mqtt.application.MqttMessageRouter;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.nio.charset.StandardCharsets;

@Configuration
public class MqttIntegrationConfig {

    @Bean("mqttInputChannel")
    MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean("mqttOutboundChannel")
    MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ConditionalOnProperty(name = "smartguard.mqtt.enabled", havingValue = "true", matchIfMissing = true)
    MqttPahoClientFactory mqttClientFactory(
            @Value("${smartguard.mqtt.broker-url}") String brokerUrl,
            @Value("${smartguard.mqtt.username:}") String username,
            @Value("${smartguard.mqtt.password:}") String password
    ) {
        var options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        if (!username.isBlank()) {
            options.setUserName(username);
        }
        if (!password.isBlank()) {
            options.setPassword(password.toCharArray());
        }

        var factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    @ConditionalOnProperty(name = "smartguard.mqtt.enabled", havingValue = "true", matchIfMissing = true)
    MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter(
            MqttPahoClientFactory mqttClientFactory,
            MessageChannel mqttInputChannel,
            @Value("${smartguard.mqtt.client-id}") String clientId,
            @Value("${smartguard.mqtt.topic-prefix:smartguard}") String topicPrefix
    ) {
        var adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId + "-inbound",
                mqttClientFactory,
                topicPrefix + "/devices/+/readings",
                topicPrefix + "/devices/+/access-events"
        );
        adapter.setConverter(new DefaultPahoMessageConverter(StandardCharsets.UTF_8.name()));
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel);
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    MessageHandler mqttInboundHandler(MqttMessageRouter router) {
        return message -> {
            String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
            router.route(topic == null ? "" : topic, String.valueOf(message.getPayload()));
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    @ConditionalOnProperty(name = "smartguard.mqtt.enabled", havingValue = "true", matchIfMissing = true)
    MessageHandler mqttOutboundHandler(
            MqttPahoClientFactory mqttClientFactory,
            @Value("${smartguard.mqtt.client-id}") String clientId,
            @Value("${smartguard.mqtt.topic-prefix:smartguard}") String topicPrefix
    ) {
        var handler = new MqttPahoMessageHandler(clientId + "-outbound", mqttClientFactory);
        handler.setAsync(true);
        handler.setDefaultQos(1);
        handler.setDefaultTopic(topicPrefix + "/devices/commands");
        handler.setConverter(new DefaultPahoMessageConverter(StandardCharsets.UTF_8.name()));
        return handler;
    }
}
