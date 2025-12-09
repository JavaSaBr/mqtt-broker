package javasabr.mqtt.model.subscriber.tree;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.ThreadSafe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcurrentSubscriberTree implements ThreadSafe {

  SubscriberNode rootNode;

  public ConcurrentSubscriberTree() {
    this.rootNode = new SubscriberNode();
  }

  @Nullable
  public SingleSubscriber subscribe(SingleSubscriber subscriber) {
    return rootNode.subscribe(0, subscriber, subscriber.subscription().topicFilter());
  }

  public boolean unsubscribe(MqttUser user, TopicFilter topicFilter) {
    return rootNode.unsubscribe(0, user, topicFilter);
  }

  public Array<SingleSubscriber> matches(TopicName topicName) {
    var resultArray = MutableArray.ofType(SingleSubscriber.class);
    matchesTo(resultArray, topicName);
    return resultArray;
  }

  public MutableArray<SingleSubscriber> matchesTo(MutableArray<SingleSubscriber> container, TopicName topicName) {
    var resultArray = MutableArray.ofType(SingleSubscriber.class);
    rootNode.matchesTo(0, topicName, topicName.levelsCount() - 1, container);
    return resultArray;
  }
}
