package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.CallId;
import javasabr.mqtt.model.acl.matcher.ClientMatcher;

public record ClientIdCondition(ClientMatcher<String> clientMatcher) implements Condition {
  @Override
  public boolean test(CallId callId) {
    return clientMatcher.test(callId.clientId());
  }
}
