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
    final int topicLength = topic.length();
    int topicPosition = 0;

    final int totalLevels;
    if (topicLength == 0) {
      totalLevels = 1;
    } else {
      int slashCount = 0;
      for (int i = 0; i < topicLength; i++) {
        if (topic.charAt(i) == DELIMITER_CHAR) {
          slashCount++;
        }
      }
      totalLevels = 1 + slashCount;
    }
    int consumedLevels = 0;

    for (int i = 0; i < expectedValue.levelsCount(); i++) {
      String filterSegment = expectedValue.segment(i);
      if (Objects.equals(filterSegment, MULTI_LEVEL_WILDCARD)) {
        return i == expectedValue.levelsCount() - 1;
      }
      if (consumedLevels >= totalLevels) {
        return false;
      }
      final int segmentStart = topicPosition;
      int segmentEnd = segmentStart;
      while (segmentEnd < topicLength && topic.charAt(segmentEnd) != DELIMITER_CHAR) {
        segmentEnd++;
      }
      final int segmentLength = segmentEnd - segmentStart;
      if (Objects.equals(filterSegment, SINGLE_LEVEL_WILDCARD)) {
        consumedLevels++;
        topicPosition = (segmentEnd < topicLength ? segmentEnd + 1 : topicLength);
        continue;
      }
      if (filterSegment.length() != segmentLength) {
        return false;
      }
      for (int k = 0; k < segmentLength; k++) {
        if (filterSegment.charAt(k) != topic.charAt(segmentStart + k)) {
          return false;
        }
      }
      consumedLevels++;
      topicPosition = (segmentEnd < topicLength ? segmentEnd + 1 : topicLength);
    }
    return consumedLevels == totalLevels;
  }
}
