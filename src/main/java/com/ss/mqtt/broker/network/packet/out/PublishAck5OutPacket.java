package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.reason.code.PublishAckReasonCode;
import com.ss.mqtt.broker.model.data.type.StringPair;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Publish acknowledgement.
 */
public class PublishAck5OutPacket extends PublishAck311OutPacket {

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the UTF-8 Encoded String representing the reason associated with this response. This
          Reason String is a human readable string designed for diagnostics and is not intended to be parsed by
          the receiver.

          The sender uses this value to give additional information to the receiver. The sender MUST NOT send
          this property if it would increase the size of the PUBACK packet beyond the Maximum Packet Size
          specified by the receiver [MQTT-3.4.2-2]. It is a Protocol Error to include the Reason String more than
          once.
         */
        PacketProperty.REASON_STRING,
        /*
          Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
          information. The sender MUST NOT send this property if it would increase the size of the PUBACK
          packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.4.2-3]. The User Property is
          allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to
          appear more than once.
         */
        PacketProperty.USER_PROPERTY
    );

    private final @NotNull Array<StringPair> userProperties;
    private final @NotNull String reason;
    private final @NotNull PublishAckReasonCode reasonCode;

    public PublishAck5OutPacket(
        int packetId,
        @NotNull PublishAckReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        super(packetId);
        this.reasonCode = reasonCode;
        this.userProperties = userProperties;
        this.reason = reason;
    }

    @Override
    public int getExpectedLength() {
        return -1;
    }

    @Override
    protected boolean isPropertiesSupported() {
        return true;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        super.writeVariableHeader(buffer);
        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901123
        writeByte(buffer, reasonCode.getValue());
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901125
        writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
        writeNotEmptyProperty(
            buffer,
            PacketProperty.REASON_STRING,
            reason
        );
    }
}
