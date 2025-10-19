package javasabr.mqtt.network.packet.in;

import static javasabr.mqtt.model.utils.TopicUtils.buildTopicFilter;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.PacketType;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.Getter;

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
      PacketProperty.USER_PROPERTY);

  private MutableArray<TopicFilter> topicFilters;
  private int packetId;

  public UnsubscribeInPacket(byte info) {
    super(info);
    this.topicFilters = ArrayFactory.mutableArray(TopicFilter.class);
  }

  @Override
  public byte packetType() {
    return PACKET_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    packetId = readShortUnsigned(buffer);
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {

    if (buffer.remaining() < 1) {
      throw new IllegalStateException("No any topic filters.");
    }

    while (buffer.hasRemaining()) {
      topicFilters.add(buildTopicFilter(readString(buffer, Integer.MAX_VALUE)));
    }
  }

  @Override
  protected Set<PacketProperty> getAvailableProperties() {
    return AVAILABLE_PROPERTIES;
  }
}
