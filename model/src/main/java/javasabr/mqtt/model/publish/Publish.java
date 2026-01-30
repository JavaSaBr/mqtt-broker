package javasabr.mqtt.model.publish;

import java.util.UUID;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public interface Publish {
  int messageId();

  QoS qos();

  TopicName topicName();

  @Nullable 
  TopicName responseTopicName();

  UUID dataId();

  boolean duplicated();

  boolean retained();

  @Nullable 
  String contentType();

  IntArray subscriptionIds();

  byte @Nullable [] correlationData();

  long messageExpiryInterval();

  int topicAlias();

  PayloadFormat payloadFormat();

  Array<StringPair> userProperties();
}
