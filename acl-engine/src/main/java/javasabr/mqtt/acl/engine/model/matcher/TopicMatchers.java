package javasabr.mqtt.acl.engine.model.matcher;

import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;

public class TopicMatchers {

  public static TopicMatcher<AbstractTopic> eq(String string) {
    return new TopicNameMatcher(TopicName.valueOf(string));
  }

  public static TopicMatcher<AbstractTopic> match(String string) {
    return new TopicFilterMatcher(TopicFilter.valueOf(string));
  }
}
