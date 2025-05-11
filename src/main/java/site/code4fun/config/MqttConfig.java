package site.code4fun.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.UUID;

@Slf4j
@Configuration
public class MqttConfig {

    @Bean
    @Lazy
    public IMqttClient mqttClient() {
        String publisherId = UUID.randomUUID().toString();
        IMqttClient client = null;
        MqttMessage message = new MqttMessage();
        message.setPayload(publisherId.getBytes());
        message.setRetained(true);
        message.setQos(0);

        try {
            client = new MqttClient("tcp://test.mosquitto.org:1883", publisherId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            client.connect(options);
//            client.publish("/trungtq", message);
            log.info("IMqttClient connected: {}", client.isConnected());

//            client.subscribeWithResponse("/trungtq", (topic, msg) -> System.out.println(msg.getId() + " -> " + new String(msg.getPayload())));
        } catch (MqttException e) {
            log.error("IMqttClient {}", e.getMessage());
        }
        return client;
    }
}
