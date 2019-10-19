package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Subscribe acknowledgement.
 */
public class SubscribeAck5OutPacket extends SubscribeAck311OutPacket {

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the UTF-8 Encoded String representing the reason associated with this response. This
          Reason String is a human readable string designed for diagnostics and SHOULD NOT be parsed by the
          Client.

          The Server uses this value to give additional information to the Client. The Server MUST NOT send this
          Property if it would increase the size of the SUBACK packet beyond the Maximum Packet Size specified
          by the Client
         */
        PacketProperty.REASON_STRING,
        /*
          Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
          information. The Server MUST NOT send this property if it would increase the size of the SUBACK packet
          beyond the Maximum Packet Size specified by Client [MQTT-3.9.2-2]. The User Property is allowed to
          appear multiple times to represent multiple name, value pairs. The same name is allowed to appear more
          than once.
         */
        PacketProperty.USER_PROPERTY
    );

    private final @Nullable Array<StringPair> userProperties;
    private final @Nullable String reason;

    public SubscribeAck5OutPacket(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<SubscribeAckReasonCode> reasonCodes,
        @Nullable Array<StringPair> userProperties,
        @Nullable String reason
    ) {
        super(client, packetId, reasonCodes);
        this.userProperties = userProperties;
        this.reason = reason;
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901174
        writeNullableProperty(
            buffer,
            PacketProperty.REASON_STRING,
            reason
        );

        if (userProperties != null) {
            for (var property : userProperties) {
                writeProperty(buffer, PacketProperty.USER_PROPERTY, property);
            }
        }
    }
}