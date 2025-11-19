package javasabr.mqtt.model.topic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class TopicFilter extends AbstractTopic {

  public static final String MULTI_LEVEL_WILDCARD = "#";
  public static final char MULTI_LEVEL_WILDCARD_CHAR = MULTI_LEVEL_WILDCARD.charAt(0);
  public static final String SINGLE_LEVEL_WILDCARD = "+";
  public static final char SINGLE_LEVEL_WILDCARD_CHAR = SINGLE_LEVEL_WILDCARD.charAt(0);
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
}

