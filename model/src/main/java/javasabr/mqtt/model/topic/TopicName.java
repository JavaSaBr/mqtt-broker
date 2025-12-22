package javasabr.mqtt.model.topic;

import java.util.Objects;

public class TopicName extends AbstractTopic {

  public static final TopicName INVALID_TOPIC_NAME = new TopicName("$invalid$") {
    @Override
    public boolean isInvalid() {
      return true;
    }
  };

  public static final TopicName EMPTY_TOPIC_NAME = new TopicName("") {
    @Override
    public boolean isEmpty() {
      return true;
    }
  };

  public TopicName(String topicName) {
    super(topicName);
  }

  @Override
  public boolean isMatched(AbstractTopic anotherTopic) {
    if (anotherTopic == this) {
      return true;
    } else if (anotherTopic instanceof TopicFilter topicFilter && topicFilter.wildcard()) {
      return false;
    } else if (levelsCount() != anotherTopic.levelsCount()) {
      return false;
    }
    return Objects.equals(rawTopic(), anotherTopic.rawTopic());
  }

  public boolean isEmpty() {
    return false;
  }

  public static TopicName valueOf(String rawTopicName) {
    return new TopicName(rawTopicName);
  }
}

