package javasabr.mqtt.model.topic;

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

  public boolean isEmpty() {
    return false;
  }

  public static TopicName valueOf(String rawTopicName) {
    return new TopicName(rawTopicName);
  }
}

