package javasabr.mqtt.model.topic;

import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.util.TopicUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@EqualsAndHashCode(of = "rawTopic")
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractTopic {

  static {
    DebugUtils.registerIncludedFields("rawTopic");
  }

  private static final String[] EMPTY_ARRAY = new String[0];
  private static final String EMPTY = "";

  String[] segments;
  String rawTopic;
  int length;

  protected AbstractTopic() {
    length = 0;
    segments = EMPTY_ARRAY;
    rawTopic = EMPTY;
  }

  protected AbstractTopic(String topicName) {
    length = topicName.length();
    segments = TopicUtils.splitTopic(topicName);
    rawTopic = topicName;
  }

  public String segment(int level) {
    return segments[level];
  }

  public int levelsCount() {
    return segments.length;
  }

  String lastSegment() {
    return segments[segments.length - 1];
  }

  public boolean isInvalid() {
    return false;
  }

  @Override
  public String toString() {
    return rawTopic;
  }
}
