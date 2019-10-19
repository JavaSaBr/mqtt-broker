package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

/**
 * Publish received (QoS 2 delivery part 1).
 */
public class PublishReceivedOutPacket extends MqttWritablePacket {

    public PublishReceivedOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
