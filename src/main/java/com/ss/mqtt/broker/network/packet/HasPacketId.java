package com.ss.mqtt.broker.network.packet;

import com.ss.rlib.network.packet.Packet;

public interface HasPacketId extends Packet {

    int getPacketId();
}
