package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.reason.code.DisconnectReasonCode;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.mqtt.broker.util.DebugUtils;
import com.ss.rlib.common.util.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Disconnect notification.
 */
@Getter
public class DisconnectInPacket extends MqttReadablePacket {

    public static final byte PACKET_TYPE = (byte) PacketType.DISCONNECT.ordinal();

    static {
        DebugUtils.registerIncludedFields("reasonCode");
    }

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

    private @NotNull DisconnectReasonCode reasonCode;

    // properties
    private @NotNull String reason;
    private @NotNull String serverReference;

    private long sessionExpiryInterval;

    public DisconnectInPacket(byte info) {
        super(info);
        this.reasonCode = DisconnectReasonCode.NORMAL_DISCONNECTION;
        this.reason = StringUtils.EMPTY;
        this.serverReference = StringUtils.EMPTY;
        this.sessionExpiryInterval = MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT;
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void readImpl(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        this.sessionExpiryInterval = connection.getClient().getSessionExpiryInterval();
        super.readImpl(connection, buffer);
    }

    @Override
    protected void readVariableHeader(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901207
        if (connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining()) {
            reasonCode = DisconnectReasonCode.of(readUnsignedByte(buffer));
        }
    }

    @Override
    protected boolean isPropertiesSupported(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        return connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining();
    }

    @Override
    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return AVAILABLE_PROPERTIES;
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, long value) {
        switch (property) {
            case SESSION_EXPIRY_INTERVAL:
                sessionExpiryInterval = value;
                break;
            default:
                unexpectedProperty(property);
        }
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull String value) {
        switch (property) {
            case REASON_STRING:
                reason = value;
                break;
            case SERVER_REFERENCE:
                serverReference = value;
                break;
            default:
                unexpectedProperty(property);
        }
    }
}
