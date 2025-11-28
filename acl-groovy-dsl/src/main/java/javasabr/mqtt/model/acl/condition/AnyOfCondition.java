package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.CallId;
import javasabr.rlib.collections.array.Array;

public record AnyOfCondition(Array<Condition> conditions) implements Condition {

  public AnyOfCondition(Condition... conditions) {
    this(Array.of(conditions));
  }

  @Override
  public boolean test(CallId callId) {
    for (Condition condition : conditions) {
      if (condition.test(callId)) {
        return true;
      }
    }
    return false;
  }
}
