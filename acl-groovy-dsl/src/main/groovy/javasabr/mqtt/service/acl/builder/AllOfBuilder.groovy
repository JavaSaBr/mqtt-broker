package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.MqttUserCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.exception.AclConfigurationException
import javasabr.rlib.collections.array.Array

class AllOfBuilder extends ConditionBuilder {

  enum Identity {
    USER_NAME, CLIENT_ID, IP_ADDRESS
  }

  private final Set<Identity> alreadySetIdentities = new HashSet<>()

  @Override
  ConditionBuilder userName(ValueMatcher<String>... userNames) {
    requireSingleMatcher(Identity.USER_NAME, userNames)
    super.userName(userNames)
  }

  @Override
  ConditionBuilder clientId(ValueMatcher<String>... clientIds) {
    requireSingleMatcher(Identity.CLIENT_ID, clientIds)
    super.clientId(clientIds)
  }

  ConditionBuilder ipAddress(ValueMatcher<String>... ipAddresses) {
    requireSingleMatcher(Identity.IP_ADDRESS, ipAddresses)
    super.ipAddress(ipAddresses)
  }

  private void requireSingleMatcher(Identity identity, ValueMatcher<String>[] newMatchers) {
    if (alreadySetIdentities.contains(identity) || newMatchers.length > 1) {
      throw new AclConfigurationException("AllOf condition can only have a single-matcher members")
    } else {
      alreadySetIdentities.add(identity)
    }
  }

  MqttUserCondition build() {
    new AllOfCondition(Array.copyOf(conditions))
  }
}
