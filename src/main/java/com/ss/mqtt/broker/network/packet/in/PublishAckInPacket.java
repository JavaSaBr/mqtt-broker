package com.ss.mqtt.broker.network.packet.in;

public class PublishAckInPacket extends MqttReadablePacket {
    protected PublishAckInPacket(byte info) {
        super(info);
    }
}
