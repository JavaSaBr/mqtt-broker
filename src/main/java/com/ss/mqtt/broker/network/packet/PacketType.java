package com.ss.mqtt.broker.network.packet;

public enum PacketType {
    RESERVED,
    /**
     * After a Network Connection is established by a Client to a Server, the first Packet sent from
     * the Client to the Server MUST be a CONNECT Packet
     */
    CONNECT,
    /**
     * The CONNACK Packet is the packet sent by the Server in response to a CONNECT Packet received from a Client.
     * The first packet sent from the Server to the Client MUST be a CONNACK Packet [MQTT-3.2.0-1].
     */
    CONNECT_ACK,
    /**
     * A PUBLISH Control Packet is sent from a Client to a Server or from Server to a Client to
     * transport an Application Message.
     */
    PUBLISH,
    /**
     * A PUBACK Packet is the response to a PUBLISH Packet with QoS level 1.
     */
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
    /**
     * The PUBCOMP packet is the response to a PUBREL packet. It is the fourth and final packet of
     * the QoS 2 protocol exchange.
     */
    PUBLISH_COMPLETED,
    /**
     * The SUBSCRIBE Packet is sent from the Client to the Server to create one or more Subscriptions.
     * Each Subscription registers a Client’s interest in one or more Topics. The Server sends PUBLISH Packets
     * to the Client in order to forward Application Messages that were published to Topics that match
     * these Subscriptions. The SUBSCRIBE Packet also specifies (for each Subscription) the maximum QoS with
     * which the Server can send Application Messages to the Client.
     */
    SUBSCRIBE,
    /**
     * A SUBACK Packet is sent by the Server to the Client to confirm receipt and processing of a SUBSCRIBE Packet.
     */
    SUBSCRIBE_ACK,
    /**
     * An UNSUBSCRIBE Packet is sent by the Client to the Server, to unsubscribe from topics.
     */
    UNSUBSCRIBE,
    /**
     * The UNSUBACK Packet is sent by the Server to the Client to confirm receipt of an UNSUBSCRIBE Packet.
     */
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
    AUTHENTICATE
}
