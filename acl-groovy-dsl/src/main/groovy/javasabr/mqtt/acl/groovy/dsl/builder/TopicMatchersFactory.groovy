//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.matcher.AnyTopicMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.model.topic.TopicValidator

class TopicMatchersFactory {

  TopicNameMatcher eq(String rawTopicName) {
    if (!TopicValidator.validateTopicName(rawTopicName)) {
      throw new AclConfigurationException("Invalid topic name:[$rawTopicName]")
    }
    return new TopicNameMatcher(TopicName.valueOf(rawTopicName))
  }

  TopicFilterMatcher match(String rawTopicFilter) {
    if (!TopicValidator.validateTopicFilter(rawTopicFilter)) {
      throw new AclConfigurationException("Invalid topic filter:[$rawTopicFilter]")
    }
    return new TopicFilterMatcher(TopicFilter.valueOf(rawTopicFilter))
  }

  AnyTopicMatcher anyTopic() {
    return AnyTopicMatcher.instance()
  }
}
