package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.condition.AllOfCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.rlib.collections.array.Array

class AllOfUserConditionBuilder extends UserConditionBuilder {

  enum Identity {
    USER_NAME, CLIENT_ID, IP_ADDRESS
  }

  Set<Identity> alreadySetIdentities = new HashSet<>()
  
  AllOfUserConditionBuilder anyOf(Closure<?> config) {
    this.conditions.add(new AnyOfUserConditionBuilder().configure(config).build())
    return this
  }
  
  @Override
  UserConditionBuilder userName(ValueMatcher<String>... userNames) {
    requireSingleMatcher(Identity.USER_NAME, userNames)
    return super.userName(userNames)
  }

  @Override
  UserConditionBuilder clientId(ValueMatcher<String>... clientIds) {
    requireSingleMatcher(Identity.CLIENT_ID, clientIds)
    return super.clientId(clientIds)
  }

  UserConditionBuilder ipAddress(ValueMatcher<String>... ipAddresses) {
    requireSingleMatcher(Identity.IP_ADDRESS, ipAddresses)
    return super.ipAddress(ipAddresses)
  }

  private void requireSingleMatcher(Identity identity, ValueMatcher<String>[] newMatchers) {
    if (alreadySetIdentities.contains(identity) || newMatchers.length > 1) {
      throw new AclConfigurationException("AllOf condition can only have single-matcher members")
    } else {
      alreadySetIdentities.add(identity)
    }
  }

  MqttUserCondition build() {
    return new AllOfCondition(Array.copyOf(conditions))
  }
}
