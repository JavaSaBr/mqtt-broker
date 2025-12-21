package javasabr.mqtt.acl.groovy.dsl.builder

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
      throw new IllegalArgumentException("Already included any user condition")
    }
    conditions.add(MqttUserCondition.MATCH_ANY)
    return this
  }

  @Override
  UserConditionBuilder userName(ValueMatcher<String> userName) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new IllegalArgumentException("Already included any user condition")
    }
    return super.userName(userName)
  }

  @Override
  UserConditionBuilder userNames(Collection<ValueMatcher<String>> userNames) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new IllegalArgumentException("Already included any user condition")
    }
    return super.userNames(userNames)
  }

  @Override
  UserConditionBuilder clientId(ValueMatcher<String> clientId) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new IllegalArgumentException("Already included any user condition")
    }
    return super.clientId(clientId)
  }

  @Override
  UserConditionBuilder clientIds(Collection<ValueMatcher<String>> clientIds) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new IllegalArgumentException("Already included any user condition")
    }
    return super.clientIds(clientIds)
  }


  @Override
  UserConditionBuilder ipAddress(ValueMatcher<String> ipAddress) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new IllegalArgumentException("Already included any user condition")
    }
    return super.ipAddress(ipAddress)
  }

  @Override
  UserConditionBuilder ipAddresses(Collection<ValueMatcher<String>> ipAddresses) {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new IllegalArgumentException("Already included any user condition")
    }
    return super.ipAddresses(ipAddresses)
  }

  @Override
  MqttUserCondition build() {
    if (conditions.size() > 1) {
      return new AnyOfCondition(Array.copyOf(conditions))
    }
    return conditions.isEmpty() ? MqttUserCondition.MATCH_NONE : conditions.first()
  }
}
