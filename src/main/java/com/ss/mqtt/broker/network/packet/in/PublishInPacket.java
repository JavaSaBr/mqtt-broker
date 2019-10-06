package com.ss.mqtt.broker.network.packet.in;

public class PublishInPacket extends MqttReadablePacket {

    public PublishInPacket(byte info) {
        super(info);
        int qos = (info >> 1) & 0x03;
        boolean retained = (info & 0x01) == 0x01;
        boolean duplicate = (info & 0x08) == 0x08;
    }
}
