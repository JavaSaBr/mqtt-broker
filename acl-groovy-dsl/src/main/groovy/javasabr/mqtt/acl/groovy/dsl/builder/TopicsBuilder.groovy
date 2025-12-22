//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.matcher.TopicMatcher
import javasabr.mqtt.model.topic.AbstractTopic
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

class TopicsBuilder {

  TopicMatchersFactory topicMatchersFactory = new TopicMatchersFactory()
  MutableArray<TopicMatcher<AbstractTopic>> topicMatchers = MutableArray.ofType(TopicMatcher)

  TopicsBuilder eq(String rawTopicName) {
    if (topicMatchers.contains(TopicMatcher.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any topic condition")
    }
    topicMatchers.add(topicMatchersFactory.eq(rawTopicName))
    return this
  }

  TopicsBuilder match(String rawTopicFilter) {
    if (topicMatchers.contains(TopicMatcher.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any topic condition")
    }
    topicMatchers.add(topicMatchersFactory.match(rawTopicFilter))
    return this
  }

  TopicsBuilder anyTopic() {
    if (topicMatchers.contains(TopicMatcher.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any topic condition")
    }
    topicMatchers.add(topicMatchersFactory.anyTopic())
    return this
  }

  TopicsBuilder configure(Closure<?> config) {
    config.delegate = this
    config.resolveStrategy = Closure.DELEGATE_ONLY
    config()
    return this
  }
  
  Array<TopicMatcher<AbstractTopic>> build() {
    return Array.copyOf(topicMatchers)
  }
}
