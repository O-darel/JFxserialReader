package org.serial.serial.service;


import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class MqttService {
    private MqttClient mqttClient;
    private String currentBroker;

    public void connect(String broker, String username, String password) throws Exception {
        String clientId = "SerialMqttBridge_" + UUID.randomUUID().toString().substring(0, 8);
        currentBroker = broker;

        mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);

        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
        }

        if (password != null && !password.isEmpty()) {
            options.setPassword(password.toCharArray());
        }

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("MQTT Connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // Not subscribing to topics, so this won't be called
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Message delivered successfully
            }
        });

        mqttClient.connect(options);
    }

    public void disconnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
            } catch (MqttException e) {
                System.err.println("Error disconnecting from MQTT: " + e.getMessage());
            }
        }
    }

    public void publish(String topic, String payload) throws Exception {
        if (mqttClient == null || !mqttClient.isConnected()) {
            throw new Exception("MQTT client is not connected");
        }

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1); // QoS 1: At least once delivery
        message.setRetained(false);

        mqttClient.publish(topic, message);
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    public String getCurrentBroker() {
        return currentBroker;
    }
}
