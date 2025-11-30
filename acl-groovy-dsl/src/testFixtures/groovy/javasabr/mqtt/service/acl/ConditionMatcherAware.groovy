package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.MqttUserCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.matcher.EqualsMatcher
import javasabr.mqtt.service.acl.builder.ClientMatcherBuilder
import javasabr.rlib.collections.array.Array

interface ConditionMatcherAware extends ClientMatcherBuilder {

  default MqttUserCondition userNameEquals(String value) { new UserNameCondition(new EqualsMatcher(value)) }

  default MqttUserCondition userNameRegex(String value) { new UserNameCondition(regex(value)) }

  default MqttUserCondition clientIdEquals(String value) { new ClientIdCondition(new EqualsMatcher(value)) }

  default MqttUserCondition clientIdRegex(String value) { new ClientIdCondition(regex(value)) }

  default MqttUserCondition ipAddressEquals(String value) { new IpAddressCondition(new EqualsMatcher(value)) }

  default MqttUserCondition ipAddressRegex(String value) { new IpAddressCondition(regex(value)) }
}
