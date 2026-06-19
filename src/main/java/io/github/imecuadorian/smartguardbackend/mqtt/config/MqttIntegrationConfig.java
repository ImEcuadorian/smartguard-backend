package io.github.imecuadorian.smartguardbackend.mqtt.config;

import io.github.imecuadorian.smartguardbackend.mqtt.application.MqttMessageRouter;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MqttIntegrationConfig.class);

    @Bean("mqttInputChannel")
    MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean("mqttOutboundChannel")
    MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ConditionalOnProperty(
            name = "smartguard.mqtt.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    MqttPahoClientFactory mqttClientFactory(
            @Value("${smartguard.mqtt.broker-url}") String brokerUrl,
            @Value("${smartguard.mqtt.username:}") String username,
            @Value("${smartguard.mqtt.password:}") String password
    ) {
        var options = new MqttConnectOptions();

        options.setServerURIs(new String[]{brokerUrl});
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

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
    @ConditionalOnProperty(
            name = "smartguard.mqtt.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter(
            MqttPahoClientFactory mqttClientFactory,
            MessageChannel mqttInputChannel,
            @Value("${smartguard.mqtt.client-id}") String clientId,
            @Value("${smartguard.mqtt.topic-prefix:smartguard}") String topicPrefix
    ) {
        String readingsTopic = topicPrefix + "/devices/+/readings";
        String accessTopic = topicPrefix + "/devices/+/access-events";

        log.info("MQTT inbound subscribiendo a: {}, {}", readingsTopic, accessTopic);

        var adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId + "-inbound",
                mqttClientFactory,
                readingsTopic,
                accessTopic
        );

        var converter = new DefaultPahoMessageConverter(StandardCharsets.UTF_8.name());

        // Forzamos que llegue texto, no byte[].
        converter.setPayloadAsBytes(false);

        adapter.setConverter(converter);
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel);

        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    MessageHandler mqttInboundHandler(MqttMessageRouter router) {
        return message -> {
            String topic = message.getHeaders()
                    .get(MqttHeaders.RECEIVED_TOPIC, String.class);

            Object rawPayload = message.getPayload();

            String payload;

            if (rawPayload instanceof byte[] bytes) {
                payload = new String(bytes, StandardCharsets.UTF_8);
            } else {
                payload = String.valueOf(rawPayload);
            }

            log.info("MQTT recibido | topic={} | payload={}", topic, payload);

            try {
                router.route(topic == null ? "" : topic, payload);
            } catch (Exception exception) {
                log.error(
                        "Error procesando MQTT | topic={} | payload={}",
                        topic,
                        payload,
                        exception
                );
            }
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    @ConditionalOnProperty(
            name = "smartguard.mqtt.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    MessageHandler mqttOutboundHandler(
            MqttPahoClientFactory mqttClientFactory,
            @Value("${smartguard.mqtt.client-id}") String clientId,
            @Value("${smartguard.mqtt.topic-prefix:smartguard}") String topicPrefix
    ) {
        var handler = new MqttPahoMessageHandler(
                clientId + "-outbound",
                mqttClientFactory
        );

        handler.setAsync(true);
        handler.setDefaultQos(1);
        handler.setDefaultTopic(topicPrefix + "/devices/commands");

        var converter = new DefaultPahoMessageConverter(StandardCharsets.UTF_8.name());
        converter.setPayloadAsBytes(false);

        handler.setConverter(converter);

        return handler;
    }
}