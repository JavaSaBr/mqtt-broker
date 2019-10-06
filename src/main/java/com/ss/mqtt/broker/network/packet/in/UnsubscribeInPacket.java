package com.ss.mqtt.broker.network.packet.in;

public class UnsubscribeInPacket extends MqttReadablePacket {
    protected UnsubscribeInPacket(byte info) {
        super(info);
    }
}
