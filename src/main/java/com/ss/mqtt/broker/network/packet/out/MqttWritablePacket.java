package com.ss.mqtt.broker.network.packet.out;

import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.network.packet.impl.AbstractWritablePacket;

public class MqttWritablePacket extends AbstractWritablePacket {

    public final int getPacketTypeAndFlags() {

        var type = getPacketType();
        var controlFlags = getPacketFlags();

        return NumberUtils.setHighByteBits(controlFlags, type);
    }

    protected byte getPacketType() {
        throw new UnsupportedOperationException();
    }

    protected byte getPacketFlags() {
        return 0;
    }
}
