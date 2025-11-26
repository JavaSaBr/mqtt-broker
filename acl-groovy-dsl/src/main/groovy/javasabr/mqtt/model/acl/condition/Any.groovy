package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.CallId;

public record Any() implements Condition {

  @Override
  public boolean test(CallId callId) {
    return true;
  }
}
