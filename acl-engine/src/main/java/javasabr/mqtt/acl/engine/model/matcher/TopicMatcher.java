package javasabr.mqtt.acl.engine.model.matcher;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.AbstractTopic;

public interface TopicMatcher<T extends AbstractTopic> {

  TopicMatcher<AbstractTopic> MATCH_ANY = AnyTopicMatcher.instance();

  boolean test(MqttUser user, T topic);
}
