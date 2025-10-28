package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Subscribe request.
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class SubscribeMqtt311OutMessage extends TrackableMqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.SUBSCRIBE.ordinal();

  Array<Subscription> subscriptions;

  public SubscribeMqtt311OutMessage(int messageId, Array<Subscription> subscriptions) {
    super(messageId);
    this.subscriptions = subscriptions;
  }

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void writePayload(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718066
    for (Subscription subscribedTopic : subscriptions) {
      TopicFilter topicFilter = subscribedTopic.topicFilter();
      writeString(buffer, topicFilter.rawTopic());
      writeByte(buffer, buildSubscriptionOptions(subscribedTopic));
    }
  }

  protected int buildSubscriptionOptions(Subscription topicFilter) {
    return topicFilter.qos().index();
  }
}
