package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.CallId;
import javasabr.rlib.collections.array.Array;

public record AllOfCondition(Array<Condition> conditions) implements Condition {

  public AllOfCondition(Condition... conditions) {
    this(Array.of(conditions));
  }

  @Override
  public boolean test(CallId callId) {
    for (Condition condition : conditions) {
      if (!condition.test(callId)) {
        return false;
      }
    }
    return true;
  }
}
