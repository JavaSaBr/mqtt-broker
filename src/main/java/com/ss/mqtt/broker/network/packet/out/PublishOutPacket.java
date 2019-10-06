package com.ss.mqtt.broker.network.packet.out;

public class PublishOutPacket extends MqttWritablePacket {

    private int qos = 1;
    private boolean retained = false;
    private boolean dup = false;

    @Override
    protected byte getPacketFlags() {

        byte info = (byte) (qos << 1);

        if (retained) {
            info |= 0x01;
        }

        if (dup) {
            info |= 0x08;
        }

        return info;
    }
}
