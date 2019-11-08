package com.ss.mqtt.broker.network.client;

import com.ss.mqtt.broker.config.MqttConnectionConfig;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.network.packet.factory.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface MqttClient {

    @NotNull MqttPacketOutFactory getPacketOutFactory();
    @NotNull MqttConnectionConfig getConnectionConfig();

    @NotNull String getClientId();
    @NotNull MqttSession getSession();

    int getKeepAlive();
    int getMaximumPacketSize();
    int getReceiveMax();
    int getTopicAliasMaximum();

    long getSessionExpiryInterval();

    void send(@NotNull MqttWritablePacket packet);
}
