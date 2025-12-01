package javasabr.mqtt.model.acl.matcher;

import javasabr.mqtt.model.topic.AbstractTopic;

public record AnyTopicMatcher() implements ValueMatcher<AbstractTopic> {

  @Override
  public boolean test(AbstractTopic requestedTopic) {
    return true;
  }
}
