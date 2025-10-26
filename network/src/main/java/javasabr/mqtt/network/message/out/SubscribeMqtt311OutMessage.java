package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Subscribe request.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class SubscribeMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.SUBSCRIBE.ordinal();

  Array<SubscribeTopicFilter> topicFilters;
  int messageId;

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718065
    writeShort(buffer, messageId);
  }

  @Override
  protected void writePayload(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718066
    for (SubscribeTopicFilter subscribedTopic : topicFilters) {
      TopicFilter topicFilter = subscribedTopic.getTopicFilter();
      writeString(buffer, topicFilter.toString());
      writeByte(buffer, buildSubscriptionOptions(subscribedTopic));
    }
  }

  protected int buildSubscriptionOptions(SubscribeTopicFilter topicFilter) {
    return topicFilter.getQos().ordinal();
  }
}
