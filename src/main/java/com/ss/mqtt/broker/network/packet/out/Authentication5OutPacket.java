package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.AuthenticateReasonCode;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Authentication exchange.
 */
public class Authentication5OutPacket extends MqttWritablePacket {

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

    private @NotNull Array<StringPair> userProperties;

    private @NotNull AuthenticateReasonCode reasonCode;

    private @NotNull String reason;
    private @NotNull String authenticateMethod;

    private @NotNull byte[] authenticateData;

    public Authentication5OutPacket(
        @NotNull MqttClient client,
        @NotNull AuthenticateReasonCode reasonCode,
        @NotNull String authenticateMethod,
        @NotNull byte[] authenticateData,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        super(client);
        this.userProperties = userProperties;
        this.reasonCode = reasonCode;
        this.reason = reason;
        this.authenticateMethod = authenticateMethod;
        this.authenticateData = authenticateData;
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901219
        writeByte(buffer, reasonCode.getValue());
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901221
        writeStringPairProperties(
            buffer,
            PacketProperty.USER_PROPERTY,
            userProperties
        );
        writeNotEmptyProperty(
            buffer,
            PacketProperty.REASON_STRING,
            reason
        );
        writeNotEmptyProperty(
            buffer,
            PacketProperty.AUTHENTICATION_METHOD,
            authenticateMethod
        );
        writeNotEmptyProperty(
            buffer,
            PacketProperty.AUTHENTICATION_DATA,
            authenticateData
        );
    }
}
