package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.CallId;
import javasabr.mqtt.model.acl.value.matcher.ClientMatcher;

public record UserNameCondition(ClientMatcher<String> clientMatcher) implements Condition {
  @Override
  public boolean test(CallId callId) {
    return clientMatcher.test(callId.username());
  }
}
