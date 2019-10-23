package com.ss.mqtt.broker.network.packet;

public enum PacketType {
    RESERVED,
    CONNECT_REQUEST,
    CONNECT_ACK,
    PUBLISH,
    PUBLISH_ACK,
    /**
     * A PUBREC Packet is the response to a PUBLISH Packet with QoS 2. It is the second packet of the QoS 2
     * protocol exchange.
     */
    PUBLISH_RECEIVED,
    /**
     * A PUBREL Packet is the response to a PUBREC Packet. It is the third packet of the QoS 2 protocol exchange.
     */
    PUBLISH_RELEASED,
    PUBLISH_COMPLETED,
    SUBSCRIBE,
    SUBSCRIBE_ACK,
    UNSUBSCRIBE,
    UNSUBSCRIBE_ACK,
    /**
     * The PINGREQ packet is sent from a Client to the Server. It can be used to:
     * <p>
     * • Indicate to the Server that the Client is alive in the absence of any other MQTT Control Packets being
     * sent from the Client to the Server.
     * <p>
     * • Request that the Server responds to confirm that it is alive.
     * <p>
     * • Exercise the network to indicate that the Network Connection is active.
     * <p>
     * This packet is used in Keep Alive processing. Refer to section 3.1.2.10 for more details
     */
    PING_REQUEST,
    /**
     * A PINGRESP Packet is sent by the Server to the Client in response to a PINGREQ packet. It indicates
     * that the Server is alive.
     */
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
    /**
     * An AUTH packet is sent from Client to Server or Server to Client as part of an extended authentication
     * exchange, such as challenge / response authentication. It is a Protocol Error for the Client or Server to
     * send an AUTH packet if the CONNECT packet did not contain the same Authentication Method.
     */
    AUTHENTICATE;

    private static final PacketType[] VALUES = values();

    public static PacketType fromByte(byte packetType) {
        return VALUES[packetType];
    }
}
