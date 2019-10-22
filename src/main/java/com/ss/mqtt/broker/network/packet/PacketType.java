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
    /**
     * The DISCONNECT packet is the final MQTT Control Packet sent from the Client or the Server. It
     * indicates the reason why the Network Connection is being closed. The Client or Server MAY send a
     * DISCONNECT packet before closing the Network Connection. If the Network Connection is closed
     * without the Client first sending a DISCONNECT packet with Reason Code 0x00 (Normal disconnection)
     * and the Connection has a Will Message, the Will Message is published. Refer to section 3.1.2.5 for
     * further details.
     */
    DISCONNECT,
    AUTHENTICATE
}
