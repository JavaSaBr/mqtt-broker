package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Connect acknowledgment.
 */
public class ConnectAck311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.CONNECT_ACK.ordinal();

    /**
     * The values the Connect Reason Code are shown below. If a well formed CONNECT packet is received
     * by the Server, but the Server is unable to complete the Connection the Server MAY send a CONNACK
     * packet containing the appropriate Connect Reason code from this table. If a Server sends a CONNACK
     * packet containing a Reason code of 128 or greater it MUST then close the Network Connection
     */
    protected final @NotNull ConnectAckReasonCode reasonCode;

    /**
     * The Session Present flag informs the Client whether the Server is using Session State from a
     * previous connection for this ClientID.
     * This allows the Client and Server to have a consistent view of the Session State.
     * If the Server accepts a connection with Clean Start set to 1, the Server MUST set Session
     * Present to 0 in the CONNACK packet in addition to setting a 0x00 (Success) Reason Code in the CONNACK packet
     */
    private final boolean sessionPresent;

    public ConnectAck311OutPacket(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode,
        boolean sessionPresent
    ) {
        super(client);
        this.reasonCode = reasonCode;
        this.sessionPresent = sessionPresent;
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    public int getExpectedLength() {
        return 2;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718035
        buffer.put((byte) (sessionPresent ? 0x01 : 0x00));
        buffer.put(getReasonCodeValue());
    }

    protected byte getReasonCodeValue() {
        return reasonCode.getMqtt311();
    }
}
