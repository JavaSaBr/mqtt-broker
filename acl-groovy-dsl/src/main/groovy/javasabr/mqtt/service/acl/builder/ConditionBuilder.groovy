//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.condition.AnyCondition
import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.matcher.ClientMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

abstract class ConditionBuilder implements ValueMatcherBuilder {

  public static final Condition MATCH_ANY = new AnyCondition()

  protected MutableArray<Condition> conditions = ArrayFactory.mutableArray(Condition)

  ConditionBuilder userName(ClientMatcher<String>... username) {
    this.conditions.addAll(username.collect { new UserNameCondition(it) })
    return this
  }

  ConditionBuilder clientId(ClientMatcher<String>... clientId) {
    this.conditions.addAll(clientId.collect { new ClientIdCondition(it) })
    return this
  }

  ConditionBuilder ipAddress(ClientMatcher<String>... ipAddress) {
    this.conditions.addAll(ipAddress.collect { new IpAddressCondition(it) })
    this
  }

  ConditionBuilder allOf(Closure<?> config) {
    this.conditions << new AllOfBuilder().buildCondition(config).build()
    return this
  }

  ConditionBuilder anyOf(Closure<?> config) {
    this.conditions << new AnyOfBuilder().buildCondition(config).build()
    return this
  }

  ConditionBuilder buildCondition(Closure<?> config) {
    config.delegate = this
    config.resolveStrategy = Closure.DELEGATE_ONLY
    config()
    return this
  }

  ConditionBuilder any() { this.conditions << MATCH_ANY; this }

  abstract Condition build()
}
