package com.ss.mqtt.broker.network.packet.factory;

import com.ss.mqtt.broker.model.ConnectReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import org.jetbrains.annotations.NotNull;

public abstract class MqttPacketOutFactory {

    public abstract @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectReasonCode reasonCode,
        boolean sessionPresent
    );
}
