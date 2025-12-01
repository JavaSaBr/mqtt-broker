//file:noinspection unused
package javasabr.mqtt.service.acl.builder;

import javasabr.mqtt.model.acl.matcher.TopicFilterMatcher;
import javasabr.mqtt.model.acl.matcher.TopicNameMatcher;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;

public interface TopicMatcherBuilder {

  default ValueMatcher<AbstractTopic> eq(String string) {
    return new TopicNameMatcher(TopicName.valueOf(string));
  }

  default ValueMatcher<AbstractTopic> match(String string) {
    return new TopicFilterMatcher(TopicFilter.valueOf(string));
  }
}
