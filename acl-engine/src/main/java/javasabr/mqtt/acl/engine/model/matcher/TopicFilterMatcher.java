package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicFilter;

public record TopicFilterMatcher(TopicFilter expectedTopic) implements ValueMatcher<AbstractTopic> {

  @Override
  public boolean test(AbstractTopic requestedTopic) {
    return matches(requestedTopic);
  }

  private boolean matches(AbstractTopic requestedTopicFilter) {
    final int expectedFilterLevels = expectedTopic.levelsCount();
    final int incomingFilterLevels = requestedTopicFilter.levelsCount();
    for (int i = 0; i < expectedFilterLevels; i++) {
      String expectedSegment = expectedTopic.segment(i);
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

  @Override
  public String toString() {
    return "Match:[" + expectedTopic + "]";
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
