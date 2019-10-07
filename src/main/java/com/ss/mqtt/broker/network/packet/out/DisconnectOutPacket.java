package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

public class DisconnectOutPacket extends MqttWritablePacket {

    public DisconnectOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}
