package javasabr.mqtt.model.topic;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TopicFilter extends AbstractTopic {

  public static final TopicFilter INVALID_TOPIC_FILTER = new TopicFilter() {
    @Override
    public boolean isInvalid() {
      return true;
    }
  };

  public TopicFilter(String topicFilter) {
    super(topicFilter);
  }
}

