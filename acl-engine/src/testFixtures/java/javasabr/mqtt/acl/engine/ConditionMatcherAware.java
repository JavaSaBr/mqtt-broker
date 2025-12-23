package javasabr.mqtt.acl.engine;

import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition;
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition;
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicMatchers;
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher;
import javasabr.mqtt.acl.engine.model.matcher.UserMatchers;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;

public interface ConditionMatcherAware {

  default MqttUserCondition userNameEquals(String value) {
    return new UserNameCondition(UserMatchers.eq(value));
  }

  default MqttUserCondition userNameRegex(String value) {
    return new UserNameCondition(UserMatchers.regex(value));
  }

  default MqttUserCondition clientIdEquals(String value) {
    return new ClientIdCondition(UserMatchers.eq(value));
  }

  default MqttUserCondition clientIdRegex(String value) {
    return new ClientIdCondition(UserMatchers.regex(value));
  }

  default MqttUserCondition ipAddressEquals(String value) {
    return new IpAddressCondition(UserMatchers.eq(value));
  }

  default MqttUserCondition ipAddressRegex(String value) {
    return new IpAddressCondition(UserMatchers.regex(value));
  }

  default TopicMatcher topicEq(String value) {
    return TopicMatchers.eq(value);
  }

  default TopicMatcher topicMatch(String value) {
    return TopicMatchers.match(value);
  }
  
  default TopicCondition topicNameCondition(String value) {
    return new TopicCondition(Array.of(new TopicNameMatcher(TopicName.valueOf(value))));
  }

  default TopicCondition topicFilterCondition(String value) {
    return new TopicCondition(Array.of(new TopicFilterMatcher(TopicFilter.valueOf(value))));
  }
}

