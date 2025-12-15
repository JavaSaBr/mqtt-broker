package javasabr.mqtt.acl.engine;

import javasabr.mqtt.acl.engine.builder.ClientMatcherBuilder;
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition;
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition;
import javasabr.mqtt.acl.engine.model.matcher.EqualsMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;

public interface ConditionMatcherAware extends ClientMatcherBuilder {

  default MqttUserCondition userNameEquals(String value) {
    return new UserNameCondition(new EqualsMatcher(value));
  }

  default MqttUserCondition userNameRegex(String value) {
    return new UserNameCondition(regex(value));
  }

  default MqttUserCondition clientIdEquals(String value) {
    return new ClientIdCondition(new EqualsMatcher(value));
  }

  default MqttUserCondition clientIdRegex(String value) {
    return new ClientIdCondition(regex(value));
  }

  default MqttUserCondition ipAddressEquals(String value) {
    return new IpAddressCondition(new EqualsMatcher(value));
  }

  default MqttUserCondition ipAddressRegex(String value) {
    return new IpAddressCondition(regex(value));
  }

  default TopicCondition topicNameCondition(String value) {
    return new TopicCondition(Array.of(new TopicNameMatcher(TopicName.valueOf(value))));
  }

  default TopicCondition topicFilterCondition(String value) {
    return new TopicCondition(Array.of(new TopicFilterMatcher(TopicFilter.valueOf(value))));
  }
}

