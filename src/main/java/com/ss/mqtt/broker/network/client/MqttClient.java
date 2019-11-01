package com.ss.mqtt.broker.network.client;

import com.ss.mqtt.broker.config.MqttConnectionConfig;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import org.jetbrains.annotations.NotNull;

public interface MqttClient {

    @NotNull MqttConnectionConfig getConnectionConfig();
    @NotNull String getClientId();

    int getKeepAlive();
    int getMaximumPacketSize();
    int getReceiveMax();
    int getTopicAliasMaximum();

    long getSessionExpiryInterval();

    void send(@NotNull MqttWritablePacket packet);
}
