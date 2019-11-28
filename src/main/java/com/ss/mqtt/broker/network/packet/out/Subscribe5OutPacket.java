package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.data.type.StringPair;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Subscribe request.
 */
public class Subscribe5OutPacket extends Subscribe311OutPacket {

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

    // properties
    private final @NotNull Array<StringPair> userProperties;
    private final int subscriptionId;

    public Subscribe5OutPacket(@NotNull Array<SubscribeTopicFilter> topicFilters, int packetId) {
        this(topicFilters, packetId, Array.empty(), MqttPropertyConstants.SUBSCRIPTION_ID_UNDEFINED);
    }

    public Subscribe5OutPacket(
        @NotNull Array<SubscribeTopicFilter> topicFilters,
        int packetId,
        @NotNull Array<StringPair> userProperties,
        int subscriptionId
    ) {
        super(topicFilters, packetId);
        this.userProperties = userProperties;
        this.subscriptionId = subscriptionId;
    }

    protected int buildSubscriptionOptions(@NotNull SubscribeTopicFilter topicFilter) {

        var subscriptionOptions = 0;

        subscriptionOptions |= topicFilter.getRetainHandling().ordinal() << 4;

        if (topicFilter.isRetainAsPublished()) {
            subscriptionOptions |= 0b0000_1000;
        }

        if (topicFilter.isNoLocal()) {
            subscriptionOptions |= 0b0000_0100;
        }

        subscriptionOptions |= topicFilter.getQos().ordinal();

        return subscriptionOptions;
    }

    @Override
    protected boolean isPropertiesSupported() {
        return true;
    }

    @Override
    protected void writeProperties(@NotNull ByteBuffer buffer) {
        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901164
        writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
        writeProperty(
            buffer,
            PacketProperty.SUBSCRIPTION_IDENTIFIER,
            subscriptionId,
            MqttPropertyConstants.SUBSCRIPTION_ID_UNDEFINED
        );
    }
}
