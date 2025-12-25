//file:noinspection unused
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
  UserConditionBuilder userName(ValueMatcher<String> matcher) {
    requireSingleMatcher(Identity.USER_NAME, matcher)
    return super.userName(matcher)
  }

  @Override
  UserConditionBuilder clientId(ValueMatcher<String> matcher) {
    requireSingleMatcher(Identity.CLIENT_ID, matcher)
    return super.clientId(matcher)
  }

  UserConditionBuilder ipAddress(ValueMatcher<String> matcher) {
    requireSingleMatcher(Identity.IP_ADDRESS, matcher)
    return super.ipAddress(matcher)
  }

  private void requireSingleMatcher(Identity identity, ValueMatcher<String> matcher) {
    if (alreadySetIdentities.contains(identity)) {
      throw new AclConfigurationException("AllOf condition can only have single-matcher members")
    } else {
      alreadySetIdentities.add(identity)
    }
  }

  MqttUserCondition build() {
    return new AllOfCondition(Array.copyOf(conditions))
  }
}
