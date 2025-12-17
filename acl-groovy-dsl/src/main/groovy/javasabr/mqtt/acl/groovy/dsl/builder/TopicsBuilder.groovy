package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.mqtt.model.topic.AbstractTopic
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.model.topic.TopicValidator
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

class TopicsBuilder {

  MutableArray<ValueMatcher<AbstractTopic>> topicMatchers = MutableArray.ofType(ValueMatcher)

  TopicsBuilder eq(String rawTopicName) {
    if (!TopicValidator.validateTopicName(rawTopicName)) {
      throw new IllegalArgumentException("Invalid topic name:[$rawTopicName]")
    }
    topicMatchers.add(new TopicNameMatcher(TopicName.valueOf(rawTopicName)))
    return this
  }

  TopicsBuilder match(String rawTopicFilter) {
    if (!TopicValidator.validateTopicFilter(rawTopicFilter)) {
      throw new IllegalArgumentException("Invalid topic filter:[$rawTopicFilter]")
    }
    topicMatchers.add(new TopicFilterMatcher(TopicFilter.valueOf(rawTopicFilter)))
    return this
  }

  TopicsBuilder configure(Closure<?> config) {
    config.delegate = this
    config.resolveStrategy = Closure.DELEGATE_ONLY
    config()
    return this
  }
  
  Array<ValueMatcher<AbstractTopic>> build() {
    return Array.copyOf(topicMatchers)
  }
}
