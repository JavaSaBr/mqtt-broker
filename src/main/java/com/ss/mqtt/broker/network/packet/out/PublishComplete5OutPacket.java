package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.PublishCompletedReasonCode;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Publish complete (QoS 2 delivery part 3).
 */
public class PublishComplete5OutPacket extends PublishComplete311OutPacket {

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the UTF-8 Encoded String representing the reason associated with this response. This
          Reason String is human readable, designed for diagnostics and SHOULD NOT be parsed by the
          receiver.

          The sender uses this value to give additional information to the receiver. The sender MUST NOT send
          this Property if it would increase the size of the PUBREL packet beyond the Maximum Packet Size
          specified by the receiver [MQTT-3.6.2-2]. It is a Protocol Error to include the Reason String more than
          once.
         */
        PacketProperty.REASON_STRING,
        /*
          Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
          information for the PUBREL. The sender MUST NOT send this property if it would increase the size of the
          PUBREL packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.6.2-3]. The User
          Property is allowed to appear multiple times to represent multiple name, value pairs. The same name is
          allowed to appear more than once
         */
        PacketProperty.USER_PROPERTY
    );

    private final @NotNull Array<StringPair> userProperties;
    private final @NotNull PublishCompletedReasonCode reasonCode;
    private final @NotNull String reason;

    public PublishComplete5OutPacket(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishCompletedReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        super(client, packetId);
        this.reasonCode = reasonCode;
        this.userProperties = userProperties;
        this.reason = reason;
    }

    @Override
    public int getExpectedLength() {
        return -1;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        super.writeVariableHeader(buffer);

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901154
        writeByte(buffer, reasonCode.getValue());
    }

    @Override
    protected boolean isPropertiesSupported() {
        return true;
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {
        super.writeProperties(buffer);

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901155
        writeUserProperties(buffer, userProperties);
        writeNotEmptyProperty(buffer, PacketProperty.REASON_STRING, reason);
    }
}
