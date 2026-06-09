package javasabr.mqtt.acl.mug.dsl.builder;

import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher;
import javasabr.mqtt.acl.engine.model.matcher.dynamic.DynamicTopicMatcher;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;

public class TopicsBuilder {

  private final MutableArray<TopicMatcher> matchers = ArrayFactory.mutableArray(TopicMatcher.class);

  public TopicsBuilder eq(String rawTopicName) {
    checkAnyTopic();
    if (!TopicValidator.validateTopicName(rawTopicName)) {
      throw new AclConfigurationException("Invalid topic name:[%s]".formatted(rawTopicName));
    }
    matchers.add(new TopicNameMatcher(TopicName.valueOf(rawTopicName)));
    return this;
  }

  public TopicsBuilder match(String rawTopicFilter) {
    checkAnyTopic();
    if (!TopicValidator.validateTopicFilter(rawTopicFilter)) {
      throw new AclConfigurationException("Invalid topic filter:[%s]".formatted(rawTopicFilter));
    }
    matchers.add(new TopicFilterMatcher(TopicFilter.valueOf(rawTopicFilter)));
    return this;
  }

  public TopicsBuilder dynamic(String rawTopic) {
    checkAnyTopic();
    try {
      matchers.add(DynamicTopicMatcher.autoBuild(rawTopic));
    } catch (RuntimeException e) {
      throw new AclConfigurationException(e.getMessage());
    }
    return this;
  }

  public TopicsBuilder anyTopic() {
    checkAnyTopic();
    matchers.add(TopicMatcher.MATCH_ANY);
    return this;
  }

  private void checkAnyTopic() {
    if (matchers.contains(TopicMatcher.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any topic condition");
    }
  }

  public Array<TopicMatcher> build() {
    return Array.copyOf(matchers);
  }
}
