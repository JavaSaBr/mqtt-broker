package com.ss.mqtt.broker.network.packet.in;

public class PublishReleaseInPacket extends MqttReadablePacket {
    protected PublishReleaseInPacket(byte info) {
        super(info);
    }
}
