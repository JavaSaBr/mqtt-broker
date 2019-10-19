package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

/**
 * Publish release (QoS 2 delivery part 2).
 */
public class PublishReleaseOutPacket extends MqttWritablePacket {

    public PublishReleaseOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
