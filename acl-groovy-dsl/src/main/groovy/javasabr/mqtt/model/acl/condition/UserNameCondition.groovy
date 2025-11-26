package javasabr.mqtt.model.acl.condition

import groovy.transform.ImmutableOptions
import javasabr.mqtt.model.acl.CallId
import javasabr.mqtt.model.acl.value.matcher.ClientMatcher

@ImmutableOptions(knownImmutableClasses = ClientMatcher)
record UserNameCondition(ClientMatcher<String> clientMatcher) implements Condition {
  @Override
  boolean test(CallId callId) {
    return clientMatcher.test(callId.username())
  }
}
