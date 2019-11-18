package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.data.type.StringPair;
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Unsubscribe acknowledgement.
 */
public class UnsubscribeAck5OutPacket extends UnsubscribeAck311OutPacket {

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the UTF-8 Encoded String representing the reason associated with this response. This
          Reason String is a human readable string designed for diagnostics and SHOULD NOT be parsed by the
          Client.

          The Server uses this value to give additional information to the Client. The Server MUST NOT send this
          Property if it would increase the size of the UNSUBACK packet beyond the Maximum Packet Size
          specified by the Client [MQTT-3.11.2-1]. It is a Protocol Error to include the Reason String more than
          once.
         */
        PacketProperty.REASON_STRING,
        /*
          Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
          information. The Server MUST NOT send this property if it would increase the size of the UNSUBACK
          packet beyond the Maximum Packet Size specified by the Client [MQTT-3.11.2-2]. The User Property is
          allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to
          appear more than once.
         */
        PacketProperty.USER_PROPERTY
    );

    private final @NotNull Array<UnsubscribeAckReasonCode> reasonCodes;
    private final @NotNull Array<StringPair> userProperties;
    private final @NotNull String reason;

    public UnsubscribeAck5OutPacket(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<UnsubscribeAckReasonCode> reasonCodes,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        super(client, packetId);
        this.reasonCodes = reasonCodes;
        this.userProperties = userProperties;
        this.reason = reason;
    }

    @Override
    protected boolean isPropertiesSupported() {
        return true;
    }

    @Override
    public int getExpectedLength() {
        return -1;
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901182
        writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
        writeNotEmptyProperty(
            buffer,
            PacketProperty.REASON_STRING,
            reason
        );
    }

    @Override
    protected void writePayload(@NotNull ByteBuffer buffer) {
        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901185
        for (var reasonCode : reasonCodes) {
            writeByte(buffer, reasonCode.getValue());
        }
    }
}
