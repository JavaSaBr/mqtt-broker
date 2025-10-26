package javasabr.mqtt.network.message.in;

import static javasabr.mqtt.model.util.TopicUtils.buildTopicFilter;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Unsubscribe request.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnsubscribeMqttInMessage extends MqttInMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.UNSUBSCRIBE.ordinal();

  private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
        name is allowed to appear more than once.
       */
      PacketProperty.USER_PROPERTY);

  MutableArray<TopicFilter> topicFilters;
  int messageId;

  public UnsubscribeMqttInMessage(byte info) {
    super(info);
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    messageId = readShortUnsigned(buffer);
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {
    if (buffer.remaining() < 1) {
      throw new IllegalStateException("No any topic filters.");
    }

    topicFilters = ArrayFactory.mutableArray(TopicFilter.class);
    while (buffer.hasRemaining()) {
      topicFilters.add(buildTopicFilter(readString(buffer, Integer.MAX_VALUE)));
    }
  }

  @Override
  protected Set<PacketProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }
}
