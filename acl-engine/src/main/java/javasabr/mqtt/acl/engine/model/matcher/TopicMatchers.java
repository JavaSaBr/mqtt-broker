package javasabr.mqtt.acl.engine.model.matcher;

import javasabr.mqtt.acl.engine.model.matcher.dynamic.DynamicTopicMatcher;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;

public class TopicMatchers {

  public static TopicMatcher eq(String rawTopicName) {
    return new TopicNameMatcher(TopicName.valueOf(rawTopicName));
  }

  public static TopicMatcher match(String rawTopicFilter) {
    return new TopicFilterMatcher(TopicFilter.valueOf(rawTopicFilter));
  }

  public static TopicMatcher dynamic(String rawTopic) {
    return DynamicTopicMatcher.autoBuild(rawTopic);
  }
}
