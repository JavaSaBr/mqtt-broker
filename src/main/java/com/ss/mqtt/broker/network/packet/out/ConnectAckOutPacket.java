package com.ss.mqtt.broker.network.packet.out;

/**
 * Connect acknowledgment.
 */
public class ConnectAckOutPacket extends MqttWritablePacket {

    @Override
    protected byte getPacketType() {
        return 2;
    }
}
