package com.ss.mqtt.broker.network.packet.out;

import com.ss.rlib.network.packet.impl.AbstractWritablePacket;

public class MqttWritablePacket extends AbstractWritablePacket {

    public final int getPacketTypeAndFlags() {

        var type = getPacketType();
        var controlFlags = getPacketFlags();

        return ((type & 0x0F) << 4) ^ (controlFlags & 0x0F);
    }

    protected byte getPacketType() {
        throw new UnsupportedOperationException();
    }

    protected byte getPacketFlags() {
        return 0;
    }
}
