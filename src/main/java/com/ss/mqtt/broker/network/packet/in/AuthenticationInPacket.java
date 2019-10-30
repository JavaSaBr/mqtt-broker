package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.AuthenticateReasonCode;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Authentication exchange.
 */
@Getter
public class AuthenticationInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.AUTHENTICATE.ordinal();

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by a UTF-8 Encoded String containing the name of the authentication method. It is a Protocol
          Error to omit the Authentication Method or to include it more than once. Refer to section 4.12 for more
          information about extended authentication.
         */
        PacketProperty.AUTHENTICATION_METHOD,
        /*
          Followed by Binary Data containing authentication data. It is a Protocol Error to include Authentication
          Data more than once. The contents of this data are defined by the authentication method. Refer to
          section 4.12 for more information about extended authentication.
         */
        PacketProperty.AUTHENTICATION_DATA,
        /*
          Followed by the UTF-8 Encoded String representing the reason for the disconnect. This Reason String is
          human readable, designed for diagnostics and SHOULD NOT be parsed by the receiver.

          The sender MUST NOT send this property if it would increase the size of the AUTH packet beyond the
          Maximum Packet Size specified by the receiver [MQTT-3.15.2-2]. It is a Protocol Error to include the
          Reason String more than once.
         */
        PacketProperty.REASON_STRING,
        /*
          Followed by UTF-8 String Pair. This property may be used to provide additional diagnostic or other
          information. The sender MUST NOT send this property if it would increase the size of the AUTH packet
          beyond the Maximum Packet Size specified by the receiver [MQTT-3.15.2-3]. The User Property is
          allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to
          appear more than once.
         */
        PacketProperty.USER_PROPERTY
    );

    private @NotNull AuthenticateReasonCode reasonCode;

    // properties
    private @NotNull String reason;
    private @NotNull String authenticationMethod;

    private @NotNull byte[] authenticationData;

    public AuthenticationInPacket(byte info) {
        super(info);
        this.reasonCode = AuthenticateReasonCode.SUCCESS;
        this.reason = StringUtils.EMPTY;
        this.authenticationMethod = StringUtils.EMPTY;
        this.authenticationData = ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void readVariableHeader(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901219
        reasonCode = AuthenticateReasonCode.of(readUnsignedByte(buffer));
    }

    @Override
    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return AVAILABLE_PROPERTIES;
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull byte[] value) {
        switch (property) {
            case AUTHENTICATION_DATA:
                authenticationData = value;
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
            case AUTHENTICATION_METHOD:
                authenticationMethod = value;
                break;
            default:
                unexpectedProperty(property);
        }
    }
}
