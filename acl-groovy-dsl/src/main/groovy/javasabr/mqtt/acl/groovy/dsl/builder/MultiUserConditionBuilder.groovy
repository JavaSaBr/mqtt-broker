//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition

abstract class MultiUserConditionBuilder extends UserConditionBuilder {

  UserConditionBuilder userNames(Closure<?> config) {
    def userNames = new UserMatchersBuilder()
        .configure(config)
        .build()
    def transformed = userNames.collect {
      new UserNameCondition(replaceMatcherIfNeed(it))
    }
    conditions.addAll(transformed)
    return this
  }

  UserConditionBuilder clientIds(Closure<?> config) {
    def clientIds = new UserMatchersBuilder()
        .configure(config)
        .build()
    def transformed = clientIds.collect {
      new ClientIdCondition(replaceMatcherIfNeed(it))
    }
    conditions.addAll(transformed)
    return this
  }

  UserConditionBuilder ipAddresses(Closure<?> config) {
    def ipAddresses = new UserMatchersBuilder()
        .configure(config)
        .build()
    def transformed = ipAddresses.collect {
      new IpAddressCondition(replaceMatcherIfNeed(it))
    }
    conditions.addAll(transformed)
    return this
  }
}
