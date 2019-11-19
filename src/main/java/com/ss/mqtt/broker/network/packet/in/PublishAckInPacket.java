package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.reason.code.PublishAckReasonCode;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Publish acknowledgment (QoS 1).
 */
@Getter
public class PublishAckInPacket extends MqttReadablePacket implements HasPacketId {

    private static final int PACKET_TYPE = PacketType.PUBLISH_ACK.ordinal();

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

    private @NotNull PublishAckReasonCode reasonCode;
    private int packetId;

    // properties
    private @NotNull String reason;

    public PublishAckInPacket(byte info) {
        super(info);
        this.reasonCode = PublishAckReasonCode.SUCCESS;
        this.reason = StringUtils.EMPTY;
    }

    @Override
    public byte getPacketType() {
        return (byte) PACKET_TYPE;
    }

    @Override
    protected void readVariableHeader(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {

        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718045
        packetId = readUnsignedShort(buffer);

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901123
        if (connection.isSupported(MqttVersion.MQTT_5)) {
            reasonCode = PublishAckReasonCode.of(readUnsignedByte(buffer));
        }
    }

    @Override
    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return AVAILABLE_PROPERTIES;
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull String value) {
        switch (property) {
            case REASON_STRING:
                reason = value;
                break;
            default:
                unexpectedProperty(property);
        }
    }
}
