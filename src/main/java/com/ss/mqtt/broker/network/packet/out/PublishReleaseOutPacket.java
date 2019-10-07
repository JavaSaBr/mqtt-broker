package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

public class PublishReleaseOutPacket extends MqttWritablePacket {

    public PublishReleaseOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
