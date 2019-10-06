package com.ss.mqtt.broker.network.packet.in;

public class AuthenticationInPacket extends MqttReadablePacket {
    protected AuthenticationInPacket(byte info) {
        super(info);
    }
}
