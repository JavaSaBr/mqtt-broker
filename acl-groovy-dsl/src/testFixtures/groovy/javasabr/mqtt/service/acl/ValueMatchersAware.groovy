package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.MqttUserCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.matcher.EqualsMatcher
import javasabr.mqtt.model.acl.matcher.RegexMatcher
import javasabr.mqtt.model.acl.matcher.TopicFilterMatcher
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.rlib.collections.array.Array

import java.util.regex.Pattern

interface ValueMatchersAware {

  default MqttUserCondition userNameEquals(String value) { new UserNameCondition(new EqualsMatcher(value)) }

  default MqttUserCondition userNameRegex(String value) { new UserNameCondition(new RegexMatcher(Pattern.compile(value))) }

  default MqttUserCondition clientIdEquals(String value) { new ClientIdCondition(new EqualsMatcher(value)) }

  default MqttUserCondition clientIdRegex(String value) { new ClientIdCondition(new RegexMatcher(Pattern.compile(value))) }

  default MqttUserCondition ipAddressEquals(String value) { new IpAddressCondition(new EqualsMatcher(value)) }

  default MqttUserCondition ipAddressRegex(String value) { new IpAddressCondition(new RegexMatcher(Pattern.compile(value))) }

  default TopicCondition topicCondition(String value) { new TopicCondition(Array.of(new EqualsMatcher(value)))  }

  default ValueMatcher<String> topicFilterMatcher(String value) { new TopicFilterMatcher(TopicFilter.valueOf(value)) }
}
