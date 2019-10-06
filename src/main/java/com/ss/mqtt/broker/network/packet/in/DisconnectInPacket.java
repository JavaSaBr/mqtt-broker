package com.ss.mqtt.broker.network.packet.in;

public class DisconnectInPacket extends MqttReadablePacket {
    public DisconnectInPacket(byte info) {
        super(info);
    }
}
