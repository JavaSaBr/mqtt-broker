//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher

abstract class MultiUserConditionBuilder extends UserConditionBuilder {

  UserConditionBuilder userNames(Collection<? extends ValueMatcher<String>> userNames) {
    def transformed = userNames.collect {
      new UserNameCondition(replaceMatcherIfNeed(it))
    }
    this.conditions.addAll(transformed)
    return this
  }

  UserConditionBuilder clientIds(Collection<? extends ValueMatcher<String>> clientIds) {
    def transformed = clientIds.collect {
      new ClientIdCondition(replaceMatcherIfNeed(it))
    }
    this.conditions.addAll(transformed)
    return this
  }

  UserConditionBuilder ipAddresses(Collection<? extends ValueMatcher<String>> ipAddresses) {
    def transformed = ipAddresses.collect {
      new IpAddressCondition(replaceMatcherIfNeed(it))
    }
    this.conditions.addAll(transformed)
    return this
  }
}
