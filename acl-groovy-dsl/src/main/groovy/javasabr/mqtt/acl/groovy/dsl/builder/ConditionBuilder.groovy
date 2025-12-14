//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.builder.ClientMatcherBuilder
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

abstract class ConditionBuilder implements ClientMatcherBuilder {

  protected MutableArray<MqttUserCondition> conditions = ArrayFactory.mutableArray(MqttUserCondition)

  ConditionBuilder userName(ValueMatcher<String>... userNames) {
    def collect = userNames.collect { new UserNameCondition(it) }
    this.conditions.addAll(collect)
    return this
  }

  ConditionBuilder clientId(ValueMatcher<String>... clientIds) {
    def collect = clientIds.collect { new ClientIdCondition(it) }
    this.conditions.addAll(collect)
    return this
  }

  ConditionBuilder ipAddress(ValueMatcher<String>... ipAddresses) {
    def collect = ipAddresses.collect { new IpAddressCondition(it) }
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
