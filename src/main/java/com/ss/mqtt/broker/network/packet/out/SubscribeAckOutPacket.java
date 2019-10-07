package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

public class SubscribeAckOutPacket extends MqttWritablePacket {

    public SubscribeAckOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
