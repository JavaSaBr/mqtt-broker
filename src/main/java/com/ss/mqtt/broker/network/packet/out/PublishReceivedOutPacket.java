package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

public class PublishReceivedOutPacket extends MqttWritablePacket {

    public PublishReceivedOutPacket(@NotNull MqttClient client) {
        super(client);
    }
}