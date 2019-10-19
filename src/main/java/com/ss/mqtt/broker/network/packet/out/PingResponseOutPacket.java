package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

/**
 * PING response.
 */
public class PingResponseOutPacket extends MqttWritablePacket {

    public PingResponseOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
