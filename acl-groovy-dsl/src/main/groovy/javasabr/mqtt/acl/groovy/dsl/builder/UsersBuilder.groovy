//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.rlib.collections.array.Array

class UsersBuilder extends MultiUserConditionBuilder {

  UsersBuilder allOf(Closure<?> config) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new IllegalArgumentException("Already included any user condition")
    }
    conditions.add(new AllOfUserConditionBuilder().configure(config).build())
    return this
  }

  UsersBuilder anyOf(Closure<?> config) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new IllegalArgumentException("Already included any user condition")
    }
    conditions.add(new AnyOfUserConditionBuilder().configure(config).build())
    return this
  }

  UsersBuilder anyUser() {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    conditions.add(MqttUserCondition.MATCH_ANY)
    return this
  }

  @Override
  UserConditionBuilder userName(ValueMatcher<String> matcher) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    return super.userName(matcher)
  }

  @Override
  UserConditionBuilder userNames(Closure<?> config) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    return super.userNames(config)
  }
  
  @Override
  UserConditionBuilder clientId(ValueMatcher<String> matcher) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    return super.clientId(matcher)
  }

  @Override
  UserConditionBuilder clientIds(Closure<?> config) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    return super.clientIds(config)
  }

  @Override
  UserConditionBuilder ipAddress(ValueMatcher<String> matcher) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    return super.ipAddress(matcher)
  }

  @Override
  UserConditionBuilder ipAddresses(Closure<?> config) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition")
    }
    return super.ipAddresses(config)
  }

  @Override
  MqttUserCondition build() {
    if (conditions.size() > 1) {
      return new AnyOfCondition(Array.copyOf(conditions))
    }
    return conditions.isEmpty() ? MqttUserCondition.MATCH_NONE : conditions.first()
  }
}
