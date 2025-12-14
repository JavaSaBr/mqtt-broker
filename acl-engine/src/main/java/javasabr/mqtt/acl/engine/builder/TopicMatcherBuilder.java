//file:noinspection unused
package javasabr.mqtt.acl.engine.builder;

import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
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
