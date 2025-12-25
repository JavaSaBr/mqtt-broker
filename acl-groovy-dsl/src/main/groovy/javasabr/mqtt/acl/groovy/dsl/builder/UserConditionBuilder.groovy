//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

abstract class UserConditionBuilder extends UserMatchersFactory {

  MutableArray<MqttUserCondition> conditions = ArrayFactory.mutableArray(MqttUserCondition)

  UserConditionBuilder userName(ValueMatcher<String> matcher) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    conditions.add(new UserNameCondition(matcher))
    return this
  }

  UserConditionBuilder clientId(ValueMatcher<String> matcher) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    conditions.add(new ClientIdCondition(matcher))
    return this
  }

  UserConditionBuilder ipAddress(ValueMatcher<String> matcher) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    conditions.add(new IpAddressCondition(matcher))
    return this
  }

  UserConditionBuilder configure(Closure<?> config) {
    config.delegate = this
    config.resolveStrategy = Closure.DELEGATE_ONLY
    config()
    return this
  }

  abstract MqttUserCondition build()
}
