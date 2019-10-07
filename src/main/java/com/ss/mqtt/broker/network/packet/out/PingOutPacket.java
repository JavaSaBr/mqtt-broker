package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

public class PingOutPacket extends MqttWritablePacket {

    public PingOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
