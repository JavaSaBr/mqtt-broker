//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.matcher.TopicFilterMatcher
import javasabr.mqtt.model.acl.matcher.TopicMatcher
import javasabr.mqtt.model.acl.matcher.TopicNameMatcher

interface TopicMatcherBuilder {

  default TopicMatcher<String> exact(String string) {
    return new TopicNameMatcher(string);
  }

  default TopicMatcher<String> match(String string) {
    return new TopicFilterMatcher(string);
  }
}
