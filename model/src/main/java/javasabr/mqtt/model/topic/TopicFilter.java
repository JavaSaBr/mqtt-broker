package javasabr.mqtt.model.topic;

import java.util.Date;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
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

  public TopicFilter(String[] segments, String rawTopic, boolean wildcard) {
    super(segments, rawTopic);
    this.wildcard = wildcard;
  }

  @Override
  public boolean isMatched(AbstractTopic anotherTopic) {
    if (anotherTopic == this) {
      return true;
    } else if (!wildcard()) {
      if (levelsCount() != anotherTopic.levelsCount()) {
        return false;
      }
      return Objects.equals(rawTopic(), anotherTopic.rawTopic());
    }
    
    int levels = levelsCount();
    int anotherTopicLevels = anotherTopic.levelsCount();

    for (int level = 0; level < levels; level++) {
      String segment = segment(level);
      if (isMultiLevelWildcard(segment)) {
        // it always will be the last segment
        return true;
      } else if (level >= anotherTopicLevels) {
        return false;
      } else if (isSingleLevelWildcard(segment)) {
        continue;
      }
      String segmentToCompare = anotherTopic.segment(level);
      if (!Objects.equals(segment, segmentToCompare)) {
        return false;
      }
    }
    return levels == anotherTopicLevels;
  }

  public static TopicFilter valueOf(String rawTopicFilter) {
    return new TopicFilter(rawTopicFilter);
  }
  
  public static boolean isMultiLevelWildcard(String segment) {
    // we always replace such segments to a constant
    //noinspection StringEquality
    return segment == MULTI_LEVEL_WILDCARD;
  }
  
  public static boolean isSingleLevelWildcard(String segment) {
    // we always replace such segments to a constant
    //noinspection StringEquality
    return segment == SINGLE_LEVEL_WILDCARD;
  }
}

