package javasabr.mqtt.model.publish;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.UUID;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public record SimpleIncomingPublish(
    UUID id,
    int messageId,
    QoS qos,
    TopicName topicName,
    @Nullable
    TopicName responseTopicName,
    PublishData data,
    boolean duplicated,
    boolean retained,
    IntArray subscriptionIds,
    long messageExpiryInterval,
    int topicAlias,
    Array<StringPair> userProperties) implements IncomingPublish {

  static {
    DebugUtils.registerIncludedFields("id", "messageId", "topicName", "data");
  }
  
  @Override
  public IncomingPublish withDuplicated() {
    if (duplicated()) {
      return this;
    } else {
      return new SimpleIncomingPublish(
          id,
          messageId,
          qos,
          topicName,
          responseTopicName,
          data,
          true,
          retained,
          subscriptionIds,
          messageExpiryInterval,
          topicAlias,
          userProperties);
    }
  }

  @Override
  public IncomingPublish withoutRetained() {
    if (!retained()) {
      return this;
    } else {
      return new SimpleIncomingPublish(
          id,
          messageId,
          qos,
          topicName,
          responseTopicName,
          data,
          duplicated,
          false,
          subscriptionIds,
          messageExpiryInterval,
          topicAlias,
          userProperties);
    }
  }

  @JsonValue
  public Object jsonDebugValue() {
    return Map.of(
        "id", id,
        "messageId", messageId,
        "topicName", topicName,
        "data", data);
  }
  
  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
