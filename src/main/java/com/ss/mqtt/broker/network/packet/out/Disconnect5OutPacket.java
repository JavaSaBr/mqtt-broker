package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.DisconnectReasonCode;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Disconnect notification.
 */
public class Disconnect5OutPacket extends Disconnect311OutPacket {

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          If the Session Expiry Interval is absent, the Session Expiry Interval in the CONNECT packet is used.

          The Session Expiry Interval MUST NOT be sent on a DISCONNECT by the Server [MQTT-3.14.2-2].

          If the Session Expiry Interval in the CONNECT packet was zero, then it is a Protocol Error to set a non
          zero Session Expiry Interval in the DISCONNECT packet sent by the Client. If such a non-zero Session
          Expiry Interval is received by the Server, it does not treat it as a valid DISCONNECT packet. The Server
          uses DISCONNECT with Reason Code 0x82 (Protocol Error) as described in
         */
        PacketProperty.SESSION_EXPIRY_INTERVAL,
        /*
          The sender MUST NOT send this Property if it would increase the size of the DISCONNECT packet
          beyond the Maximum Packet Size specified by the receiver [MQTT-3.14.2-3]. It is a Protocol Error to
          include the Reason String more than once.
         */
        PacketProperty.REASON_STRING,
        /*
          Followed by UTF-8 String Pair. This property may be used to provide additional diagnostic or other
          information. The sender MUST NOT send this property if it would increase the size of the DISCONNECT
          packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.14.2-4]. The User Property is
          allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to
          appear more than once.
         */
        PacketProperty.USER_PROPERTY,
        /*
          The Server sends DISCONNECT including a Server Reference and Reason Code {0x9C (Use another
          2601 server)} or 0x9D (Server moved) as described in section 4.13.
         */
        PacketProperty.SERVER_REFERENCE
    );

    private final @NotNull DisconnectReasonCode reasonCode;
    private final @NotNull Array<StringPair> userProperties;

    private final @Nullable String reason;
    private final @Nullable String serverReference;

    public Disconnect5OutPacket(
        @NotNull MqttClient client,
        @NotNull DisconnectReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @Nullable String reason,
        @Nullable String serverReference
    ) {
        super(client);
        this.reasonCode = reasonCode;
        this.userProperties = userProperties;
        this.reason = reason;
        this.serverReference = serverReference;
    }

    @Override
    public int getExpectedLength() {
        return -1;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
       writeByte(buffer, reasonCode.getValue());
    }

    @Override
    protected boolean isPropertiesSupported() {
        return true;
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {

        writeProperty(
            buffer,
            PacketProperty.SESSION_EXPIRY_INTERVAL,
            client.getSessionExpiryInterval(),
            MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT
        );

        writeNullableProperty(
            buffer,
            PacketProperty.REASON_STRING,
            reason
        );

        writeNullableProperty(
            buffer,
            PacketProperty.SERVER_REFERENCE,
            serverReference
        );

        writeStringPairProperties(
            buffer,
            PacketProperty.USER_PROPERTY,
            userProperties
        );
    }
}
