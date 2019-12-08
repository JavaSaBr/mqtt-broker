package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.array.Array;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Subscribe request.
 */
@RequiredArgsConstructor
public class Subscribe311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.SUBSCRIBE.ordinal();

    private final @NotNull Array<SubscribeTopicFilter> topicFilters;
    private final int packetId;

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718065
        writeShort(buffer, packetId);
    }

    @Override
    protected void writePayload(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718066
        for (var topicFilter : topicFilters) {
            writeString(buffer, topicFilter.getTopicFilter().toString());
            writeByte(buffer, buildSubscriptionOptions(topicFilter));
        }
    }

    protected int buildSubscriptionOptions(@NotNull SubscribeTopicFilter topicFilter) {
        return topicFilter.getQos().ordinal();
    }
}
