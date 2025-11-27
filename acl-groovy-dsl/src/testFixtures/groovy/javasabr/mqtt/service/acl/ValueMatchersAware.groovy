package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.matcher.EqualsMatcher
import javasabr.mqtt.model.acl.matcher.RegexMatcher
import javasabr.mqtt.model.acl.matcher.TopicFilterMatcher
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.topic.TopicFilter

import java.util.regex.Pattern

interface ValueMatchersAware {

  default Condition userNameEquals(String value) { new UserNameCondition(new EqualsMatcher(value)) }

  default Condition userNameRegex(String value) { new UserNameCondition(new RegexMatcher(Pattern.compile(value))) }

  default Condition clientIdEquals(String value) { new ClientIdCondition(new EqualsMatcher(value)) }

  default Condition clientIdRegex(String value) { new ClientIdCondition(new RegexMatcher(Pattern.compile(value))) }

  default Condition ipAddressEquals(String value) { new IpAddressCondition(new EqualsMatcher(value)) }

  default Condition ipAddressRegex(String value) { new IpAddressCondition(new RegexMatcher(Pattern.compile(value))) }

  default ValueMatcher<String> topicFilterMatcher(String value) { new TopicFilterMatcher(TopicFilter.valueOf(value)) }
}
