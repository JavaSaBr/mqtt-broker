package javasabr.mqtt.model.topic;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class TopicFilter extends AbstractTopic {

  public static final String MULTI_LEVEL_WILDCARD = "#";
  public static final char MULTI_LEVEL_WILDCARD_CHAR = '#';
  public static final String SINGLE_LEVEL_WILDCARD = "+";
  public static final char SINGLE_LEVEL_WILDCARD_CHAR = '+';
  public static final String SPECIAL = "$";

  public static final TopicFilter INVALID_TOPIC_FILTER = new TopicFilter("$invalid$") {
    @Override
    public boolean isInvalid() {
      return true;
    }
  };

  boolean wildcard;

  public TopicFilter(String rawTopicFilter) {
    super(rawTopicFilter);
    this.wildcard = rawTopicFilter.contains(SINGLE_LEVEL_WILDCARD) || rawTopicFilter.contains(MULTI_LEVEL_WILDCARD);
  }

  public static TopicFilter valueOf(String rawTopicFilter) {
    return new TopicFilter(rawTopicFilter);
  }

  public boolean matches(String topic) {
    int topicLength = topic.length();
    int topicPosition = 0;

    for (int i = 0; i < segments.length; i++) {
      if (Objects.equals(segments[i], MULTI_LEVEL_WILDCARD)) {
        return i == segments.length - 1;
      }
      if (topicPosition > topicLength) return false;
      if (topicPosition == topicLength) return false;
      int segmentEnd = topicPosition;
      while (segmentEnd < topicLength && topic.charAt(segmentEnd) != DELIMITER_CHAR) {
        segmentEnd++;
      }
      int segmentLength = segmentEnd - topicPosition;
      if (Objects.equals(segments[i], SINGLE_LEVEL_WILDCARD)) {
        topicPosition = (segmentEnd == topicLength ? topicLength : segmentEnd + 1);
        continue;
      }
      String filterSeg = segments[i];
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

