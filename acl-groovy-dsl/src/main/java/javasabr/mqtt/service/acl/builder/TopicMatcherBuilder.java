//file:noinspection unused
package javasabr.mqtt.service.acl.builder;

import javasabr.mqtt.model.acl.condition.TopicCondition;
import javasabr.mqtt.model.acl.matcher.TopicFilterMatcher;
import javasabr.mqtt.model.acl.matcher.TopicNameMatcher;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;

public interface TopicMatcherBuilder {

  default TopicNameMatcher eq(String string) {
    return new TopicNameMatcher(TopicName.valueOf(string));
  }

  default ValueMatcher<AbstractTopic> match(String string) {
    return new TopicFilterMatcher(TopicFilter.valueOf(string));
  }

  default TopicCondition topicCondition(String value) {
    return new TopicCondition(Array.of(eq(value)));
  }

}
