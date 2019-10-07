package com.ss.mqtt.broker.network.packet.factory;

import com.ss.mqtt.broker.model.ConnectReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.out.ConnectAck311OutPacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import org.jetbrains.annotations.NotNull;

public class Mqtt311PacketOutFactory extends MqttPacketOutFactory {

    @Override
    public @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectReasonCode reasonCode,
        boolean sessionPresent
    ) {
        return new ConnectAck311OutPacket(client, reasonCode, sessionPresent);
    }
}
