package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.CallId;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;

public record IpAddressCondition(ValueMatcher<String> clientMatcher) implements Condition {
  @Override
  public boolean test(CallId callId) {
    return clientMatcher.test(callId.ipAddress());
  }
}
