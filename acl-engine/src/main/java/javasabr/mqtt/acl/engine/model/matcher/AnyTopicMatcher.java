package javasabr.mqtt.acl.engine.model.matcher;

import javasabr.mqtt.model.topic.AbstractTopic;

public record AnyTopicMatcher() implements ValueMatcher<AbstractTopic> {

  @Override
  public boolean test(AbstractTopic requestedTopic) {
    return true;
  }
}
