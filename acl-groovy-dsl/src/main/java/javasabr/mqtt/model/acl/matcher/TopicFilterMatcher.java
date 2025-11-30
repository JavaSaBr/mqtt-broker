package javasabr.mqtt.model.acl.matcher;

import java.util.Objects;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicFilter;

public record TopicFilterMatcher(AbstractTopic expectedTopicFilter) implements ValueMatcher<AbstractTopic> {

  @Override
  public boolean test(AbstractTopic requestedTopicFilter) {
    return matches(requestedTopicFilter);
  }

  private boolean matches(AbstractTopic requestedTopicFilter) {
    final int expectedFilterLevels = expectedTopicFilter.levelsCount();
    final int incomingFilterLevels = requestedTopicFilter.levelsCount();
    for (int i = 0; i < expectedFilterLevels; i++) {
      String expectedSegment = expectedTopicFilter.segment(i);
      if (Objects.equals(expectedSegment, TopicFilter.MULTI_LEVEL_WILDCARD)) {
        return i == expectedFilterLevels - 1;
      } else if (i >= incomingFilterLevels) {
        return false;
      }
      String requestedSegment = requestedTopicFilter.segment(i);
      if (Objects.equals(expectedSegment, TopicFilter.SINGLE_LEVEL_WILDCARD)) {
        continue;
      }
      if (!Objects.equals(expectedSegment, requestedSegment)) {
        return false;
      }
    }
    return expectedFilterLevels == incomingFilterLevels;
  }
}
