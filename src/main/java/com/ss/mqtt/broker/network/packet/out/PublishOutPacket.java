package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

public class PublishOutPacket extends MqttWritablePacket {

    private int qos = 1;
    private boolean retained = false;
    private boolean dup = false;

    public PublishOutPacket(@NotNull MqttClient client) {
        super(client);
    }

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
