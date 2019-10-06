package com.ss.mqtt.broker.network.packet.in;

/**
 * Connection request.
 */
public class ConnectInPacket extends MqttReadablePacket {

    public static final int PACKET_TYPE = 1;

    public ConnectInPacket(byte info) {
        super(info);
    }
}
