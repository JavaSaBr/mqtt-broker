package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

/**
 * Unsubscribe acknowledgement.
 */
public class UnsubscribeAckOutPacket extends MqttWritablePacket {

    public UnsubscribeAckOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
