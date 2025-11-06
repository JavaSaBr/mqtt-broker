package javasabr.mqtt.model.topic.tree;

import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.subscribtion.SubscriptionOwner;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.ThreadSafe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcurrentTopicTree implements ThreadSafe {

  TopicNode rootNode;

  public ConcurrentTopicTree() {
    this.rootNode = new TopicNode();
  }

  public void subscribe(SubscriptionOwner owner, Subscription subscription) {
    rootNode.subscribe(0, owner, subscription, subscription.topicFilter());
  }

  public boolean unsubscribe(SubscriptionOwner owner, TopicFilter topicFilter) {
    return rootNode.unsubscribe(0, owner, topicFilter);
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
