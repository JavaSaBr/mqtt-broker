package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;

public class DynamicTopicNameMatcher extends DynamicTopicMatcher<TopicName> {
  
  public DynamicTopicNameMatcher(TopicName originalTopicName) {
    super(originalTopicName);
  }

  @Override
  public boolean test(MqttUser user, AbstractTopic topic) {
    if (topic.levelsCount() != resolvers.length) {
      return false;
    } else if (topic instanceof TopicFilter topicFilter && topicFilter.wildcard()) {
      return false;
    }
    return super.test(user, topic);
  }

  @Override
  protected TopicName constructTopic(String[] segments, String rawTopicName) {
    return new TopicName(segments, rawTopicName);
  }
}
