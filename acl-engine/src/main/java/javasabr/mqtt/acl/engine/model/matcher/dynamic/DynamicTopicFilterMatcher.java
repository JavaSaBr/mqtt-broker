package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import javasabr.mqtt.model.topic.TopicFilter;

public class DynamicTopicFilterMatcher extends DynamicTopicMatcher<TopicFilter> {
  
  public DynamicTopicFilterMatcher(TopicFilter originalTopicFilter) {
    super(originalTopicFilter);
  }
  
  @Override
  protected TopicFilter constructTopic(String[] segments, String rawTopicFilter) {
    return new TopicFilter(segments, rawTopicFilter, originalTopic.wildcard());
  }
}
