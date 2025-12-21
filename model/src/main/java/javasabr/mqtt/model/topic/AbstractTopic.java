package javasabr.mqtt.model.topic;

import javasabr.mqtt.base.util.DebugUtils;
import javasabr.rlib.common.util.StringUtils;
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

  public static final String DELIMITER = "/";
  public static final char DELIMITER_CHAR = '/';


  static {
    DebugUtils.registerIncludedFields("rawTopic");
  }

  String[] segments;
  String rawTopic;
  int length;

  protected AbstractTopic(String rawTopicName) {
    length = rawTopicName.length();
    segments = splitTopic(rawTopicName);
    rawTopic = rawTopicName;
  }

  public boolean isShared() {
    return false;
  }

  public String segment(int level) {
    return segments[level];
  }

  public boolean isSingleLevelWildcard(int level) {
    String segment = segments[level];
    return segment.length() == 1 && segment.charAt(0) == TopicFilter.SINGLE_LEVEL_WILDCARD_CHAR;
  }

  public boolean isMultiLevelWildcard(int level) {
    String segment = segments[level];
    return segment.length() == 1 && segment.charAt(0) == TopicFilter.MULTI_LEVEL_WILDCARD_CHAR;
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

  protected static String[] splitTopic(String topic) {
    int segmentCount = countOccurrencesOf(topic, AbstractTopic.DELIMITER) + 1;
    var segments = new String[segmentCount];
    int i = 0, pos = 0, end;
    while ((end = topic.indexOf(AbstractTopic.DELIMITER, pos)) >= 0) {
      segments[i++] = topic.substring(pos, end);
      pos = end + 1;
    }
    segments[i] = topic.substring(pos);
    return segments;
  }

  protected static int countOccurrencesOf(String str, String sub) {
    if (StringUtils.isEmpty(str)) {
      return 0;
    }
    int count = 0;
    int pos = 0;
    int idx;
    while ((idx = str.indexOf(sub, pos)) != -1) {
      ++count;
      pos = idx + sub.length();
    }
    return count;
  }
}
