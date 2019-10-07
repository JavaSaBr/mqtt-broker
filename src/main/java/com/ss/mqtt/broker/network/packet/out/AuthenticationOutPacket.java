package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

public class AuthenticationOutPacket extends MqttWritablePacket {

    public AuthenticationOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
