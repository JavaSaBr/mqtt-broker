package com.ss.mqtt.broker.network.packet.in;

public class SubscribeInPacket extends MqttReadablePacket {
    protected SubscribeInPacket(byte info) {
        super(info);
    }
}
