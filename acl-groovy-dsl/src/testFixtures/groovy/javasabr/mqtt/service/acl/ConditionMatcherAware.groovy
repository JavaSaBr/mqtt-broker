package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.MqttUserCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.service.acl.builder.ValueMatcherBuilder
import javasabr.rlib.collections.array.Array

interface ConditionMatcherAware extends ValueMatcherBuilder {

  default MqttUserCondition userNameEquals(String value) { new UserNameCondition(eq(value)) }

  default MqttUserCondition userNameRegex(String value) { new UserNameCondition(regex(value)) }

  default MqttUserCondition clientIdEquals(String value) { new ClientIdCondition(eq(value)) }

  default MqttUserCondition clientIdRegex(String value) { new ClientIdCondition(regex(value)) }

  default MqttUserCondition ipAddressEquals(String value) { new IpAddressCondition(eq(value)) }

  default MqttUserCondition ipAddressRegex(String value) { new IpAddressCondition(regex(value)) }

  default TopicCondition topicCondition(String value) { new TopicCondition(Array.of(eq(value))) }

  default ValueMatcher<String> topicFilterMatcher(String value) { match(value) }
}
