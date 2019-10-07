package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

public class PublishAckOutPacket extends MqttWritablePacket {

    public PublishAckOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
