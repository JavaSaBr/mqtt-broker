package javasabr.mqtt.legacy.model.topic;

import static javasabr.mqtt.legacy.util.TopicUtils.splitTopic;

import javasabr.mqtt.legacy.util.DebugUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = "rawTopic")
public abstract class AbstractTopic {

  static {
    DebugUtils.registerIncludedFields("rawTopic");
  }

  private static final String[] EMPTY_ARRAY = new String[0];
  private static final String EMPTY = "";
  private final String[] segments;
  private final String rawTopic;
  private final int length;

  AbstractTopic() {
    length = 0;
    segments = EMPTY_ARRAY;
    rawTopic = EMPTY;
  }

  AbstractTopic(String topicName) {
    length = topicName.length();
    segments = splitTopic(topicName);
    rawTopic = topicName;
  }

  String getSegment(int level) {
    return segments[level];
  }

  int levelsCount() {
    return segments.length;
  }

  String lastSegment() {
    return segments[segments.length - 1];
  }

  @Override
  public String toString() {
    return rawTopic;
  }
}
