package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.exception.AclConfigurationException
import javasabr.rlib.collections.array.Array

class AllOfBuilder extends ConditionBuilder {

  @Override
  ConditionBuilder userName(ValueMatcher<String>... username) {
    requireSingleMatcher(username, userNames)
    super.userName(username)
  }

  @Override
  ConditionBuilder clientId(ValueMatcher<String>... clientId) {
    requireSingleMatcher(clientId, clientIds)
    super.clientId(clientId)
  }

  ConditionBuilder ipAddress(ValueMatcher<String>... ipAddress) {
    requireSingleMatcher(ipAddress, ipAddresses)
    super.ipAddress(ipAddress)
  }

  private static void requireSingleMatcher(ValueMatcher<String>[] newMatchers, Array<Condition> existingConditions) {
    if (newMatchers.length != 1 || !existingConditions.isEmpty()) {
      throw new AclConfigurationException("AllOf condition can only have a single-matcher members")
    }
  }

  Condition build() {
    new AllOfCondition(Array.copyOf(conditions))
  }
}
