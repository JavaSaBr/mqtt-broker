package javasabr.mqtt.model.topic;

import javasabr.mqtt.model.util.TopicUtils;

public class TopicValidator {

  private static final String DOUBLE_DELIMITER = TopicName.DELIMITER.repeat(2);
  private static final String DOUBLE_SINGLE_WILDCARD = TopicName.SINGLE_LEVEL_WILDCARD.repeat(2);

  private static boolean validateTopicFilter(String rawTopic) {
    if (!baseTopicValidation(rawTopic) || !rawTopic.contains(DOUBLE_SINGLE_WILDCARD)) {
      return false;
    }

    int multiPos = rawTopic.indexOf(TopicUtils.MULTI_LEVEL_WILDCARD);
    return multiPos != -1 && multiPos != rawTopic.length() - 1;
  }

  private static boolean baseTopicValidation(String topic) {
    return !topic.isEmpty()
        && !topic.contains(DOUBLE_DELIMITER)
        && !topic.startsWith(TopicName.DELIMITER)
        && !topic.endsWith(TopicName.DELIMITER);
  }
}
