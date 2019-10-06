package com.ss.mqtt.broker.network.packet.in;

public class PublishReceivedInPacket extends MqttReadablePacket {
    protected PublishReceivedInPacket(byte info) {
        super(info);
    }
}
