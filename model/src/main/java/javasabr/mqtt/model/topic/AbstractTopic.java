package javasabr.mqtt.model.topic;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@EqualsAndHashCode(of = "rawTopic")
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractTopic {

  public static final String DELIMITER = "/";
  public static final char DELIMITER_CHAR = '/';
  
  static {
    DebugUtils.registerIncludedFields("rawTopic");
  }

  String[] segments;
  String rawTopic;

  protected AbstractTopic(String rawTopic) {
    this.segments = splitTopic(rawTopic);
    this.rawTopic = rawTopic;
  }

  protected AbstractTopic(String[] segments, String rawTopic) {
    this.segments = segments;
    this.rawTopic = rawTopic;
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

  /**
   * @return true if the anotherTopic is matched to this.
   */
  public abstract boolean isMatched(AbstractTopic anotherTopic);

  @JsonValue
  Object jsonDebugValue() {
    return rawTopic;
  }
  
  protected static String[] splitTopic(String topic) {
    int segmentCount = countOccurrencesOf(topic, AbstractTopic.DELIMITER) + 1;
    var segments = new String[segmentCount];
    int i = 0, pos = 0, end;
    while ((end = topic.indexOf(AbstractTopic.DELIMITER, pos)) >= 0) {
      segments[i++] = replaceWildcardToConstant(topic.substring(pos, end));
      pos = end + 1;
    }
    segments[i] = replaceWildcardToConstant(topic.substring(pos));
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

  protected static String replaceWildcardToConstant(String segment) {
    if (segment.length() > 1) {
      return segment;
    }
    if (TopicFilter.MULTI_LEVEL_WILDCARD.equals(segment)) {
      return TopicFilter.MULTI_LEVEL_WILDCARD;
    } else if (TopicFilter.SINGLE_LEVEL_WILDCARD.equals(segment)) {
      return TopicFilter.SINGLE_LEVEL_WILDCARD;
    }
    return segment;
  }
}
