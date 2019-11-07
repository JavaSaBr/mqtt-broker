package com.ss.mqtt.broker.network.client;

import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import org.jetbrains.annotations.NotNull;

public interface UnsafeMqttClient extends MqttClient {

    @NotNull MqttConnection getConnection();

    void handle(@NotNull MqttReadablePacket packet);

    void configure(
        long sessionExpiryInterval,
        int receiveMax,
        int maximumPacketSize,
        int topicAliasMaximum,
        int keepAlive
    );

    void setClientId(@NotNull String clientId);

    void setSession(@NotNull MqttSession session);

    void reject(@NotNull ConnectAckReasonCode reasonCode);
}
