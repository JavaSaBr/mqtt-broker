package com.ss.mqtt.broker.network.packet.in;

public class PingRequestInPacket extends MqttReadablePacket {
    protected PingRequestInPacket(byte info) {
        super(info);
    }
}
