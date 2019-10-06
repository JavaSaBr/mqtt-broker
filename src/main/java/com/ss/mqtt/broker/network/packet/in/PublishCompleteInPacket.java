package com.ss.mqtt.broker.network.packet.in;

public class PublishCompleteInPacket extends MqttReadablePacket {
    protected PublishCompleteInPacket(byte info) {
        super(info);
    }
}
