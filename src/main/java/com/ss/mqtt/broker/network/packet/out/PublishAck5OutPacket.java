package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

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

    private final @Nullable Array<StringPair> userProperties;
    private final @Nullable String reason;

    public PublishAck5OutPacket(
        @NotNull MqttClient client,
        @NotNull PublishAckReasonCode reasonCode,
        int packetId,
        @Nullable Array<StringPair> userProperties,
        @Nullable String reason
    ) {
        super(client, reasonCode, packetId);
        this.userProperties = userProperties;
        this.reason = reason;
    }

    @Override
    public int getExpectedLength() {
        return -1;
    }

    @Override
    protected void writeImpl(@NotNull ByteBuffer buffer) {
        super.writeImpl(buffer);

        var propertiesBuffer = getPropertiesBuffer();

        writeNullableProperty(
            propertiesBuffer,
            PacketProperty.REASON_STRING,
            reason
        );

        if (userProperties != null) {
            for (var property : userProperties) {
                writeProperty(propertiesBuffer, PacketProperty.USER_PROPERTY, property);
            }
        }

        writeProperties(buffer, propertiesBuffer);
    }
}
