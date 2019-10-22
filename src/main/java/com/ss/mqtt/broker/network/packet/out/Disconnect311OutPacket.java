package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

/**
 * Disconnect notification.
 */
public class Disconnect311OutPacket extends MqttWritablePacket {

    public Disconnect311OutPacket(@NotNull MqttClient client) {
        super(client);
    }

    @Override
    public int getExpectedLength() {
        return 0;
    }
}
