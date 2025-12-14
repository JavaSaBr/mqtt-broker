package javasabr.mqtt.service.acl

import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition
import javasabr.mqtt.acl.engine.model.matcher.EqualsMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.service.acl.builder.ClientMatcherBuilder
import javasabr.rlib.collections.array.Array

interface ConditionMatcherAware extends ClientMatcherBuilder {

  default MqttUserCondition userNameEquals(String value) { new UserNameCondition(new EqualsMatcher(value)) }

  default MqttUserCondition userNameRegex(String value) { new UserNameCondition(regex(value)) }

  default MqttUserCondition clientIdEquals(String value) { new ClientIdCondition(new EqualsMatcher(value)) }

  default MqttUserCondition clientIdRegex(String value) { new ClientIdCondition(regex(value)) }

  default MqttUserCondition ipAddressEquals(String value) { new IpAddressCondition(new EqualsMatcher(value)) }

  default MqttUserCondition ipAddressRegex(String value) { new IpAddressCondition(regex(value)) }

  default TopicCondition topicNameCondition(String value) {
    return new TopicCondition(Array.of(new TopicNameMatcher(TopicName.valueOf(value))));
  }

  default TopicCondition topicFilterCondition(String value) {
    return new TopicCondition(Array.of(new TopicFilterMatcher(TopicFilter.valueOf(value))));
  }
}
