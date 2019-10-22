package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Subscribe request.
 */
@Getter
public class SubscribeInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.SUBSCRIBE.ordinal();

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by a Variable Byte Integer representing the identifier of the subscription. The Subscription
          Identifier can have the value of 1 to 268,435,455. It is a Protocol Error if the Subscription Identifier has a
          value of 0. It is a Protocol Error to include the Subscription Identifier more than once.

          The Subscription Identifier is associated with any subscription created or modified as the result of this
          SUBSCRIBE packet. If there is a Subscription Identifier, it is stored with the subscription. If this property is
          not specified, then the absence of a Subscription Identifier is stored with the subscription.
         */
        PacketProperty.SUBSCRIPTION_IDENTIFIER,
        /*
          The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
          name is allowed to appear more than once.
         */
        PacketProperty.USER_PROPERTY
    );

    private @NotNull Array<SubscribeTopicFilter> topicFilters;

    private int packetId;
    private int subscriptionId;

    public SubscribeInPacket(byte info) {
        super(info);
        this.topicFilters = ArrayFactory.newArray(SubscribeTopicFilter.class);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void readVariableHeader(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        packetId = readUnsignedShort(buffer);
    }

    @Override
    protected void readPayload(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {

        if (buffer.remaining() < 1) {
            throw new IllegalStateException("No any topic filters.");
        }

        while (buffer.hasRemaining()) {

            var topicFilter = readString(buffer);
            var options = readUnsignedByte(buffer);

            var qos = QoS.of(options & 0x03);
            var retainHandling = SubscribeRetainHandling.of((options >> 4) & 0x03);

            if (qos == QoS.INVALID || retainHandling == SubscribeRetainHandling.INVALID) {
                throw new IllegalStateException("Unsupported qos or retain handling");
            }

            var noLocal = NumberUtils.isSetBit(options, 2);
            var rap = NumberUtils.isSetBit(options, 3);

            topicFilters.add(new SubscribeTopicFilter(topicFilter, qos, retainHandling, noLocal, rap));
        }
    }

    @Override
    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return AVAILABLE_PROPERTIES;
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, long value) {
        switch (property) {
            case SUBSCRIPTION_IDENTIFIER:
                subscriptionId = (int) value;
                break;
            default:
                unexpectedProperty(property);
        }
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull StringPair value) {
        switch (property) {
            case USER_PROPERTY:
                if (userProperties == null) {
                    userProperties = ArrayFactory.newArray(StringPair.class);
                }
                userProperties.add(value);
                break;
            default:
                unexpectedProperty(property);
        }
    }

    public @NotNull Array<StringPair> getUserProperties() {
        return ObjectUtils.ifNull(userProperties, Array.empty());
    }

}
