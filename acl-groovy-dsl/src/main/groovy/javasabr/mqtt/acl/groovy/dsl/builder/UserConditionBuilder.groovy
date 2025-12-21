//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

abstract class UserConditionBuilder extends UserMatchersFactory {

  MutableArray<MqttUserCondition> conditions = ArrayFactory.mutableArray(MqttUserCondition)

  UserConditionBuilder userName(ValueMatcher<String> userName) {
    this.conditions.add(new UserNameCondition(userName))
    return this
  }
  
  UserConditionBuilder clientId(ValueMatcher<String> clientId) {
    this.conditions.add(new ClientIdCondition(clientId))
    return this
  }

  UserConditionBuilder ipAddress(ValueMatcher<String> ipAddress) {
    this.conditions.add(new IpAddressCondition(ipAddress))
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
