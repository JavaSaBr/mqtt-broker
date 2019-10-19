package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.model.TopicFilter;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Subscribe request.
 */
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

    private @Nullable Array<StringPair> userProperties;
    private @NotNull Array<TopicFilter> topicFilters;

    private int packetId;
    private int subscriptionId;

    public SubscribeInPacket(byte info) {
        super(info);
        this.topicFilters = ArrayFactory.newArray(TopicFilter.class);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void readImpl(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        super.readImpl(connection, buffer);

        packetId = readUnsignedShort(buffer);

        var client = connection.getClient();

        if (client.getMqttVersion().ordinal() >= MqttVersion.MQTT_5.ordinal()) {
            readProperties(buffer);
        }

        if (buffer.remaining() < 1) {
            throw new IllegalStateException("No any topic filters.");
        }

        while (buffer.hasRemaining()) {

            var topicFilter = readString(buffer);
            var options = readUnsignedByte(buffer);

            var noLocal = NumberUtils.isSetBit(options, 3);
        }
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
}
