package javasabr.mqtt.model.acl.matcher;

import static javasabr.mqtt.model.topic.AbstractTopic.DELIMITER_CHAR;
import static javasabr.mqtt.model.topic.TopicFilter.MULTI_LEVEL_WILDCARD;
import static javasabr.mqtt.model.topic.TopicFilter.SINGLE_LEVEL_WILDCARD;

import java.util.Objects;
import javasabr.mqtt.model.topic.TopicFilter;

public record TopicFilterMatcher(TopicFilter expectedValue) implements ValueMatcher<String> {

  @Override
  public boolean test(String incomingValue) {
    return matches(incomingValue);
  }

  private boolean matches(String topic) {
    int topicLength = topic.length();
    int topicPosition = 0;

    for (int i = 0; i < expectedValue.levelsCount(); i++) {
      if (Objects.equals(expectedValue.segment(i), MULTI_LEVEL_WILDCARD)) {
        return i == expectedValue.levelsCount() - 1;
      }
      if (topicPosition > topicLength) return false;
      if (topicPosition == topicLength) return false;
      int segmentEnd = topicPosition;
      while (segmentEnd < topicLength && topic.charAt(segmentEnd) != DELIMITER_CHAR) {
        segmentEnd++;
      }
      int segmentLength = segmentEnd - topicPosition;
      if (Objects.equals(expectedValue.segment(i), SINGLE_LEVEL_WILDCARD)) {
        topicPosition = (segmentEnd == topicLength ? topicLength : segmentEnd + 1);
        continue;
      }
      String filterSeg = expectedValue.segment(i);
      if (filterSeg.length() != segmentLength) return false;
      for (int k = 0; k < segmentLength; k++) {
        if (filterSeg.charAt(k) != topic.charAt(topicPosition + k)) {
          return false;
        }
      }
      topicPosition = (segmentEnd == topicLength ? topicLength : segmentEnd + 1);
    }
    return topicPosition == topicLength;
  }
}
