package javasabr.mqtt.legacy.util;

import javasabr.mqtt.legacy.model.topic.SharedTopicFilter;
import javasabr.mqtt.legacy.model.topic.TopicFilter;
import javasabr.mqtt.legacy.model.topic.TopicName;
import org.springframework.util.StringUtils;

public class TopicUtils {

  private static final TopicFilter INVALID_TOPIC_FILTER = new TopicFilter();
  private static final TopicName INVALID_TOPIC_NAME = new TopicName();
  public static final TopicName EMPTY_TOPIC_NAME = new TopicName();

  private static final String SHARE_KEYWORD = "$share";
  private static final String DELIMITER = "/";
  public static final String MULTI_LEVEL_WILDCARD = "#";
  public static final String SINGLE_LEVEL_WILDCARD = "+";

  public static boolean isInvalid(TopicFilter topicFilter) {
    return topicFilter == INVALID_TOPIC_FILTER;
  }

  public static boolean isInvalid(TopicName topicName) {
    return topicName == INVALID_TOPIC_NAME;
  }

  public static boolean isShared(TopicFilter topicFilter) {
    return topicFilter instanceof SharedTopicFilter;
  }

  public static boolean hasWildcard(TopicFilter topicFilter) {
    var topic = topicFilter.getRawTopic();
    return topic.contains(SINGLE_LEVEL_WILDCARD) || topic.contains(MULTI_LEVEL_WILDCARD);
  }

  public static TopicName buildTopicName(String topicName) {
    if (isInvalidTopicName(topicName)) {
      return INVALID_TOPIC_NAME;
    } else {
      return new TopicName(topicName);
    }
  }

  public static TopicFilter buildTopicFilter(String topicFilter) {
    if (isInvalidTopicFilter(topicFilter)) {
      return INVALID_TOPIC_FILTER;
    } else if (isShared(topicFilter)) {
      return buildSharedTopicFilter(topicFilter);
    } else {
      return new TopicFilter(topicFilter);
    }
  }

  public static String[] splitTopic(String topic) {
    int segmentCount = StringUtils.countOccurrencesOf(topic, DELIMITER) + 1;
    var segments = new String[segmentCount];
    int i = 0, pos = 0, end;
    while ((end = topic.indexOf(DELIMITER, pos)) >= 0) {
      segments[i++] = topic.substring(pos, end);
      pos = end + 1;
    }
    segments[i] = topic.substring(pos);
    return segments;
  }

  private static TopicFilter buildSharedTopicFilter(String topicFilter) {
    int firstSlash = topicFilter.indexOf(DELIMITER) + 1;
    int secondSlash = topicFilter.indexOf(DELIMITER, firstSlash);
    String group = topicFilter.substring(firstSlash, secondSlash);
    if (group.isEmpty()) {
      return INVALID_TOPIC_FILTER;
    }
    var realTopicFilter = topicFilter.substring(secondSlash + 1);
    return new SharedTopicFilter(realTopicFilter, group);
  }

  private static boolean isInvalidTopicName(String topic) {
    return invalid(topic) || topic.contains(MULTI_LEVEL_WILDCARD) || topic.contains(SINGLE_LEVEL_WILDCARD);
  }

  private static boolean isInvalidTopicFilter(String topic) {
    if (TopicUtils.invalid(topic) || topic.contains("++")) {
      return true;
    }
    int multiPos = topic.indexOf(MULTI_LEVEL_WILDCARD);
    return multiPos != -1 && multiPos != topic.length() - 1;
  }

  private static boolean invalid(String topic) {
    return topic.isEmpty() || topic.contains("//") || topic.startsWith("/") || topic.endsWith("/");
  }

  private static boolean isShared(String topicFilter) {
    return topicFilter.startsWith(SHARE_KEYWORD);
  }
}
