package javasabr.mqtt.service.acl.builder;

import javasabr.mqtt.model.acl.value.matcher.TopicFilterValueMatcher;
import javasabr.mqtt.model.acl.value.matcher.TopicMatcher;
import javasabr.mqtt.model.acl.value.matcher.TopicNameValueMatcher;

interface TopicMatcherBuilder {

    default TopicMatcher<String> exact(String string) {
      return new TopicNameValueMatcher(string);
    }

    default TopicMatcher<String> match(String string) {
      return new TopicFilterValueMatcher(string);
    }
  }
