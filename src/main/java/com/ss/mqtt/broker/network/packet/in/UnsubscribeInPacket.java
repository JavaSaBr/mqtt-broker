package com.ss.mqtt.broker.network.packet.in;

import static com.ss.mqtt.broker.util.TopicUtils.buildTopicFilter;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Unsubscribe request.
 */
@Getter
public class UnsubscribeInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.UNSUBSCRIBE.ordinal();

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
          name is allowed to appear more than once.
         */
        PacketProperty.USER_PROPERTY
    );

    private MutableArray<TopicFilter> topicFilters;
    private int packetId;

    public UnsubscribeInPacket(byte info) {
        super(info);
        this.topicFilters = ArrayFactory.mutableArray(TopicFilter.class);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
        packetId = readUnsignedShort(buffer);
    }

    @Override
    protected void readPayload(MqttConnection connection, ByteBuffer buffer) {

        if (buffer.remaining() < 1) {
            throw new IllegalStateException("No any topic filters.");
        }

        while (buffer.hasRemaining()) {
            topicFilters.add(buildTopicFilter(readString(buffer)));
        }
    }

    @Override
    protected Set<PacketProperty> getAvailableProperties() {
        return AVAILABLE_PROPERTIES;
    }
}
