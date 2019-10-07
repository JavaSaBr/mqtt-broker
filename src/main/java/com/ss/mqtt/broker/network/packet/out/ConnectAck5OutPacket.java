package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.ConnectReasonCode;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Connect acknowledgment.
 */
public class ConnectAck5OutPacket extends ConnectAck311OutPacket {

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        PacketProperty.SESSION_EXPIRY_INTERVAL,
        PacketProperty.RECEIVE_MAXIMUM,
        PacketProperty.MAXIMUM_QOS,
        PacketProperty.RETAIN_AVAILABLE,
        PacketProperty.MAXIMUM_PACKET_SIZE,
        PacketProperty.ASSIGNED_CLIENT_IDENTIFIER,
        PacketProperty.TOPIC_ALIAS,
        PacketProperty.REASON_STRING,
        PacketProperty.USER_PROPERTY,
        PacketProperty.WILDCARD_SUBSCRIPTION_AVAILABLE,
        PacketProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE,
        PacketProperty.SHARED_SUBSCRIPTION_AVAILABLE,
        PacketProperty.SERVER_KEEP_ALIVE,
        PacketProperty.RESPONSE_INFORMATION,
        PacketProperty.SERVER_REFERENCE,
        PacketProperty.AUTHENTICATION_METHOD,
        PacketProperty.AUTHENTICATION_DATA
    );

    public ConnectAck5OutPacket(
        @NotNull MqttClient client,
        @NotNull ConnectReasonCode reasonCode,
        boolean sessionPresent
    ) {
        super(client, reasonCode, sessionPresent);
    }

    @Override
    protected void writeImpl(@NotNull ByteBuffer buffer) {
        super.writeImpl(buffer);


    }
}
