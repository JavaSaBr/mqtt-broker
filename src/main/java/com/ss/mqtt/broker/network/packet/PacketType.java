package com.ss.mqtt.broker.network.packet;

public enum PacketType {
    RESERVED,
    CONNECT_REQUEST,
    CONNECT_ACK,
    PUBLISH,
    PUBLISH_ACK,
    PUBLISH_RECEIVED,
    PUBLISH_RELEASED,
    PUBLISH_COMPLETED,
    SUBSCRIBE,
    SUBSCRIBE_ACK,
    UNSUBSCRIBE,
    UNSUBSCRIBE_ACK,
    PING_REQUEST,
    PING_RESPONSE,
    DISCONNECT,
    AUTHENTICATE;

    private static final PacketType[] VALUES = values();

    public static PacketType fromByte(byte packetType) {
        return VALUES[packetType];
    }
}
