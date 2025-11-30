//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.MqttUserCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

abstract class ConditionBuilder implements ClientMatcherBuilder {

  protected MutableArray<MqttUserCondition> conditions = ArrayFactory.mutableArray(MqttUserCondition)

  ConditionBuilder userName(ValueMatcher<String>... username) {
    def collect = username.collect { new UserNameCondition(it) }
    this.conditions.addAll(collect)
    return this
  }

  ConditionBuilder clientId(ValueMatcher<String>... clientId) {
    def collect = clientId.collect { new ClientIdCondition(it) }
    this.conditions.addAll(collect)
    return this
  }

  ConditionBuilder ipAddress(ValueMatcher<String>... ipAddress) {
    def collect = ipAddress.collect { new IpAddressCondition(it) }
    this.conditions.addAll(collect)
    return this
  }

  ConditionBuilder buildCondition(Closure<?> config) {
    config.delegate = this
    config.resolveStrategy = Closure.DELEGATE_ONLY
    config()
    return this
  }

  abstract MqttUserCondition build()
}
