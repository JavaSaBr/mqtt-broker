package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

/**
 * Publish complete (QoS 2 delivery part 3).
 */
public class PublishCompleteOutPacket extends MqttWritablePacket {

    public PublishCompleteOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
