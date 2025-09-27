package com.ss.mqtt.broker.network.packet;

import javasabr.rlib.network.packet.Packet;

public interface HasPacketId extends Packet {

    int getPacketId();
}
